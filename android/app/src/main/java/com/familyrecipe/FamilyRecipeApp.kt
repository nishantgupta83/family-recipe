package com.familyrecipe

import android.app.Application
import com.familyrecipe.core.database.AppDatabase
import com.familyrecipe.core.models.FamilySamples
import com.familyrecipe.core.models.RecipeSamples
import com.familyrecipe.core.repositories.FamilyMemberRepository
import com.familyrecipe.core.repositories.FamilyRepository
import com.familyrecipe.core.repositories.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FamilyRecipeApp : Application() {

    // Application-scoped coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Database
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    // Repositories
    val recipeRepository: RecipeRepository by lazy {
        RecipeRepository(database.recipeDao())
    }

    val familyRepository: FamilyRepository by lazy {
        FamilyRepository(database.familyDao())
    }

    val familyMemberRepository: FamilyMemberRepository by lazy {
        FamilyMemberRepository(database.familyMemberDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize sample data on first launch
        applicationScope.launch {
            initializeSampleDataIfNeeded()
        }
    }

    private suspend fun initializeSampleDataIfNeeded() {
        val familyCount = familyRepository.getFamilyCount()
        if (familyCount > 0) return

        // Create sample family and member
        val (sampleFamily, sampleMember) = FamilySamples.sampleFamily()

        familyRepository.createFamily(sampleFamily)
        familyMemberRepository.createMember(sampleMember)

        // Create sample recipes
        val sampleRecipes = RecipeSamples.sampleRecipes(
            familyId = sampleFamily.id,
            createdById = sampleMember.id
        )

        recipeRepository.insertAll(sampleRecipes)
    }

    companion object {
        lateinit var instance: FamilyRecipeApp
            private set
    }
}
