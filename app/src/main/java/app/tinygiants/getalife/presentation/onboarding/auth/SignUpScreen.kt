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
fun SignUpScreen(
    onBackClicked: () -> Unit,
    onSignUpSuccess: () -> Unit,
    onLoginClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when {
            uiState.signUpSuccess -> onSignUpSuccess()
            uiState.errorMessage != null -> {
                snackbarHostState.showSnackbar(uiState.errorMessage!!)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrieren") },
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
                    text = "Konto erstellen",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Registriere dich und starte deine finanzielle Reise",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.m))

                // Social Sign-Up Buttons
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
                            text = "Schnell registrieren mit:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Google Button
                        OutlinedButton(
                            onClick = {
                                // TODO: Implement Google Sign-Up
                                scope.launch {
                                    snackbarHostState.showSnackbar("Google Sign-Up wird implementiert...")
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
                            Text("Mit Google registrieren")
                        }

                        // Facebook Button
                        Button(
                            onClick = {
                                // TODO: Implement Facebook Sign-Up
                                scope.launch {
                                    snackbarHostState.showSnackbar("Facebook Sign-Up wird implementiert...")
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
                            Text("Mit Facebook registrieren")
                        }

                        // Twitter Button  
                        Button(
                            onClick = {
                                // TODO: Implement Twitter Sign-Up
                                scope.launch {
                                    snackbarHostState.showSnackbar("Twitter Sign-Up wird implementiert...")
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
                            Text("Mit Twitter registrieren")
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

                // Email Sign up Form
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
                        // Name Input
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = viewModel::onNameChanged,
                            label = { Text("Name") },
                            enabled = !uiState.isLoading,
                            isError = uiState.nameError != null,
                            supportingText = uiState.nameError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

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

                        // Confirm Password Input
                        var confirmPasswordVisible by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChanged,
                            label = { Text("Passwort bestätigen") },
                            visualTransformation = if (confirmPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !uiState.isLoading,
                            isError = uiState.confirmPasswordError != null,
                            supportingText = uiState.confirmPasswordError?.let { { Text(it) } },
                            trailingIcon = {
                                TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Text(
                                        text = if (confirmPasswordVisible) "Verbergen" else "Anzeigen",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.s))

                        // Sign Up Button
                        Button(
                            onClick = viewModel::signUpWithEmail,
                            enabled = !uiState.isLoading &&
                                    uiState.name.isNotBlank() &&
                                    uiState.email.isNotBlank() &&
                                    uiState.password.isNotBlank() &&
                                    uiState.confirmPassword.isNotBlank(),
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
                                Text("Mit E-Mail registrieren")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.m))

                // Login link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bereits ein Konto? ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(
                        onClick = onLoginClicked,
                        enabled = !uiState.isLoading
                    ) {
                        Text("Anmelden")
                    }
                }

                Spacer(modifier = Modifier.height(spacing.m))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreen(
            onBackClicked = { },
            onSignUpSuccess = { },
            onLoginClicked = { }
        )
    }
}