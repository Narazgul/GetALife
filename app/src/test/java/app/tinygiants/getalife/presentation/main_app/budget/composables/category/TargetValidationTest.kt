package app.tinygiants.getalife.presentation.main_app.budget.composables.category

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TargetValidationTest {

    @Test
    fun parseNonNegativeAmount_returnsNull_forInvalidOrNegative() {
        assertNull(TargetValidation.parseNonNegativeAmount(""))
        assertNull(TargetValidation.parseNonNegativeAmount("abc"))
        assertNull(TargetValidation.parseNonNegativeAmount("-1"))
    }

    @Test
    fun parseNonNegativeAmount_parsesValidNumbers() {
        assertEquals(0.0, TargetValidation.parseNonNegativeAmount("0.0")!!, 0.0001)
        assertEquals(12.5, TargetValidation.parseNonNegativeAmount("12,5")!!, 0.0001)
        assertEquals(99.99, TargetValidation.parseNonNegativeAmount("99.99")!!, 0.0001)
    }

    @Test
    fun parseDateOrNull_returnsNull_forInvalid() {
        assertNull(TargetValidation.parseDateOrNull(""))
        assertNull(TargetValidation.parseDateOrNull("2025-13-01"))
        assertNull(TargetValidation.parseDateOrNull("01-01-2025"))
    }

    @Test
    fun parseDateOrNull_parsesValidIsoDate() {
        val date = TargetValidation.parseDateOrNull("2025-12-31")
        assertEquals(LocalDate(2025, 12, 31), date)
    }
}
