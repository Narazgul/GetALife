package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import app.tinygiants.getalife.presentation.onboarding.OnboardingViewModel
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay

@Composable
fun PlanGenerationScreen(
    viewModel: OnboardingViewModel,
    onPlanGenerated: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.generateBudgetFromAnswers()
        delay(2500) // Simulate generation time for animation
        onPlanGenerated()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(spacing.l))
            Text(
                text = "Okay, verstanden! Wir erstellen deinen persönlichen Finanzplan.",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            // Here you could add dynamic text showing which categories are being created
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanGenerationScreenPreview() {
    MaterialTheme {
        // This preview won't show much without a real ViewModel
        PlanGenerationScreen(viewModel = hiltViewModel(), onPlanGenerated = {})
    }
}