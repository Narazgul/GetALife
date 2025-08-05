package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.datetime.YearMonth
import javax.inject.Inject

/**
 * Sehr einfacher Fake, der lediglich die gleiche Konstruktor-Signatur besitzt
 * wie die echte UseCase-Klasse, aber keinerlei Logik ausführt. Dadurch können
 * Tests die Dependency übergeben, ohne dass die finale Originalklasse
 * erweitert werden muss.
 */
class RecalculateCategoryMonthlyStatusUseCaseFake @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long, yearMonth: YearMonth) {
        // absichtlich leer – im Testkontext reicht eine No-Op-Implementierung
    }
}