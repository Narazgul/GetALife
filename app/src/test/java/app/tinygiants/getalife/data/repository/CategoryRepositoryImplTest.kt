package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.CategoryDaoFake
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CategoryRepositoryImplTest {

    private lateinit var repository: CategoryRepositoryImpl
    private lateinit var fakeDao: CategoryDaoFake

    @BeforeEach
    fun setUp() {
        fakeDao = CategoryDaoFake()
        repository = CategoryRepositoryImpl(fakeDao)
    }

    @Test
    fun `Test CategoriesFlow`(): Unit = runTest {

    }

}