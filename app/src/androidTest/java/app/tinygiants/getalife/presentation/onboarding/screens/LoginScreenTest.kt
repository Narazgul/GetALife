package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.tinygiants.getalife.theme.GetALifeTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private var backClickCount = 0
    private var loginSuccessCount = 0
    private var signUpClickCount = 0
    private var forgotPasswordClickCount = 0

    @Before
    fun setup() {
        hiltRule.inject()

        // Reset counters
        backClickCount = 0
        loginSuccessCount = 0
        signUpClickCount = 0
        forgotPasswordClickCount = 0
    }

    @Test
    fun loginScreen_displaysAllRequiredElements() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        // Check if all required elements are displayed
        composeTestRule.onNodeWithText("Anmelden").assertIsDisplayed()
        composeTestRule.onNodeWithText("Willkommen zurück!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Melde dich an, um deine Finanzen zu verwalten").assertIsDisplayed()

        // Social login buttons
        composeTestRule.onNodeWithText("Mit Google anmelden").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mit Facebook anmelden").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mit Twitter anmelden").assertIsDisplayed()

        // Form fields
        composeTestRule.onNodeWithText("E-Mail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Passwort").assertIsDisplayed()

        // Buttons and links
        composeTestRule.onNodeWithText("Mit E-Mail anmelden").assertIsDisplayed()
        composeTestRule.onNodeWithText("Passwort vergessen?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Noch kein Konto?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Registrieren").assertIsDisplayed()
    }

    @Test
    fun loginScreen_backButtonWorks() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        // Click back button
        composeTestRule.onNodeWithText("← Zurück").performClick()

        assert(backClickCount == 1)
    }

    @Test
    fun loginScreen_signUpButtonWorks() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        // Click sign up button
        composeTestRule.onNodeWithText("Registrieren").performClick()

        assert(signUpClickCount == 1)
    }

    @Test
    fun loginScreen_forgotPasswordButtonWorks() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        // Click forgot password button
        composeTestRule.onNodeWithText("Passwort vergessen?").performClick()

        assert(forgotPasswordClickCount == 1)
    }

    @Test
    fun loginScreen_emailInputWorks() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        val emailInput = "test@example.com"

        // Find email field and enter text
        composeTestRule.onNode(hasText("E-Mail")).performTextInput(emailInput)

        // Verify that text was entered
        composeTestRule.onNodeWithText(emailInput).assertIsDisplayed()
    }

    @Test
    fun loginScreen_passwordInputWorks() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        val passwordInput = "password123"

        // Find password field and enter text
        composeTestRule.onNode(hasText("Passwort")).performTextInput(passwordInput)

        // Password should be hidden, so we can't directly check the text
        // Instead, we check if the toggle button is working
        composeTestRule.onNodeWithText("Anzeigen").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun loginScreen_passwordVisibilityToggleWorks() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        val passwordInput = "password123"

        // Enter password
        composeTestRule.onNode(hasText("Passwort")).performTextInput(passwordInput)

        // Initially, password should be hidden and "Anzeigen" button visible
        composeTestRule.onNodeWithText("Anzeigen").assertIsDisplayed()

        // Click to show password
        composeTestRule.onNodeWithText("Anzeigen").performClick()

        // Now "Verbergen" button should be visible
        composeTestRule.onNodeWithText("Verbergen").assertIsDisplayed()

        // Click to hide password again
        composeTestRule.onNodeWithText("Verbergen").performClick()

        // "Anzeigen" button should be visible again
        composeTestRule.onNodeWithText("Anzeigen").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loginButtonStateChanges() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        val loginButton = composeTestRule.onNodeWithText("Mit E-Mail anmelden")

        // Initially, login button should be disabled (no email/password)
        loginButton.assertIsNotEnabled()

        // Enter email
        composeTestRule.onNode(hasText("E-Mail")).performTextInput("test@example.com")

        // Still disabled (no password)
        loginButton.assertIsNotEnabled()

        // Enter password
        composeTestRule.onNode(hasText("Passwort")).performTextInput("password123")

        // Now should be enabled
        loginButton.assertIsEnabled()
    }

    @Test
    fun loginScreen_socialLoginButtonsWork() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        // All social login buttons should be clickable
        composeTestRule.onNodeWithText("Mit Google anmelden").assertHasClickAction()
        composeTestRule.onNodeWithText("Mit Facebook anmelden").assertHasClickAction()
        composeTestRule.onNodeWithText("Mit Twitter anmelden").assertHasClickAction()

        // Click each button to ensure they don't crash
        composeTestRule.onNodeWithText("Mit Google anmelden").performClick()
        composeTestRule.onNodeWithText("Mit Facebook anmelden").performClick()
        composeTestRule.onNodeWithText("Mit Twitter anmelden").performClick()
    }

    @Test
    fun loginScreen_appLogoIsDisplayed() {
        composeTestRule.setContent {
            GetALifeTheme {
                LoginScreen(
                    onBackClicked = { backClickCount++ },
                    onLoginSuccess = { loginSuccessCount++ },
                    onSignUpClicked = { signUpClickCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickCount++ }
                )
            }
        }

        // App logo should be displayed
        composeTestRule.onNodeWithContentDescription("App Logo").assertIsDisplayed()
    }
}