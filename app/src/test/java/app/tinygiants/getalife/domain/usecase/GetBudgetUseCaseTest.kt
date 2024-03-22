package app.tinygiants.getalife.domain.usecase

import app.tinygiants.getalife.data.repository.CategoryRepositoryImpl
import app.tinygiants.getalife.data.local.dao.HeaderDao
import org.junit.jupiter.api.BeforeEach

class GetBudgetUseCaseTest {

    private lateinit var useCase: GetBudgetUseCase
    private lateinit var repository: CategoryRepositoryImpl

    private lateinit var headerDao: HeaderDao

    @BeforeEach
    fun setUp() {
        headerDao = object : HeaderDao()

        repository = CategoryRepositoryImpl(headerDao)
        useCase = GetBudgetUseCase()
    }
}