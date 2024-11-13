package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Progress

class CalculateCategoryProgressUseCase {

    operator fun invoke(categoryEntity: CategoryEntity): Progress {
        return EmptyProgress()
    }
}