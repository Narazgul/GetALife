package app.tinygiants.getalife.domain.usecase.header

import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class AddHeaderUseCase @Inject constructor(
    private val repository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(headerName: String) {

        val header = withContext(defaultDispatcher) {
            HeaderEntity(
                id = Random.nextLong(),
                name = headerName,
                isExpanded = true
            )
        }

        repository.addHeader(headerEntity = header)
    }
}