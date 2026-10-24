package com.rekora.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rekora.app.ui.theme.*
import com.rekora.app.ui.screens.splash.SplashScreen
import com.rekora.app.ui.screens.onboarding.OnboardingScreen
import com.rekora.app.ui.screens.auth.LoginScreen
import com.rekora.app.ui.screens.auth.RegisterScreen
import com.rekora.app.ui.screens.home.HomeScreen
import com.rekora.app.ui.screens.explore.ExploreScreen
import com.rekora.app.ui.screens.ai.AiEngineerScreen
import com.rekora.app.ui.screens.workspace.WorkspaceScreen
import com.rekora.app.ui.screens.profile.ProfileScreen
import com.rekora.app.ui.screens.project.ProjectDetailScreen
import com.rekora.app.ui.screens.project.ProjectMatcherScreen
import com.rekora.app.ui.screens.components_catalog.ComponentCatalogScreen
import com.rekora.app.ui.screens.components_catalog.ComponentDetailScreen
import com.rekora.app.ui.screens.cart.CartScreen
import com.rekora.app.ui.screens.cart.CheckoutScreen
import com.rekora.app.ui.screens.fabrication.FabricationSubmitScreen
import com.rekora.app.ui.screens.marketplace.MarketplaceScreen
import com.rekora.app.ui.screens.ai.AiConversationScreen
import com.rekora.app.ui.screens.notifications.NotificationsScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = RekoraRoute.Home.route,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = RekoraRoute.Explore.route,
        label = "Explore",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    ),
    BottomNavItem(
        route = RekoraRoute.AiEngineer.route,
        label = "AI Engineer",
        selectedIcon = Icons.Filled.SmartToy,
        unselectedIcon = Icons.Outlined.SmartToy
    ),
    BottomNavItem(
        route = RekoraRoute.Workspace.route,
        label = "Workspace",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    BottomNavItem(
        route = RekoraRoute.Profile.route,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

@Composable
fun RekoraNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if bottom bar should be shown
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                RekoraBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RekoraRoute.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { RekoraMotion.screenEnter },
            exitTransition = { RekoraMotion.screenExit },
            popEnterTransition = { RekoraMotion.screenPopEnter },
            popExitTransition = { RekoraMotion.screenPopExit }
        ) {
            // Splash
            composable(RekoraRoute.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(RekoraRoute.Onboarding.route) {
                            popUpTo(RekoraRoute.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(RekoraRoute.Home.route) {
                            popUpTo(RekoraRoute.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding
            composable(RekoraRoute.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToLogin = {
                        navController.navigate(RekoraRoute.Login.route)
                    },
                    onNavigateToHome = {
                        navController.navigate(RekoraRoute.Home.route) {
                            popUpTo(RekoraRoute.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth
            composable(RekoraRoute.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(RekoraRoute.Register.route)
                    },
                    onNavigateToHome = {
                        navController.navigate(RekoraRoute.Home.route) {
                            popUpTo(RekoraRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(RekoraRoute.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate(RekoraRoute.Home.route) {
                            popUpTo(RekoraRoute.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Main tabs
            composable(RekoraRoute.Home.route) {
                HomeScreen(
                    onNavigateToProject = { id ->
                        navController.navigate(RekoraRoute.ProjectDetail.createRoute(id))
                    },
                    onNavigateToExplore = {
                        navController.navigate(RekoraRoute.Explore.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAiEngineer = {
                        navController.navigate(RekoraRoute.AiEngineer.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProjectMatcher = {
                        navController.navigate(RekoraRoute.ProjectMatcher.route)
                    },
                    onNavigateToComponents = {
                        navController.navigate(RekoraRoute.ComponentCatalog.route)
                    },
                    onNavigateToFabrication = {
                        navController.navigate(RekoraRoute.FabricationSubmit.route)
                    },
                    onNavigateToMarketplace = {
                        navController.navigate(RekoraRoute.Marketplace.route)
                    },
                    onNavigateToCart = {
                        navController.navigate(RekoraRoute.Cart.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(RekoraRoute.Notifications.route)
                    }
                )
            }

            composable(RekoraRoute.Explore.route) {
                ExploreScreen(
                    onNavigateToProject = { id ->
                        navController.navigate(RekoraRoute.ProjectDetail.createRoute(id))
                    },
                    onNavigateToProjectMatcher = {
                        navController.navigate(RekoraRoute.ProjectMatcher.route)
                    },
                    onNavigateToCart = {
                        navController.navigate(RekoraRoute.Cart.route)
                    }
                )
            }

            composable(RekoraRoute.AiEngineer.route) {
                AiEngineerScreen(
                    onNavigateToConversation = { id ->
                        navController.navigate(RekoraRoute.AiConversation.createRoute(id))
                    },
                    onNavigateToNewConversation = {
                        navController.navigate(RekoraRoute.AiNewConversation.route)
                    }
                )
            }

            composable(RekoraRoute.Workspace.route) {
                WorkspaceScreen(
                    onNavigateToProject = { id ->
                        navController.navigate(RekoraRoute.ProjectDetail.createRoute(id))
                    },
                    onNavigateToOrder = { id ->
                        navController.navigate(RekoraRoute.OrderDetail.createRoute(id))
                    },
                    onNavigateToRental = { id ->
                        navController.navigate(RekoraRoute.RentalDetail.createRoute(id))
                    },
                    onNavigateToFabricationJob = { id ->
                        navController.navigate(RekoraRoute.FabricationJobDetail.createRoute(id))
                    }
                )
            }

            composable(RekoraRoute.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(RekoraRoute.Settings.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(RekoraRoute.Notifications.route)
                    },
                    onNavigateToLogin = {
                        navController.navigate(RekoraRoute.Login.route) {
                            popUpTo(RekoraRoute.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            // Project detail
            composable(
                route = RekoraRoute.ProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
                ProjectDetailScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCart = { navController.navigate(RekoraRoute.Cart.route) },
                    onNavigateToComponent = { id ->
                        navController.navigate(RekoraRoute.ComponentDetail.createRoute(id))
                    }
                )
            }

            // Project Matcher
            composable(RekoraRoute.ProjectMatcher.route) {
                ProjectMatcherScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProject = { id ->
                        navController.navigate(RekoraRoute.ProjectDetail.createRoute(id))
                    }
                )
            }

            // Components
            composable(RekoraRoute.ComponentCatalog.route) {
                ComponentCatalogScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToComponent = { id ->
                        navController.navigate(RekoraRoute.ComponentDetail.createRoute(id))
                    },
                    onNavigateToCart = { navController.navigate(RekoraRoute.Cart.route) }
                )
            }

            composable(
                route = RekoraRoute.ComponentDetail.route,
                arguments = listOf(navArgument("componentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val componentId = backStackEntry.arguments?.getString("componentId") ?: return@composable
                ComponentDetailScreen(
                    componentId = componentId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCart = { navController.navigate(RekoraRoute.Cart.route) }
                )
            }

            // Cart & Checkout
            composable(RekoraRoute.Cart.route) {
                CartScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheckout = { navController.navigate(RekoraRoute.Checkout.route) },
                    onNavigateToComponent = { id ->
                        navController.navigate(RekoraRoute.ComponentDetail.createRoute(id))
                    }
                )
            }

            composable(RekoraRoute.Checkout.route) {
                CheckoutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOrderComplete = { orderId ->
                        navController.navigate(RekoraRoute.OrderConfirmation.createRoute(orderId)) {
                            popUpTo(RekoraRoute.Cart.route) { inclusive = true }
                        }
                    }
                )
            }

            // Fabrication
            composable(RekoraRoute.FabricationSubmit.route) {
                FabricationSubmitScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Marketplace
            composable(RekoraRoute.Marketplace.route) {
                MarketplaceScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToListing = { id ->
                        navController.navigate(RekoraRoute.MarketplaceListing.createRoute(id))
                    }
                )
            }

            // AI Conversation
            composable(
                route = RekoraRoute.AiConversation.route,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
                AiConversationScreen(
                    conversationId = conversationId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(RekoraRoute.AiNewConversation.route) {
                AiConversationScreen(
                    conversationId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Notifications
            composable(RekoraRoute.Notifications.route) {
                NotificationsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun RekoraBottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 0.75.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .bouncyClickable(targetScale = 0.94f) {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.5.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
