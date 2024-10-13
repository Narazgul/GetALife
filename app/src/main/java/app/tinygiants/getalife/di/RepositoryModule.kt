package app.tinygiants.getalife.di

import app.tinygiants.getalife.data.repository.AccountsRepositoryImpl
import app.tinygiants.getalife.data.repository.CategoryRepositoryImpl
import app.tinygiants.getalife.data.repository.GroupRepositoryImpl
import app.tinygiants.getalife.data.repository.TransactionRepositoryImpl
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideGroupRepository(groupRepositoryImpl: GroupRepositoryImpl): GroupRepository

    @Binds
    abstract fun provideCategoryRepository(categoryRepositoryImpl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun provideAccountRepository(accountsRepositoryImpl: AccountsRepositoryImpl): AccountRepository

    @Binds
    abstract fun provideTransactionRepository(transactionRepositoryImpl: TransactionRepositoryImpl): TransactionRepository
}