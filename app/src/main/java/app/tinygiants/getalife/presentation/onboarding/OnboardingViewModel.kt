package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.tinygiants.getalife.domain.model.onbaordinganswers.BeautifulLife
import app.tinygiants.getalife.domain.model.onbaordinganswers.DailyCost
import app.tinygiants.getalife.domain.model.onbaordinganswers.Debt
import app.tinygiants.getalife.domain.model.onbaordinganswers.EmergencySaving
import app.tinygiants.getalife.domain.model.onbaordinganswers.GoodStuff
import app.tinygiants.getalife.domain.model.onbaordinganswers.HowYouKnowUs
import app.tinygiants.getalife.domain.model.onbaordinganswers.OnboardingAnswers
import app.tinygiants.getalife.domain.model.onbaordinganswers.Subscription
import app.tinygiants.getalife.domain.model.onbaordinganswers.Transportation
import app.tinygiants.getalife.domain.model.onbaordinganswers.UnexpectedExpense

class OnboardingViewModel : ViewModel() {

    private var _onboardingAnswers by mutableStateOf(
        OnboardingAnswers(
            name = "",
            howYouKnowUs = HowYouKnowUs.Unknown,
            transportations = emptyList(),
            debts = emptyList(),
            dailyCosts = emptyList(),
            subscriptions = emptyList(),
            unexpectedExpenses = emptyList(),
            emergencySavings = emptyList(),
            beautifulLife = emptyList(),
            goodStuff = emptyList()
        )
    )

    fun addName(newName: String) {
        _onboardingAnswers = _onboardingAnswers.copy(name = newName)
    }

    fun addHowYouKnowUs(howYouKnowUs: HowYouKnowUs) {
        _onboardingAnswers = _onboardingAnswers.copy(howYouKnowUs = howYouKnowUs)
    }

    fun addTransportations(transportations: List<Transportation>) {
        _onboardingAnswers = _onboardingAnswers.copy(transportations = transportations)
    }

    fun addDebts(debts: List<Debt>) {
        _onboardingAnswers = _onboardingAnswers.copy(debts = debts)
    }

    fun addDailyCosts(dailyCosts: List<DailyCost>) {
        _onboardingAnswers = _onboardingAnswers.copy(dailyCosts = dailyCosts)
    }

    fun addSubscriptions(subscriptions: List<Subscription>) {
        _onboardingAnswers = _onboardingAnswers.copy(subscriptions = subscriptions)
    }

    fun addUnexpectedExpenses(unexpectedExpense: List<UnexpectedExpense>) {
        _onboardingAnswers = _onboardingAnswers.copy(unexpectedExpenses = unexpectedExpense)
    }

    fun addEmergencySavings(emergencySavings: List<EmergencySaving>) {
        _onboardingAnswers = _onboardingAnswers.copy(emergencySavings = emergencySavings)
    }

    fun addBeautifulLife(beautifulLife: List<BeautifulLife>) {
        _onboardingAnswers = _onboardingAnswers.copy(beautifulLife = beautifulLife)
    }

    fun addGoodStuff(goodStuff: List<GoodStuff>) {
        _onboardingAnswers = _onboardingAnswers.copy(goodStuff = goodStuff)
    }

}