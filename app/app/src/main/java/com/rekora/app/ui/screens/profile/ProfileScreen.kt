package com.rekora.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.datastore.ThemeMode
import com.rekora.app.data.local.SeedDataProvider
import com.rekora.app.data.local.dao.*
import com.rekora.app.domain.model.User
import com.rekora.app.domain.repository.AuthRepository
import com.rekora.app.ui.components.*
import com.rekora.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val prefs: RekoraPreferences,
    private val projectDao: ProjectDao,
    private val componentDao: ComponentDao,
    private val bomItemDao: BomItemDao,
    private val inventoryItemDao: InventoryItemDao
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName: StateFlow<String> = prefs.userName
        .map { it.ifBlank { "NandhaKumar G" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "NandhaKumar G")

    val userEmail: StateFlow<String> = prefs.userEmail
        .map { it.ifBlank { "nandha.g@stjosephs.ac.in" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "nandha.g@stjosephs.ac.in")

    val userRole: StateFlow<String> = prefs.userRole
        .map { it.ifBlank { "STUDENT" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "STUDENT")

    val themeMode: StateFlow<ThemeMode> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM_DEFAULT)

    val useDynamicColor: StateFlow<Boolean> = prefs.useDynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _statusToast = MutableStateFlow<String?>(null)
    val statusToast = _statusToast.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
        }
    }

    fun setDynamicColor(dynamic: Boolean) {
        viewModelScope.launch {
            prefs.setUseDynamicColor(dynamic)
        }
    }

    fun updateProfile(name: String, college: String, department: String, bio: String) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                val updated = user.copy(
                    displayName = name.trim(),
                    college = college.trim(),
                    department = department.trim(),
                    bio = bio.trim()
                )
                authRepository.updateProfile(updated)
                _statusToast.value = "Profile updated successfully!"
            } else {
                prefs.setLoggedIn(
                    userId = prefs.userId.first() ?: "usr-local",
                    userName = name.trim(),
                    email = prefs.userEmail.first(),
                    role = prefs.userRole.first()
                )
                _statusToast.value = "Preferences saved!"
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            projectDao.deleteAll()
            projectDao.insertProjects(SeedDataProvider.getSeedProjects())

            componentDao.deleteAll()
            componentDao.insertComponents(SeedDataProvider.getSeedComponents())

            bomItemDao.insertBomItems(SeedDataProvider.getSeedBomItems())
            inventoryItemDao.insertItems(SeedDataProvider.getSeedInventoryItems())

            _statusToast.value = "Demo database reset and re-seeded successfully!"
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            authRepository.deleteAccount().fold(
                onSuccess = {
                    _statusToast.value = "Account and data deleted."
                    onDeleted()
                },
                onFailure = { error ->
                    _statusToast.value = error.localizedMessage ?: "Failed to delete account."
                }
            )
        }
    }

    fun clearToast() {
        _statusToast.value = null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.useDynamicColor.collectAsState()
    val statusToast by viewModel.statusToast.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editCollege by remember { mutableStateOf("") }
    var editDepartment by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }

    var showCharterDialog by remember { mutableStateOf(false) }
    var showRentalAgreementDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val skills = listOf(
        "Embedded C++",
        "FreeRTOS",
        "Computer Vision",
        "ESP32-S3",
        "3D Printing (PETG)",
        "LoRaWAN",
        "TinyML",
        "KiCAD"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RekoraTopBar(
                title = "Engineering Profile",
                onNotificationClick = onNavigateToNotifications
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card (Adaptive surface)
            Surface(
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Monogram avatar with halo border
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            EditorialEyebrow(text = "RESEARCHER IDENTITY")
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            LuxuryStatusCapsule(
                                label = userRole,
                                dotColor = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }

                        IconButton(
                            onClick = {
                                editName = currentUser?.displayName ?: userName
                                editCollege = currentUser?.college ?: "St. Joseph's College of Engineering"
                                editDepartment = currentUser?.department ?: "Dept of ECE"
                                editBio = currentUser?.bio ?: "Hardware & Embedded systems researcher on REKORA."
                                showEditProfileDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    // Affiliation & Lab Clearance Badge
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = currentUser?.college ?: "St. Joseph's College of Engineering",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${currentUser?.department ?: "Dept of ECE"} • Verified Campus Identity",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Metric Counters
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatItem(title = "Projects", value = "6 Active")
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            ProfileStatItem(title = "Rentals", value = "2 Live")
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            ProfileStatItem(title = "Repositories", value = "4 Unlocked")
                        }
                    }
                }
            }

            // Technical Competencies
            Surface(
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditorialEyebrow(text = "TECHNICAL COMPETENCIES & TOOLING")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skills.forEach { skill ->
                            Surface(
                                shape = RekoraCorners.Pill,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = skill,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Theme Preference Toggle (Centralized Material 3 & True AMOLED)
            Surface(
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EditorialEyebrow(text = "INTERFACE THEME & MATERIAL YOU")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            label = "System",
                            icon = Icons.Outlined.BrightnessAuto,
                            selected = currentThemeMode == ThemeMode.SYSTEM_DEFAULT,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM_DEFAULT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "Light",
                            icon = Icons.Outlined.LightMode,
                            selected = currentThemeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "AMOLED",
                            icon = Icons.Outlined.DarkMode,
                            selected = currentThemeMode == ThemeMode.DARK_AMOLED,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK_AMOLED) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    // Dynamic Color Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Material You Dynamic Color",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Harmonize accent highlights with device wallpaper (pure AMOLED black canvas preserved)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = dynamicColor,
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }
                }
            }

            // Academic Integrity & Ethics Charter Card
            Surface(
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        EditorialEyebrow(text = "VERIFIED COLLEGIATE RESEARCHER", color = MaterialTheme.colorScheme.secondary)
                    }
                    Text(
                        text = "Your student account is authenticated with the REKORA Campus Engineering Testbed. All repository checkouts and lab rental returns comply with IEEE ethics standards.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // Settings & Shortcuts
            Surface(
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 1.dp
            ) {
                Column {
                    SettingShortcutItem(
                        icon = Icons.Outlined.Gavel,
                        title = "Academic Integrity Charter",
                        subtitle = "View open research code & hardware attribution pledge",
                        onClick = { showCharterDialog = true }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    SettingShortcutItem(
                        icon = Icons.Outlined.Assignment,
                        title = "Campus Equipment Rental Agreement",
                        subtitle = "Review lab deposit policies and return checklists",
                        onClick = { showRentalAgreementDialog = true }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    SettingShortcutItem(
                        icon = Icons.Outlined.RestartAlt,
                        title = "Demo Database Reset",
                        subtitle = "Re-seed sample projects, BOMs, and QR inventory",
                        onClick = { viewModel.resetDemoData() }
                    )
                }
            }

            // Session Logout & Account Deletion Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logout Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyClickable(targetScale = 0.98f) { showLogoutDialog = true },
                    shape = RekoraCorners.Button,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out of Session",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Delete Account Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bouncyClickable(targetScale = 0.98f) { showDeleteAccountDialog = true },
                    shape = RekoraCorners.Button,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete Account & Clear Local Data",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }

            if (statusToast != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearToast() }) {
                            Text("OK", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                ) {
                    Text(statusToast ?: "")
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Engineering Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCollege,
                        onValueChange = { editCollege = it },
                        label = { Text("College / University") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDepartment,
                        onValueChange = { editDepartment = it },
                        label = { Text("Department / Lab") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Engineering Bio") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editName, editCollege, editDepartment, editBio)
                        showEditProfileDialog = false
                    },
                    enabled = editName.isNotBlank()
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Confirm Sign Out",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to end your current session? You can sign back in at any time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onNavigateToLogin)
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Stay Signed In")
                }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    text = "Permanently Delete Account?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "This action is irreversible. All your orders, equipment rentals, 3D fabrication submissions, and saved blueprints will be erased from local storage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount(onNavigateToLogin)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Charter Dialog
    if (showCharterDialog) {
        AlertDialog(
            onDismissRequest = { showCharterDialog = false },
            title = {
                Text(
                    text = "Academic Integrity Charter",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "As a REKORA student researcher:\n\n" +
                        "1. I pledge that all hardware schematics, firmware modifications, and test metrics represent authentic work.\n" +
                        "2. Open-source repositories borrowed from fellow campus teams will be duly cited.\n" +
                        "3. All rented equipment will be handled with appropriate ESD and safety precautions.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp)
                )
            },
            confirmButton = {
                TextButton(onClick = { showCharterDialog = false }) {
                    Text("I Acknowledge", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Rental Agreement Dialog
    if (showRentalAgreementDialog) {
        AlertDialog(
            onDismissRequest = { showRentalAgreementDialog = false },
            title = {
                Text(
                    text = "Equipment Rental Agreement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Campus Lab Equipment Policy:\n\n" +
                        "• Rentals are issued for up to 14 days with one renewal option.\n" +
                        "• Security deposits are held in escrow and released instantly upon QR scan return inspection.\n" +
                        "• Report damaged pins or blown fuses immediately to the Lab Operator.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp)
                )
            },
            confirmButton = {
                TextButton(onClick = { showRentalAgreementDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EditorialEyebrow(text = title.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            0.75.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .bouncyClickable(targetScale = 0.96f) { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingShortcutItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
