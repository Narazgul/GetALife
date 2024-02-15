package app.tinygiants.getalife.domain.usecase.header

import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateHeaderUseCase @Inject constructor(
    private val repository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(header: Header) {

        val headerEntity = withContext(defaultDispatcher) {
            HeaderEntity(
                id = header.id,
                name = header.name,
                isExpanded = header.isExpanded
            )
        }

        repository.updateHeader(headerEntity = headerEntity)
    }
}