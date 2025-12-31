package com.familyrecipe.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.familyrecipe.core.models.Family
import com.familyrecipe.core.models.FamilyConverters
import com.familyrecipe.core.models.FamilyMember
import com.familyrecipe.core.models.FamilyMemberConverters
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.core.models.RecipeConverters

@Database(
    entities = [
        Recipe::class,
        Family::class,
        FamilyMember::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    RecipeConverters::class,
    FamilyConverters::class,
    FamilyMemberConverters::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun familyDao(): FamilyDao
    abstract fun familyMemberDao(): FamilyMemberDao

    companion object {
        private const val DATABASE_NAME = "family_recipe_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
