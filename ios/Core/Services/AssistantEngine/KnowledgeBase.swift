import Foundation

// MARK: - Knowledge Base

/// Local knowledge base for ingredient substitutions and cooking techniques
struct KnowledgeBase {

    // MARK: - Substitutions

    func getSubstitutions(for ingredient: String) -> [Substitution]? {
        let key = ingredient.lowercased()

        // Check exact match
        if let subs = substitutions[key] {
            return subs
        }

        // Check partial match
        for (ingredientKey, subs) in substitutions {
            if ingredientKey.contains(key) || key.contains(ingredientKey) {
                return subs
            }
        }

        return nil
    }

    // MARK: - Techniques

    func getTechnique(_ name: String) -> TechniqueInfo? {
        let key = name.lowercased()

        // Check exact match
        if let info = techniques[key] {
            return info
        }

        // Check partial match
        for (techniqueKey, info) in techniques {
            if techniqueKey.contains(key) || key.contains(techniqueKey) {
                return info
            }
        }

        return nil
    }

    // MARK: - Data

    private let substitutions: [String: [Substitution]] = [
        "eggs": [
            Substitution(name: "applesauce", ratio: "1/4 cup per egg", notes: "Best for baking, adds moisture"),
            Substitution(name: "flax egg", ratio: "1 tbsp ground flax + 3 tbsp water", notes: "Vegan, let sit 5 min"),
            Substitution(name: "mashed banana", ratio: "1/4 cup per egg", notes: "Adds sweetness"),
            Substitution(name: "chia egg", ratio: "1 tbsp chia + 3 tbsp water", notes: "Vegan, let sit 5 min")
        ],
        "butter": [
            Substitution(name: "coconut oil", ratio: "1:1", notes: "Solid at room temp"),
            Substitution(name: "olive oil", ratio: "3/4 of butter amount", notes: "Good for savory"),
            Substitution(name: "applesauce", ratio: "1/2 of butter amount", notes: "For baking, reduces fat"),
            Substitution(name: "Greek yogurt", ratio: "1/2 of butter amount", notes: "Adds moisture and protein")
        ],
        "milk": [
            Substitution(name: "oat milk", ratio: "1:1", notes: "Creamy, neutral flavor"),
            Substitution(name: "almond milk", ratio: "1:1", notes: "Thinner, slight nutty flavor"),
            Substitution(name: "coconut milk", ratio: "1:1", notes: "Richer, coconut flavor"),
            Substitution(name: "water + butter", ratio: "1 cup = 1 cup water + 1 tbsp butter", notes: "In a pinch")
        ],
        "flour": [
            Substitution(name: "almond flour", ratio: "1:1 (may need more)", notes: "Gluten-free, denser"),
            Substitution(name: "oat flour", ratio: "1 1/4 cup per 1 cup flour", notes: "Blend oats"),
            Substitution(name: "coconut flour", ratio: "1/4 cup per 1 cup flour", notes: "Very absorbent")
        ],
        "sugar": [
            Substitution(name: "honey", ratio: "3/4 cup per 1 cup sugar", notes: "Reduce other liquids"),
            Substitution(name: "maple syrup", ratio: "3/4 cup per 1 cup sugar", notes: "Adds maple flavor"),
            Substitution(name: "stevia", ratio: "1 tsp per 1 cup sugar", notes: "Very concentrated")
        ],
        "sour cream": [
            Substitution(name: "Greek yogurt", ratio: "1:1", notes: "Lower fat, similar texture"),
            Substitution(name: "cottage cheese (blended)", ratio: "1:1", notes: "Blend until smooth")
        ],
        "heavy cream": [
            Substitution(name: "coconut cream", ratio: "1:1", notes: "Vegan, coconut flavor"),
            Substitution(name: "milk + butter", ratio: "3/4 cup milk + 1/4 cup melted butter", notes: "Won't whip")
        ],
        "garlic": [
            Substitution(name: "garlic powder", ratio: "1/8 tsp per clove", notes: "Less pungent")
        ],
        "onion": [
            Substitution(name: "onion powder", ratio: "1 tbsp per medium onion", notes: "For flavor only"),
            Substitution(name: "shallots", ratio: "3 shallots per onion", notes: "Milder, more delicate")
        ],
        "lemon juice": [
            Substitution(name: "lime juice", ratio: "1:1", notes: "Similar acidity"),
            Substitution(name: "vinegar", ratio: "1/2 amount", notes: "More acidic, use less")
        ],
        "baking powder": [
            Substitution(name: "baking soda + cream of tartar", ratio: "1/4 tsp soda + 1/2 tsp cream of tartar per 1 tsp", notes: "Mix fresh")
        ],
        "chocolate chips": [
            Substitution(name: "chopped chocolate bar", ratio: "1:1", notes: "Any chocolate works"),
            Substitution(name: "cacao nibs", ratio: "1:1", notes: "Less sweet, more bitter")
        ],
        "vanilla extract": [
            Substitution(name: "maple syrup", ratio: "1:1", notes: "Different flavor but works"),
            Substitution(name: "almond extract", ratio: "1/2 amount", notes: "Stronger flavor")
        ]
    ]

    private let techniques: [String: TechniqueInfo] = [
        "sauté": TechniqueInfo(
            explanation: "Cook quickly in a small amount of fat over medium-high heat, stirring frequently.",
            tips: "Heat pan first, then add oil. Food should sizzle when it hits the pan."
        ),
        "fold": TechniqueInfo(
            explanation: "Gently combine ingredients by cutting down through the mixture, across the bottom, and up the side.",
            tips: "Use a rubber spatula in a J-motion to keep air in the batter."
        ),
        "blanch": TechniqueInfo(
            explanation: "Briefly boil vegetables then plunge into ice water to stop cooking.",
            tips: "Keep water at a rolling boil. Usually 1-3 minutes depending on vegetable."
        ),
        "braise": TechniqueInfo(
            explanation: "Sear food then cook slowly in liquid in a covered pot.",
            tips: "Low and slow is key. Liquid should come about halfway up the meat."
        ),
        "deglaze": TechniqueInfo(
            explanation: "Add liquid to a hot pan to loosen browned bits stuck to the bottom.",
            tips: "Remove pan from heat briefly, add liquid, then scrape with wooden spoon."
        ),
        "dice": TechniqueInfo(
            explanation: "Cut food into small cubes, typically 1/4 to 1/2 inch.",
            tips: "First slice, then cut into strips, then cut strips into cubes."
        ),
        "julienne": TechniqueInfo(
            explanation: "Cut food into thin, matchstick-sized strips.",
            tips: "Aim for strips about 2 inches long and 1/8 inch thick."
        ),
        "mince": TechniqueInfo(
            explanation: "Cut food into very small pieces, smaller than diced.",
            tips: "Rock the knife back and forth over the food until very fine."
        ),
        "simmer": TechniqueInfo(
            explanation: "Cook liquid just below boiling, with small bubbles breaking at the surface.",
            tips: "If it's bubbling vigorously, turn down the heat."
        ),
        "whisk": TechniqueInfo(
            explanation: "Beat ingredients together to incorporate air and blend evenly.",
            tips: "Use circular motions and lift the whisk slightly to add air."
        ),
        "cream": TechniqueInfo(
            explanation: "Beat butter and sugar together until light and fluffy.",
            tips: "Use room temperature butter. Beat 3-5 minutes until pale and fluffy."
        ),
        "proof": TechniqueInfo(
            explanation: "Let yeast dough rise in a warm place until doubled in size.",
            tips: "Cover with a damp towel. A warm oven (turned off) works well."
        ),
        "temper": TechniqueInfo(
            explanation: "Gradually raise the temperature of a cold ingredient by slowly adding hot liquid.",
            tips: "Add hot liquid to eggs slowly while whisking constantly to avoid scrambling."
        ),
        "reduce": TechniqueInfo(
            explanation: "Boil a liquid to evaporate water and concentrate flavor.",
            tips: "Use a wide pan for faster reduction. Don't cover."
        ),
        "rest": TechniqueInfo(
            explanation: "Let cooked meat sit before cutting to redistribute juices.",
            tips: "Rest for about 5-10 minutes per inch of thickness."
        ),
        "sear": TechniqueInfo(
            explanation: "Cook meat quickly at high heat to create a browned crust.",
            tips: "Pat meat dry first. Don't move it until it releases easily from the pan."
        ),
        "poach": TechniqueInfo(
            explanation: "Cook gently in barely simmering liquid.",
            tips: "Water should be 160-180°F with just a few small bubbles."
        )
    ]
}

// MARK: - Supporting Types

struct Substitution {
    let name: String
    let ratio: String
    let notes: String
}

struct TechniqueInfo {
    let explanation: String
    let tips: String
}
