package com.rekora.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import com.rekora.app.ui.components.RekoraSecondaryButton
import com.rekora.app.ui.components.RekoraTealButton
import com.rekora.app.ui.theme.RekoraColors
import com.rekora.app.ui.theme.RekoraCorners
import com.rekora.app.ui.theme.RekoraDimens
import com.rekora.app.util.AuthorizedAccessCodes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Dialog States
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSubmitted by remember { mutableStateOf(false) }

    var showTesterAccessDialog by remember { mutableStateOf(false) }
    var testerCode by remember { mutableStateOf("TESTER-2026-X7") }
    var testerEmail by remember { mutableStateOf("qa.specialist@rekora.internal") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
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
            // Header with Brand Symbol
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Memory,
                    contentDescription = "REKORA",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingXs)
            ) {
                Text(
                    text = "Welcome to REKORA",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Sign in to manage blueprints, rentals, and fabrication",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Error alert if present
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { errorText ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RekoraCorners.CardSmall,
                        color = MaterialTheme.colorScheme.errorContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(RekoraDimens.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = errorText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Credentials Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(RekoraDimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)
                ) {
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("College Email") },
                        placeholder = { Text("engineer@stjosephs.edu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("At least 6 characters") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login(email, password, onSuccess = onNavigateToHome)
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RekoraCorners.Button
                    )

                    // Forgot Password link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot password?",
                            modifier = Modifier.clickable {
                                resetEmail = email
                                resetSubmitted = false
                                showForgotPasswordDialog = true
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Sign In Button
                    RekoraPrimaryButton(
                        text = "Sign In",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login(email, password, onSuccess = onNavigateToHome)
                        },
                        isLoading = isLoading,
                        enabled = email.isNotBlank() && password.length >= 6
                    )
                }
            }

            // Dedicated Tester / Authorized Access Callout
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RekoraCorners.Card,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(RekoraDimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "AUTHORIZED TESTER PORTAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = "Access evaluation environments, instructor dashboard, and lab inventory tools via verified security token.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RekoraTealButton(
                        text = "Tester / Authorized Access",
                        icon = Icons.Outlined.Key,
                        onClick = { showTesterAccessDialog = true }
                    )
                }
            }

            // Quick Pilot Accounts (One-tap login)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "  PILOT TEST PROFILES  ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    PREDEFINED_ACCOUNTS.take(2).forEach { account ->
                        QuickAccountChip(
                            account = account,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                email = account.email
                                password = "password123"
                                viewModel.loginWithPredefined(account, onSuccess = onNavigateToHome)
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                ) {
                    PREDEFINED_ACCOUNTS.drop(2).take(2).forEach { account ->
                        QuickAccountChip(
                            account = account,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                email = account.email
                                password = "password123"
                                viewModel.loginWithPredefined(account, onSuccess = onNavigateToHome)
                            }
                        )
                    }
                }
            }

            // Continue as Guest Option
            RekoraSecondaryButton(
                text = "Continue as Guest",
                icon = Icons.Outlined.PersonOutline,
                onClick = {
                    viewModel.continueAsGuest(onSuccess = onNavigateToHome)
                }
            )

            // Register redirect
            Row(
                modifier = Modifier.padding(vertical = RekoraDimens.SpacingMd),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an engineering account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Register",
                    modifier = Modifier.clickable(onClick = onNavigateToRegister),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(RekoraDimens.SpacingLg))
        }
    }

    // Tester / Authorized Access Dialog
    if (showTesterAccessDialog) {
        AlertDialog(
            onDismissRequest = { showTesterAccessDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Tester / Authorized Access",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Enter a verified invitation code to sign in with authorized role privileges.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = testerCode,
                        onValueChange = { testerCode = it },
                        label = { Text("Invitation Code") },
                        placeholder = { Text("TESTER-2026-X7") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = testerEmail,
                        onValueChange = { testerEmail = it },
                        label = { Text("Official Contact Email (Optional)") },
                        placeholder = { Text("qa.specialist@rekora.internal") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "TAP CODE TO AUTO-FILL:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    AuthorizedAccessCodes.VALID_CODES.forEach { (code, grant) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    testerCode = code
                                    testerEmail = "${grant.role.name.lowercase()}@rekora.internal"
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = code,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = grant.role.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = "${grant.defaultName} • ${grant.organization}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loginWithTesterCode(testerCode, testerEmail) {
                            showTesterAccessDialog = false
                            onNavigateToHome()
                        }
                    },
                    enabled = testerCode.isNotBlank()
                ) {
                    Text("Authenticate & Enter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTesterAccessDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingMd)) {
                    if (!resetSubmitted) {
                        Text(
                            text = "Enter your verified college email address to receive password reset instructions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("College Email") },
                            placeholder = { Text("engineer@stjosephs.edu") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = RekoraColors.Success
                            )
                            Text(
                                text = "Instructions sent! Please check your institutional inbox.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RekoraColors.Success
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (!resetSubmitted) {
                    Button(
                        onClick = {
                            viewModel.resetPassword(resetEmail) { success ->
                                if (success) {
                                    resetSubmitted = true
                                }
                            }
                        },
                        enabled = resetEmail.contains("@"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Send Link")
                    }
                } else {
                    Button(
                        onClick = { showForgotPasswordDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                if (!resetSubmitted) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun QuickAccountChip(
    account: QuickAccount,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RekoraCorners.CardSmall,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RekoraDimens.SpacingMd,
                vertical = RekoraDimens.SpacingSm
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RekoraDimens.SpacingSm)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.label.first().toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${account.label} • ${account.department}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
