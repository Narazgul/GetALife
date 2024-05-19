package app.tinygiants.getalife.domain.usecase.budget.header

import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.repository.BudgetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteHeaderUseCase @Inject constructor(
    private val repository: BudgetRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(header: Header) {

        val headerEntity = withContext(defaultDispatcher) {
            HeaderEntity(
                id = header.id,
                name = header.name,
                listPosition = header.listPosition,
                isExpanded = header.isExpanded
            )
        }

        repository.deleteHeader(headerEntity = headerEntity)
    }
}