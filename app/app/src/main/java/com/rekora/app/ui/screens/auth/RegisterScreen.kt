package com.rekora.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rekora.app.ui.components.RekoraPrimaryButton
import com.rekora.app.ui.theme.RekoraColors
import com.rekora.app.ui.theme.RekoraCorners
import com.rekora.app.ui.theme.RekoraDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var collegeName by remember { mutableStateOf("St. Joseph's College of Engineering") }

    val departments = listOf(
        "ECE - Electronics & Comm.",
        "CSE - Computer Science",
        "MECH - Mechanical Eng.",
        "EEE - Electrical & Electronics",
        "IT - Information Tech.",
        "AIDS - AI & Data Science",
        "BME - Biomedical Eng."
    )
    var selectedDepartment by remember { mutableStateOf(departments[0]) }
    var departmentExpanded by remember { mutableStateOf(false) }

    val roles = listOf("Student", "Contributor", "Faculty")
    var selectedRole by remember { mutableStateOf(roles[0]) }

    var termsAccepted by remember { mutableStateOf(false) }

    // Validation flags
    val isEmailValid = email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= 6
    val doPasswordsMatch = password == confirmPassword && password.isNotEmpty()
    val canSubmit = fullName.isNotBlank() && isEmailValid && isPasswordValid && doPasswordsMatch && termsAccepted

    Scaffold(
        containerColor = RekoraColors.LightBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RekoraColors.MidnightNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = RekoraDimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)
        ) {
            // Header Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs)
            ) {
                Text(
                    text = "Join the REKORA Network",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RekoraColors.MidnightNavy
                    )
                )
                Text(
                    text = "Connect with makers, source verified hardware, and log persistent project blueprints.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RekoraColors.SecondaryText
                    )
                )
            }

            // Error alert if any
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { errorText ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RekoraCorners.CardSmall,
                        color = RekoraColors.ErrorLight
                    ) {
                        Row(
                            modifier = Modifier.padding(RekoraDimens.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = RekoraColors.Error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorText,
                                style = MaterialTheme.typography.bodySmall,
                                color = RekoraColors.Error,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearError() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = RekoraColors.Error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Registration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RekoraCorners.CardLarge,
                colors = CardDefaults.cardColors(
                    containerColor = RekoraColors.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(RekoraDimens.SpacingXxl),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingLg)
                ) {
                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Arun Kumar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = RekoraColors.SecondaryText
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RekoraCorners.Button
                    )

                    // College Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("College Email Address") },
                        placeholder = { Text("arun.k@stjosephs.edu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = RekoraColors.SecondaryText
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RekoraCorners.Button
                    )

                    // College Name (Pre-filled)
                    OutlinedTextField(
                        value = collegeName,
                        onValueChange = { collegeName = it },
                        label = { Text("College / University") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                tint = RekoraColors.SecondaryText
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RekoraCorners.Button
                    )

                    // Department Dropdown
                    ExposedDropdownMenuBox(
                        expanded = departmentExpanded,
                        onExpandedChange = { departmentExpanded = !departmentExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedDepartment,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Department") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Engineering,
                                    contentDescription = null,
                                    tint = RekoraColors.SecondaryText
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RekoraCorners.Button
                        )

                        ExposedDropdownMenu(
                            expanded = departmentExpanded,
                            onDismissRequest = { departmentExpanded = false }
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept) },
                                    onClick = {
                                        selectedDepartment = dept
                                        departmentExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Role Selector
                    Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)) {
                        Text(
                            text = "Primary Role",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = RekoraColors.PrimaryText
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            roles.forEach { role ->
                                val isSelected = selectedRole == role
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedRole = role },
                                    shape = RekoraCorners.CardSmall,
                                    color = if (isSelected) RekoraColors.MidnightNavy else RekoraColors.LightBackground,
                                    shadowElevation = if (isSelected) 2.dp else 0.dp
                                ) {
                                    Text(
                                        text = role,
                                        modifier = Modifier.padding(vertical = RekoraDimens.SpacingSm),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else RekoraColors.PrimaryText,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("Password (min 6 chars)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = RekoraColors.SecondaryText
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = RekoraColors.SecondaryText
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RekoraCorners.Button
                    )

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LockClock,
                                contentDescription = null,
                                tint = RekoraColors.SecondaryText
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = RekoraColors.SecondaryText
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (canSubmit) {
                                    viewModel.register(
                                        fullName = fullName,
                                        email = email,
                                        pass = password,
                                        confirmPass = confirmPassword,
                                        college = collegeName,
                                        department = selectedDepartment.substringBefore(" -"),
                                        role = selectedRole.uppercase(),
                                        acceptedTerms = termsAccepted,
                                        onSuccess = onNavigateToHome
                                    )
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RekoraCorners.Button
                    )

                    // Academic Integrity & Terms Agreement Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAccepted = !termsAccepted },
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RekoraColors.EngineeringTeal
                            )
                        )
                        Text(
                            text = "I agree to the REKORA Terms of Service and commit to the Academic Integrity Code for engineering blueprints and campus fabrication.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RekoraColors.SecondaryText,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Register Button
                    RekoraPrimaryButton(
                        text = "Create Account",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.register(
                                fullName = fullName,
                                email = email,
                                pass = password,
                                confirmPass = confirmPassword,
                                college = collegeName,
                                department = selectedDepartment.substringBefore(" -"),
                                role = selectedRole.uppercase(),
                                acceptedTerms = termsAccepted,
                                onSuccess = onNavigateToHome
                            )
                        },
                        isLoading = isLoading,
                        enabled = canSubmit
                    )
                }
            }

            // Already have account link
            Row(
                modifier = Modifier.padding(vertical = RekoraDimens.SpacingMd),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an engineering account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RekoraColors.SecondaryText
                )
                Text(
                    text = "Sign In",
                    modifier = Modifier.clickable(onClick = onNavigateToLogin),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = RekoraColors.EngineeringTeal
                )
            }

            Spacer(modifier = Modifier.height(RekoraDimens.SpacingLg))
        }
    }
}
