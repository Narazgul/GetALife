package app.tinygiants.getalife.di

import app.tinygiants.getalife.data.repository.AccountsRepositoryImpl
import app.tinygiants.getalife.data.repository.BudgetRepositoryImpl
import app.tinygiants.getalife.data.repository.TransactionRepositoryImpl
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.BudgetRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideBudgetRepository(budgetRepositoryImpl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    abstract fun provideAccountRepository(accountsRepositoryImpl: AccountsRepositoryImpl): AccountRepository

    @Binds
    abstract fun provideTransactionRepository(transactionRepositoryImpl: TransactionRepositoryImpl): TransactionRepository
}