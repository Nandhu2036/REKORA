package com.rekora.app.ui.navigation

/**
 * Defines all navigation routes for the REKORA application.
 * Using sealed class hierarchy for type-safe navigation.
 */
sealed class RekoraRoute(val route: String) {
    // Splash & Onboarding
    data object Splash : RekoraRoute("splash")
    data object Onboarding : RekoraRoute("onboarding")
    
    // Auth
    data object Login : RekoraRoute("auth/login")
    data object Register : RekoraRoute("auth/register")
    data object ForgotPassword : RekoraRoute("auth/forgot_password")
    
    // Main tabs
    data object Home : RekoraRoute("main/home")
    data object Explore : RekoraRoute("main/explore")
    data object AiEngineer : RekoraRoute("main/ai_engineer")
    data object Workspace : RekoraRoute("main/workspace")
    data object Profile : RekoraRoute("main/profile")
    
    // Project
    data object ProjectDetail : RekoraRoute("project/{projectId}") {
        fun createRoute(projectId: String) = "project/$projectId"
    }
    data object ProjectMatcher : RekoraRoute("project/matcher")
    data object ProjectRepository : RekoraRoute("project/{projectId}/repository") {
        fun createRoute(projectId: String) = "project/$projectId/repository"
    }
    data object ProjectDna : RekoraRoute("project/{projectId}/dna") {
        fun createRoute(projectId: String) = "project/$projectId/dna"
    }
    
    // AI Engineer
    data object AiConversation : RekoraRoute("ai/conversation/{conversationId}") {
        fun createRoute(conversationId: String) = "ai/conversation/$conversationId"
    }
    data object AiNewConversation : RekoraRoute("ai/conversation/new")
    data object Blueprint : RekoraRoute("blueprint/{blueprintId}") {
        fun createRoute(blueprintId: String) = "blueprint/$blueprintId"
    }
    
    // Components & Commerce
    data object ComponentCatalog : RekoraRoute("components")
    data object ComponentDetail : RekoraRoute("component/{componentId}") {
        fun createRoute(componentId: String) = "component/$componentId"
    }
    data object Cart : RekoraRoute("cart")
    data object Checkout : RekoraRoute("checkout")
    data object OrderConfirmation : RekoraRoute("order/{orderId}/confirmation") {
        fun createRoute(orderId: String) = "order/$orderId/confirmation"
    }
    data object OrderDetail : RekoraRoute("order/{orderId}") {
        fun createRoute(orderId: String) = "order/$orderId"
    }
    data object OrderHistory : RekoraRoute("orders")
    
    // Rentals
    data object RentalDetail : RekoraRoute("rental/{rentalId}") {
        fun createRoute(rentalId: String) = "rental/$rentalId"
    }
    data object RentalHistory : RekoraRoute("rentals")
    
    // Fabrication
    data object FabricationSubmit : RekoraRoute("fabrication/submit")
    data object FabricationJobDetail : RekoraRoute("fabrication/{jobId}") {
        fun createRoute(jobId: String) = "fabrication/$jobId"
    }
    data object FabricationHistory : RekoraRoute("fabrication/history")
    
    // Marketplace
    data object Marketplace : RekoraRoute("marketplace")
    data object MarketplaceListing : RekoraRoute("marketplace/{listingId}") {
        fun createRoute(listingId: String) = "marketplace/$listingId"
    }
    data object CreateListing : RekoraRoute("marketplace/create")
    
    // Documentation
    data object DocumentationWorkspace : RekoraRoute("documentation/{projectId}") {
        fun createRoute(projectId: String) = "documentation/$projectId"
    }
    
    // QR Scanner
    data object QrScanner : RekoraRoute("qr/scanner")
    data object QrItemDetail : RekoraRoute("qr/item/{itemCode}") {
        fun createRoute(itemCode: String) = "qr/item/$itemCode"
    }
    
    // Profile sub-screens
    data object EditProfile : RekoraRoute("profile/edit")
    data object Settings : RekoraRoute("profile/settings")
    data object Notifications : RekoraRoute("notifications")
    data object HelpCenter : RekoraRoute("help")
    data object PrivacyPolicy : RekoraRoute("privacy")
    data object TermsOfService : RekoraRoute("terms")
    
    // Admin
    data object AdminDashboard : RekoraRoute("admin")
    data object AdminUsers : RekoraRoute("admin/users")
    data object AdminProjects : RekoraRoute("admin/projects")
    data object AdminInventory : RekoraRoute("admin/inventory")
    data object AdminOrders : RekoraRoute("admin/orders")
    data object AdminFabrication : RekoraRoute("admin/fabrication")
    data object AdminMarketplace : RekoraRoute("admin/marketplace")
}

/**
 * Bottom navigation destinations
 */
enum class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: String
) {
    HOME(RekoraRoute.Home.route, "Home", "home"),
    EXPLORE(RekoraRoute.Explore.route, "Explore", "explore"),
    AI_ENGINEER(RekoraRoute.AiEngineer.route, "AI Engineer", "smart_toy"),
    WORKSPACE(RekoraRoute.Workspace.route, "Workspace", "dashboard"),
    PROFILE(RekoraRoute.Profile.route, "Profile", "person")
}
