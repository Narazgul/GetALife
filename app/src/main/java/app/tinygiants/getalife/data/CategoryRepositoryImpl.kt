package app.tinygiants.getalife.data

import app.tinygiants.getalife.data.fake.CategoryFakeDataSource
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryHeader
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val fakeDataSource: CategoryFakeDataSource
) : CategoryRepository {

    override fun fetchCategories(): Flow<Map<CategoryHeader, List<Category>>> = fakeDataSource.fetchFakeCategories()

    override suspend fun toggleIsExpended(categoryId: Int) = fakeDataSource.toggleIsExpended(categoryId)
}