package app.tinygiants.getalife.di

import app.tinygiants.getalife.data.repository.AccountsRepositoryImpl
import app.tinygiants.getalife.data.repository.CategoryRepositoryImpl
import app.tinygiants.getalife.data.repository.FirebaseRemoteConfigRepository
import app.tinygiants.getalife.data.repository.GroupRepositoryImpl
import app.tinygiants.getalife.data.repository.InAppReviewRepositoryImpl
import app.tinygiants.getalife.data.repository.RevenueCatRepository
import app.tinygiants.getalife.data.repository.TransactionRepositoryImpl
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.InAppReviewRepository
import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import app.tinygiants.getalife.domain.repository.SubscriptionRepository
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

    @Binds
    abstract fun provideSubscriptionRepository(revenueCatRepository: RevenueCatRepository): SubscriptionRepository

    @Binds
    abstract fun provideRemoteConfigRepository(firebaseRemoteConfigRepository: FirebaseRemoteConfigRepository): RemoteConfigRepository

    @Binds
    abstract fun provideInAppReviewRepository(inAppReviewRepositoryImpl: InAppReviewRepositoryImpl): InAppReviewRepository

    // endregion
}