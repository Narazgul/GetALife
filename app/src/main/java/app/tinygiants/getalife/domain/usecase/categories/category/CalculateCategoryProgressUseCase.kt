package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Progress
import javax.inject.Inject

class CalculateCategoryProgressUseCase @Inject constructor() {

    operator fun invoke(categoryEntity: CategoryEntity): Progress {
        return EmptyProgress()
    }
}