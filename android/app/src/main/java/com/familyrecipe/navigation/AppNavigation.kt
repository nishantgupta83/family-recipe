package com.familyrecipe.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.familyrecipe.features.addrecipe.AddRecipeScreen
import com.familyrecipe.features.family.FamilyScreen
import com.familyrecipe.features.home.HomeScreen
import com.familyrecipe.features.onboarding.OnboardingScreen
import com.familyrecipe.features.search.SearchScreen
import com.familyrecipe.features.settings.SettingsScreen

// MARK: - Navigation Routes

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Search : Screen("search")
    object AddRecipe : Screen("add_recipe")
    object Family : Screen("family")
    object Settings : Screen("settings")
    object RecipeDetail : Screen("recipe/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe/$recipeId"
    }
    object CookingMode : Screen("cooking/{recipeId}") {
        fun createRoute(recipeId: String) = "cooking/$recipeId"
    }
}

// MARK: - Bottom Navigation Items

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.Search, Icons.Default.Search, "Search"),
    BottomNavItem(Screen.AddRecipe, Icons.Default.Add, "Add"),
    BottomNavItem(Screen.Family, Icons.Default.Person, "Family"),
    BottomNavItem(Screen.Settings, Icons.Default.Settings, "Settings")
)

// MARK: - App Navigation

@Composable
fun AppNavigation(
    isOnboarded: Boolean,
    onCompleteOnboarding: (memberId: String, familyId: String) -> Unit
) {
    val navController = rememberNavController()

    val startDestination = if (isOnboarded) Screen.Home.route else Screen.Onboarding.route

    // Check if current route should show bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { memberId, familyId ->
                        onCompleteOnboarding(memberId, familyId)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    }
                )
            }

            composable(Screen.AddRecipe.route) {
                AddRecipeScreen(
                    onSave = {
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Family.route) {
                FamilyScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // TODO: Add RecipeDetail and CookingMode composables
        }
    }
}
