package app.tinygiants.getalife.di

import app.tinygiants.getalife.data.local.GetALifeDatabase
import app.tinygiants.getalife.data.local.dao.AccountDao
import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.dao.CategoryMonthlyStatusDao
import app.tinygiants.getalife.data.local.dao.GroupDao
import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.remote.FirestoreDataSource
import app.tinygiants.getalife.data.repository.AccountRepositoryImpl
import app.tinygiants.getalife.data.repository.CategoryMonthlyStatusRepositoryImpl
import app.tinygiants.getalife.data.repository.CategoryRepositoryImpl
import app.tinygiants.getalife.data.repository.CrispChatRepository
import app.tinygiants.getalife.data.repository.FirebaseRemoteConfigRepository
import app.tinygiants.getalife.data.repository.GoogleInAppReviewRepository
import app.tinygiants.getalife.data.repository.GroupRepositoryImpl
import app.tinygiants.getalife.data.repository.RevenueCatRepository
import app.tinygiants.getalife.data.repository.TransactionRepositoryImpl
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.InAppReviewRepository
import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import app.tinygiants.getalife.domain.repository.SubscriptionRepository
import app.tinygiants.getalife.domain.repository.SupportChatRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.GetCurrentBudgetUseCase
import app.tinygiants.getalife.domain.usecase.FeatureFlagUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideGroupRepository(
        groupDao: GroupDao,
        getCurrentBudget: GetCurrentBudgetUseCase,
        categoryRepository: CategoryRepository,
        firestore: FirestoreDataSource,
        @ApplicationScope externalScope: CoroutineScope
    ): GroupRepository = GroupRepositoryImpl(groupDao, getCurrentBudget, categoryRepository, firestore, externalScope)

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        getCurrentBudget: GetCurrentBudgetUseCase,
        firestore: FirestoreDataSource,
        @ApplicationScope externalScope: CoroutineScope
    ): CategoryRepository = CategoryRepositoryImpl(categoryDao, getCurrentBudget, firestore, externalScope)

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        getCurrentBudget: GetCurrentBudgetUseCase,
        firestore: FirestoreDataSource,
        @ApplicationScope externalScope: CoroutineScope
    ): AccountRepository = AccountRepositoryImpl(accountDao, getCurrentBudget, firestore, externalScope)

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        accountRepository: AccountRepository,
        categoryRepository: CategoryRepository,
        getCurrentBudget: GetCurrentBudgetUseCase,
        firestore: FirestoreDataSource,
        @ApplicationScope externalScope: CoroutineScope
    ): TransactionRepository = TransactionRepositoryImpl(
        transactionDao,
        accountRepository,
        categoryRepository,
        getCurrentBudget,
        firestore,
        externalScope
    )

    @Provides
    @Singleton
    fun provideSubscriptionRepository(revenueCatRepository: RevenueCatRepository): SubscriptionRepository = revenueCatRepository

    @Provides
    @Singleton
    fun provideRemoteConfigRepository(firebaseRemoteConfigRepository: FirebaseRemoteConfigRepository): RemoteConfigRepository =
        firebaseRemoteConfigRepository

    @Provides
    @Singleton
    fun provideInAppReviewRepository(googleInAppReviewRepository: GoogleInAppReviewRepository): InAppReviewRepository =
        googleInAppReviewRepository

    @Provides
    @Singleton
    fun provideSupportChatRepository(crispChatRepository: CrispChatRepository): SupportChatRepository = crispChatRepository

    @Provides
    @Singleton
    fun provideCategoryMonthlyStatusRepository(
        categoryMonthlyStatusDao: CategoryMonthlyStatusDao,
        categoryRepository: CategoryRepository,
        getCurrentBudget: GetCurrentBudgetUseCase
    ): CategoryMonthlyStatusRepository =
        CategoryMonthlyStatusRepositoryImpl(categoryMonthlyStatusDao, categoryRepository, getCurrentBudget)
}