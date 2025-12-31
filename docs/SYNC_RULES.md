# Cloud Sync Rules

## Overview

Sync architecture is **offline-first**: local database is source of truth. Cloud sync runs opportunistically when network is available.

## Authentication

### Providers
- **iOS**: Sign in with Apple (required for App Store), Google Sign-In (optional)
- **Android**: Google Sign-In (primary), Email/Password (fallback)

### Anonymous to Authenticated Migration
```
1. User starts with anonymous auth (automatic)
2. Recipes stored locally + synced to anonymous user's cloud space
3. When user signs in, merge anonymous data into authenticated account
4. Delete anonymous account after successful migration
```

---

## Firestore Collections Structure

```
/users/{userId}
  - email: String
  - displayName: String
  - avatarUrl: String?
  - createdAt: Timestamp
  - lastActiveAt: Timestamp

/families/{familyId}
  - name: String
  - inviteCodeHash: String          # SHA-256 hash, not raw code
  - inviteCodeCreatedAt: Timestamp
  - inviteCodeAttempts: Number      # Rate limiting counter
  - inviteCodeLastAttemptAt: Timestamp
  - adminMemberId: String
  - theme: {
      templateKey: String           # "vintage" | "modern" | "playful"
      accentColorOverride: String?  # Hex color
      fontScaleOverride: Number?    # 0.8 - 1.4
    }
  - language: String                # "en" | "es" | "hi"
  - units: String                   # "metric" | "imperial"
  - createdAt: Timestamp
  - updatedAt: Timestamp

/families/{familyId}/members/{memberId}
  - userId: String                  # Links to /users/{userId}
  - name: String
  - avatarEmoji: String
  - role: String                    # "admin" | "member"
  - status: String                  # "active" | "removed" | "left"
  - joinedAt: Timestamp
  - removedAt: Timestamp?
  - removedBy: String?              # memberId who removed
  - preferences: {
      fontScale: Number?            # Member accessibility override
      reducedMotion: Boolean?
    }

/families/{familyId}/recipes/{recipeId}
  - title: String
  - recipeDescription: String
  - category: String
  - difficulty: String
  - prepTimeMinutes: Number
  - cookTimeMinutes: Number
  - servings: Number
  - familyMemory: String?
  - createdById: String             # memberId
  - favoritedBy: [String]           # [memberId, ...]
  - createdAt: Timestamp
  - updatedAt: Timestamp
  - deletedAt: Timestamp?           # Soft delete

/families/{familyId}/recipes/{recipeId}/ingredients/{ingredientId}
  - name: String
  - quantity: Number
  - unit: String
  - notes: String?
  - order: Number

/families/{familyId}/recipes/{recipeId}/instructions/{instructionId}
  - stepNumber: Number
  - text: String
  - durationSeconds: Number?
  - imageUrl: String?
  - order: Number
```

---

## Conflict Resolution

### Strategy: Last-Write-Wins + Merge-by-ID

```
Conflict Type          Resolution
─────────────────────────────────────────────────────────────
Scalar fields          Last updatedAt wins
  (title, description)

Array fields           Merge by ID, deduplicate
  (favoritedBy)        Union of both sets

Nested documents       Each document resolved independently
  (ingredients)        by its own updatedAt

Deleted vs Modified    If deletedAt exists and is newer than
                       updatedAt, document is deleted
```

### Sync Algorithm

```
1. On app launch:
   - Compare local lastSyncAt with server
   - Pull all documents where updatedAt > lastSyncAt
   - For each document:
     a. If local doesn't exist → insert
     b. If local.updatedAt < server.updatedAt → update local
     c. If local.updatedAt > server.updatedAt → push local
     d. If equal → no action

2. On local write:
   - Write to local DB immediately
   - Queue for sync
   - Attempt sync if online

3. On conflict (both modified since last sync):
   - Compare updatedAt timestamps
   - Winner overwrites loser
   - Log conflict for debugging
```

### Merge Example: favoritedBy

```
Local:  ["member-a", "member-b"]
Server: ["member-a", "member-c"]

Result: ["member-a", "member-b", "member-c"]  # Union
```

---

## Invite Code Security

### Generation
```
- 6 alphanumeric characters (uppercase + digits, no ambiguous chars)
- Exclude: 0, O, 1, I, L (avoid confusion)
- Alphabet: ABCDEFGHJKMNPQRSTUVWXYZ23456789 (32 chars)
- Entropy: 32^6 ≈ 1 billion combinations
```

### Storage
```
- Store SHA-256 hash of invite code in Firestore
- Never store raw code in cloud
- Raw code shown only to admin, stored locally with expiration
```

### Rate Limiting
```
- Max 5 attempts per invite code per hour
- After 5 failures, lock code for 1 hour
- Track in inviteCodeAttempts and inviteCodeLastAttemptAt
```

### Expiration & Rotation
```
- Invite codes expire after 7 days
- Admin can rotate code anytime (invalidates old code)
- On rotation:
  1. Generate new code
  2. Update inviteCodeHash
  3. Reset inviteCodeCreatedAt
  4. Reset inviteCodeAttempts to 0
```

### Join Flow
```
1. User enters 6-char code
2. Client hashes code locally
3. Query: find family where inviteCodeHash == hash
4. If found:
   a. Check expiration (createdAt + 7 days)
   b. Check rate limit (attempts < 5 in last hour)
   c. If valid, add user as member with status: "active"
5. If not found or invalid:
   a. Increment attempts counter
   b. Return generic "Invalid code" error
```

---

## Membership Lifecycle

### States
```
active   → Normal member, full access
removed  → Removed by admin, no access, can rejoin with new invite
left     → Left voluntarily, no access, can rejoin with new invite
```

### Transitions
```
                  ┌─────────────────┐
                  │     (none)      │
                  └────────┬────────┘
                           │ Join via invite code
                           ▼
                  ┌─────────────────┐
            ┌─────│     active      │─────┐
            │     └─────────────────┘     │
            │              │              │
   Admin removes           │        Member leaves
            │              │              │
            ▼              │              ▼
   ┌─────────────────┐     │     ┌─────────────────┐
   │     removed     │     │     │      left       │
   └─────────────────┘     │     └─────────────────┘
            │              │              │
            │   Rejoin with new code      │
            └──────────────┴──────────────┘
```

### Admin Transfer
```
1. Current admin selects new admin from active members
2. Update family.adminMemberId
3. Update old admin's role to "member"
4. Update new admin's role to "admin"
```

### Member Removal
```
1. Admin initiates removal
2. Set member.status = "removed"
3. Set member.removedAt = now
4. Set member.removedBy = adminMemberId
5. Member's device: clear local family data on next sync
```

---

## Security Rules (Firestore)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }

    // Families collection
    match /families/{familyId} {
      // Helper functions
      function isMember() {
        return exists(/databases/$(database)/documents/families/$(familyId)/members/$(request.auth.uid))
          && get(/databases/$(database)/documents/families/$(familyId)/members/$(request.auth.uid)).data.status == 'active';
      }

      function isAdmin() {
        return isMember()
          && resource.data.adminMemberId == request.auth.uid;
      }

      // Family document
      allow read: if isMember();
      allow create: if request.auth != null;
      allow update: if isAdmin();
      allow delete: if isAdmin();

      // Members subcollection
      match /members/{memberId} {
        allow read: if isMember();
        allow create: if request.auth != null; // Join flow
        allow update: if isAdmin() || request.auth.uid == memberId;
        allow delete: if isAdmin();
      }

      // Recipes subcollection
      match /recipes/{recipeId} {
        allow read: if isMember();
        allow create: if isMember();
        allow update: if isMember();
        allow delete: if isMember()
          && (resource.data.createdById == request.auth.uid || isAdmin());

        // Ingredients
        match /ingredients/{ingredientId} {
          allow read, write: if isMember();
        }

        // Instructions
        match /instructions/{instructionId} {
          allow read, write: if isMember();
        }
      }
    }
  }
}
```

---

## Sync Frequency

| Event | Sync Trigger |
|-------|--------------|
| App launch | Full sync (delta from lastSyncAt) |
| Recipe created/edited | Immediate push |
| Recipe deleted | Immediate push (soft delete) |
| Family settings changed | Immediate push |
| Member joined/left | Immediate push |
| Background (iOS) | BGAppRefreshTask every 15 min |
| Background (Android) | WorkManager periodic (15 min) |
| Network reconnect | Full sync |

---

## Offline Behavior

| Scenario | Behavior |
|----------|----------|
| Create recipe offline | Saved locally, queued for sync |
| Edit recipe offline | Saved locally, queued for sync |
| Delete recipe offline | Marked deleted locally, queued |
| Join family offline | Not possible (requires code verification) |
| View recipes offline | Full access to local cache |
| Cooking mode offline | Full functionality |

---

## Data Retention

| Data Type | Retention |
|-----------|-----------|
| Active recipes | Indefinite |
| Soft-deleted recipes | 30 days, then hard delete |
| Removed members | Membership record kept, access revoked |
| Sync logs | 7 days |
| Invite code attempts | 24 hours |
