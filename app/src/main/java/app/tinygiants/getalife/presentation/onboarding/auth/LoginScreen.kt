package app.tinygiants.getalife.presentation.onboarding.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.R
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClicked: () -> Unit,
    onLoginSuccess: () -> Unit,
    onSignUpClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when {
            uiState.loginSuccess -> onLoginSuccess()
            uiState.errorMessage != null -> {
                snackbarHostState.showSnackbar(uiState.errorMessage!!)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anmelden") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) {
                        Text("← Zurück")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.m),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                Spacer(modifier = Modifier.height(spacing.m))

                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_monochrome),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(100.dp)
                )

                Text(
                    text = "Willkommen zurück!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Melde dich an, um deine Finanzen zu verwalten",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.m))

                // Social Login Buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.m),
                        verticalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        Text(
                            text = "Schnell anmelden mit:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Google Button
                        OutlinedButton(
                            onClick = {
                                // TODO: Implement Google Sign-In
                                scope.launch {
                                    snackbarHostState.showSnackbar("Google Sign-In wird implementiert...")
                                }
                            },
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(spacing.m),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Mit Google anmelden")
                        }

                        // Facebook Button
                        Button(
                            onClick = {
                                // TODO: Implement Facebook Login
                                scope.launch {
                                    snackbarHostState.showSnackbar("Facebook Login wird implementiert...")
                                }
                            },
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(spacing.m),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1877F2),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Mit Facebook anmelden")
                        }

                        // Twitter Button  
                        Button(
                            onClick = {
                                // TODO: Implement Twitter Login
                                scope.launch {
                                    snackbarHostState.showSnackbar("Twitter Login wird implementiert...")
                                }
                            },
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(spacing.m),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1DA1F2),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Mit Twitter anmelden")
                        }
                    }
                }

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                    )
                    Text(
                        text = " oder ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                    )
                }

                // Email/Password Login Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.m),
                        verticalArrangement = Arrangement.spacedBy(spacing.m)
                    ) {
                        // Email Input
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChanged,
                            label = { Text("E-Mail") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !uiState.isLoading,
                            isError = uiState.emailError != null,
                            supportingText = uiState.emailError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Password Input
                        var passwordVisible by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChanged,
                            label = { Text("Passwort") },
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !uiState.isLoading,
                            isError = uiState.passwordError != null,
                            supportingText = uiState.passwordError?.let { { Text(it) } },
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(
                                        text = if (passwordVisible) "Verbergen" else "Anzeigen",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Forgot Password
                        TextButton(
                            onClick = onForgotPasswordClicked,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Passwort vergessen?")
                        }

                        // Login Button
                        Button(
                            onClick = viewModel::signInWithEmail,
                            enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
                            shape = RoundedCornerShape(spacing.m),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Mit E-Mail anmelden")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.m))

                // Sign up link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Noch kein Konto? ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(
                        onClick = onSignUpClicked,
                        enabled = !uiState.isLoading
                    ) {
                        Text("Registrieren")
                    }
                }

                Spacer(modifier = Modifier.height(spacing.m))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onBackClicked = { },
            onLoginSuccess = { },
            onSignUpClicked = { },
            onForgotPasswordClicked = { }
        )
    }
}