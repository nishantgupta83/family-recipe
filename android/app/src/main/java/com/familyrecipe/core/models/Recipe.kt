package com.familyrecipe.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import java.util.UUID

// MARK: - Recipe Model

@Entity(tableName = "recipes")
@TypeConverters(RecipeConverters::class)
data class Recipe(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val recipeDescription: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<Instruction> = emptyList(),
    val category: RecipeCategory = RecipeCategory.DINNER,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val prepTimeMinutes: Int = 15,
    val cookTimeMinutes: Int = 30,
    val servings: Int = 4,
    val familyId: String,
    val createdById: String,
    val familyMemory: String? = null,
    val favoritedBy: List<String> = emptyList(),
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
    val scannedSource: ScannedSource? = null
) {
    val totalTimeMinutes: Int
        get() = prepTimeMinutes + cookTimeMinutes
}

// MARK: - Scanned Source (OCR Import Data)

data class ScannedSource(
    val images: List<String> = emptyList(),           // URLs to original scanned images (preserved forever)
    val rawOCRText: String,                           // Raw extracted text before parsing
    val importedAt: Date = Date(),
    val confidenceScore: Double? = null,              // Overall OCR confidence (0-1)
    val lowConfidenceRanges: List<TextRange> = emptyList()  // Ranges with low OCR confidence
)

data class TextRange(
    val id: String = UUID.randomUUID().toString(),
    val start: Int,
    val end: Int,
    val confidence: Double
) {
    val isLowConfidence: Boolean
        get() = confidence < 0.7
}

// MARK: - Ingredient

data class Ingredient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val unit: MeasurementUnit,
    val notes: String? = null,
    val isOptional: Boolean = false
) {
    val displayString: String
        get() {
            val amountStr = if (amount % 1.0 == 0.0) {
                amount.toInt().toString()
            } else {
                String.format("%.1f", amount)
            }

            val unitStr = if (unit == MeasurementUnit.TO_TASTE) "" else " ${unit.abbreviation}"
            val notesStr = notes?.let { " ($it)" } ?: ""
            val optionalStr = if (isOptional) " (optional)" else ""

            return "$amountStr$unitStr $name$notesStr$optionalStr"
        }
}

// MARK: - Instruction

data class Instruction(
    val id: String = UUID.randomUUID().toString(),
    val stepNumber: Int,
    val text: String,
    val durationSeconds: Int? = null,
    val imageUrl: String? = null
) {
    val hasTiming: Boolean
        get() = durationSeconds != null && durationSeconds > 0

    val formattedDuration: String?
        get() {
            val seconds = durationSeconds ?: return null
            if (seconds <= 0) return null

            return when {
                seconds < 60 -> "$seconds seconds"
                seconds < 3600 -> {
                    val minutes = seconds / 60
                    "$minutes minute${if (minutes == 1) "" else "s"}"
                }
                else -> {
                    val hours = seconds / 3600
                    val minutes = (seconds % 3600) / 60
                    if (minutes == 0) {
                        "$hours hour${if (hours == 1) "" else "s"}"
                    } else {
                        "$hours hour${if (hours == 1) "" else "s"} $minutes min"
                    }
                }
            }
        }
}

// MARK: - Enums

enum class RecipeCategory(val displayName: String, val icon: String) {
    BREAKFAST("Breakfast", "sunrise"),
    BRUNCH("Brunch", "sun_and_horizon"),
    LUNCH("Lunch", "leaf"),
    DINNER("Dinner", "moon_stars"),
    APPETIZER("Appetizer", "fork_knife"),
    SNACK("Snack", "carrot"),
    DESSERT("Dessert", "birthday_cake"),
    BEVERAGE("Beverage", "cup_and_saucer"),
    SIDE("Side", "leaf_circle")
}

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Challenging")
}

enum class MeasurementUnit(val abbreviation: String) {
    // Volume
    CUP("cup"),
    TABLESPOON("tbsp"),
    TEASPOON("tsp"),
    FLUID_OUNCE("fl oz"),
    MILLILITER("ml"),
    LITER("L"),

    // Weight
    GRAM("g"),
    KILOGRAM("kg"),
    OUNCE("oz"),
    POUND("lb"),

    // Count
    PIECE("pc"),
    SLICE("slice"),
    CLOVE("clove"),
    PINCH("pinch"),
    DASH("dash"),
    TO_TASTE("to taste")
}

enum class SyncStatus {
    LOCAL,
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DOWNLOAD,
    CONFLICT
}

// MARK: - Room Type Converters

class RecipeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromIngredientList(value: List<Ingredient>): String = gson.toJson(value)

    @TypeConverter
    fun toIngredientList(value: String): List<Ingredient> {
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromInstructionList(value: List<Instruction>): String = gson.toJson(value)

    @TypeConverter
    fun toInstructionList(value: String): List<Instruction> {
        val type = object : TypeToken<List<Instruction>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromRecipeCategory(value: RecipeCategory): String = value.name

    @TypeConverter
    fun toRecipeCategory(value: String): RecipeCategory = RecipeCategory.valueOf(value)

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = Difficulty.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    @TypeConverter
    fun fromDate(value: Date): Long = value.time

    @TypeConverter
    fun toDate(value: Long): Date = Date(value)

    @TypeConverter
    fun fromScannedSource(value: ScannedSource?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toScannedSource(value: String?): ScannedSource? = value?.let {
        gson.fromJson(it, ScannedSource::class.java)
    }

    @TypeConverter
    fun fromTextRangeList(value: List<TextRange>): String = gson.toJson(value)

    @TypeConverter
    fun toTextRangeList(value: String): List<TextRange> {
        val type = object : TypeToken<List<TextRange>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}

// MARK: - Sample Data

object RecipeSamples {
    fun sampleRecipes(familyId: String, createdById: String): List<Recipe> = listOf(
        Recipe(
            title = "Grandma's Chocolate Chip Cookies",
            recipeDescription = "The most delicious chocolate chip cookies - a family favorite for generations!",
            ingredients = listOf(
                Ingredient(name = "all-purpose flour", amount = 2.0, unit = MeasurementUnit.CUP),
                Ingredient(name = "butter, softened", amount = 1.0, unit = MeasurementUnit.CUP),
                Ingredient(name = "granulated sugar", amount = 0.75, unit = MeasurementUnit.CUP),
                Ingredient(name = "brown sugar", amount = 0.75, unit = MeasurementUnit.CUP),
                Ingredient(name = "eggs", amount = 2.0, unit = MeasurementUnit.PIECE),
                Ingredient(name = "vanilla extract", amount = 1.0, unit = MeasurementUnit.TEASPOON),
                Ingredient(name = "baking soda", amount = 1.0, unit = MeasurementUnit.TEASPOON),
                Ingredient(name = "salt", amount = 1.0, unit = MeasurementUnit.TEASPOON),
                Ingredient(name = "chocolate chips", amount = 2.0, unit = MeasurementUnit.CUP)
            ),
            instructions = listOf(
                Instruction(stepNumber = 1, text = "Preheat oven to 375°F (190°C)."),
                Instruction(stepNumber = 2, text = "Mix flour, baking soda, and salt in a bowl. Set aside."),
                Instruction(stepNumber = 3, text = "Cream butter and both sugars until light and fluffy, about 3 minutes."),
                Instruction(stepNumber = 4, text = "Beat in eggs one at a time, then add vanilla."),
                Instruction(stepNumber = 5, text = "Gradually mix in flour mixture until just combined."),
                Instruction(stepNumber = 6, text = "Stir in chocolate chips."),
                Instruction(stepNumber = 7, text = "Drop rounded tablespoons onto ungreased baking sheets."),
                Instruction(stepNumber = 8, text = "Bake 9-11 minutes until golden brown.", durationSeconds = 600),
                Instruction(stepNumber = 9, text = "Cool on baking sheet for 2 minutes, then transfer to wire rack.")
            ),
            category = RecipeCategory.DESSERT,
            difficulty = Difficulty.EASY,
            prepTimeMinutes = 15,
            cookTimeMinutes = 10,
            servings = 24,
            familyId = familyId,
            createdById = createdById,
            familyMemory = "Grandma used to make these every Sunday. The secret is using room temperature butter!"
        ),
        Recipe(
            title = "Mom's Classic Pasta",
            recipeDescription = "Simple, comforting pasta with garlic and olive oil.",
            ingredients = listOf(
                Ingredient(name = "spaghetti", amount = 1.0, unit = MeasurementUnit.POUND),
                Ingredient(name = "olive oil", amount = 0.25, unit = MeasurementUnit.CUP),
                Ingredient(name = "garlic cloves, minced", amount = 4.0, unit = MeasurementUnit.CLOVE),
                Ingredient(name = "red pepper flakes", amount = 0.5, unit = MeasurementUnit.TEASPOON, isOptional = true),
                Ingredient(name = "fresh parsley, chopped", amount = 0.25, unit = MeasurementUnit.CUP),
                Ingredient(name = "parmesan cheese", amount = 0.5, unit = MeasurementUnit.CUP),
                Ingredient(name = "salt", amount = 1.0, unit = MeasurementUnit.PINCH, notes = "to taste")
            ),
            instructions = listOf(
                Instruction(stepNumber = 1, text = "Bring a large pot of salted water to boil."),
                Instruction(stepNumber = 2, text = "Cook pasta according to package directions. Reserve 1 cup pasta water before draining."),
                Instruction(stepNumber = 3, text = "While pasta cooks, heat olive oil in a large skillet over medium heat."),
                Instruction(stepNumber = 4, text = "Add garlic and red pepper flakes. Cook until fragrant, about 1 minute."),
                Instruction(stepNumber = 5, text = "Add drained pasta to skillet. Toss with oil and garlic."),
                Instruction(stepNumber = 6, text = "Add pasta water as needed to create a light sauce."),
                Instruction(stepNumber = 7, text = "Remove from heat. Add parsley and parmesan. Toss and serve.")
            ),
            category = RecipeCategory.DINNER,
            difficulty = Difficulty.EASY,
            prepTimeMinutes = 5,
            cookTimeMinutes = 15,
            servings = 4,
            familyId = familyId,
            createdById = createdById,
            familyMemory = "Mom made this on busy weeknights. We'd fight over who got the crispy garlic bits!"
        )
    )
}
