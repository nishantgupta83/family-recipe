package com.familyrecipe.core.services.assistant

// MARK: - Knowledge Base

class KnowledgeBase {

    fun getSubstitutions(ingredient: String): List<Substitution>? {
        val key = ingredient.lowercase()

        // Exact match
        substitutions[key]?.let { return it }

        // Partial match
        for ((ingredientKey, subs) in substitutions) {
            if (ingredientKey.contains(key) || key.contains(ingredientKey)) {
                return subs
            }
        }
        return null
    }

    fun getTechnique(name: String): TechniqueInfo? {
        val key = name.lowercase()

        // Exact match
        techniques[key]?.let { return it }

        // Partial match
        for ((techniqueKey, info) in techniques) {
            if (techniqueKey.contains(key) || key.contains(techniqueKey)) {
                return info
            }
        }
        return null
    }

    private val substitutions = mapOf(
        "eggs" to listOf(
            Substitution("applesauce", "1/4 cup per egg", "Best for baking"),
            Substitution("flax egg", "1 tbsp ground flax + 3 tbsp water", "Vegan, let sit 5 min"),
            Substitution("mashed banana", "1/4 cup per egg", "Adds sweetness"),
            Substitution("chia egg", "1 tbsp chia + 3 tbsp water", "Vegan")
        ),
        "butter" to listOf(
            Substitution("coconut oil", "1:1", "Solid at room temp"),
            Substitution("olive oil", "3/4 of butter amount", "Good for savory"),
            Substitution("applesauce", "1/2 of butter amount", "Reduces fat"),
            Substitution("Greek yogurt", "1/2 of butter amount", "Adds moisture")
        ),
        "milk" to listOf(
            Substitution("oat milk", "1:1", "Creamy, neutral"),
            Substitution("almond milk", "1:1", "Slightly nutty"),
            Substitution("coconut milk", "1:1", "Richer, coconut flavor")
        ),
        "flour" to listOf(
            Substitution("almond flour", "1:1 (may need more)", "Gluten-free"),
            Substitution("oat flour", "1 1/4 cup per cup", "Blend oats"),
            Substitution("coconut flour", "1/4 cup per cup", "Very absorbent")
        ),
        "sugar" to listOf(
            Substitution("honey", "3/4 cup per cup sugar", "Reduce liquids"),
            Substitution("maple syrup", "3/4 cup per cup sugar", "Adds flavor"),
            Substitution("stevia", "1 tsp per cup sugar", "Very concentrated")
        ),
        "sour cream" to listOf(
            Substitution("Greek yogurt", "1:1", "Lower fat"),
            Substitution("cottage cheese (blended)", "1:1", "Blend smooth")
        ),
        "heavy cream" to listOf(
            Substitution("coconut cream", "1:1", "Vegan"),
            Substitution("milk + butter", "3/4 cup milk + 1/4 cup butter", "Won't whip")
        ),
        "garlic" to listOf(
            Substitution("garlic powder", "1/8 tsp per clove", "Less pungent")
        ),
        "onion" to listOf(
            Substitution("onion powder", "1 tbsp per medium onion", "For flavor"),
            Substitution("shallots", "3 shallots per onion", "Milder")
        )
    )

    private val techniques = mapOf(
        "sauté" to TechniqueInfo(
            "Cook quickly in a small amount of fat over medium-high heat, stirring frequently.",
            "Heat pan first, then add oil. Food should sizzle."
        ),
        "fold" to TechniqueInfo(
            "Gently combine by cutting down, across the bottom, and up the side.",
            "Use a rubber spatula in a J-motion to keep air."
        ),
        "blanch" to TechniqueInfo(
            "Briefly boil vegetables then plunge into ice water.",
            "Keep water at a rolling boil. Usually 1-3 minutes."
        ),
        "braise" to TechniqueInfo(
            "Sear food then cook slowly in liquid in a covered pot.",
            "Low and slow. Liquid halfway up the meat."
        ),
        "deglaze" to TechniqueInfo(
            "Add liquid to loosen browned bits from the pan.",
            "Remove from heat, add liquid, scrape with wooden spoon."
        ),
        "dice" to TechniqueInfo(
            "Cut food into small cubes, typically 1/4 to 1/2 inch.",
            "Slice, cut into strips, then cut strips into cubes."
        ),
        "julienne" to TechniqueInfo(
            "Cut into thin, matchstick-sized strips.",
            "About 2 inches long and 1/8 inch thick."
        ),
        "mince" to TechniqueInfo(
            "Cut into very small pieces, smaller than diced.",
            "Rock the knife back and forth until very fine."
        ),
        "simmer" to TechniqueInfo(
            "Cook just below boiling with small bubbles.",
            "If bubbling vigorously, turn down the heat."
        ),
        "whisk" to TechniqueInfo(
            "Beat ingredients to incorporate air and blend.",
            "Circular motions, lift slightly to add air."
        ),
        "cream" to TechniqueInfo(
            "Beat butter and sugar until light and fluffy.",
            "Room temp butter. Beat 3-5 minutes until pale."
        ),
        "reduce" to TechniqueInfo(
            "Boil a liquid to evaporate water and concentrate flavor.",
            "Use a wide pan. Don't cover."
        ),
        "sear" to TechniqueInfo(
            "Cook quickly at high heat to create a browned crust.",
            "Pat meat dry. Don't move until it releases."
        )
    )
}

data class Substitution(
    val name: String,
    val ratio: String,
    val notes: String
)

data class TechniqueInfo(
    val explanation: String,
    val tips: String
)
