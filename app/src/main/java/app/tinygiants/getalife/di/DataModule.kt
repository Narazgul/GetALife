package app.tinygiants.getalife.di

import android.content.Context
import app.tinygiants.getalife.data.local.GetALifeDatabase
import app.tinygiants.getalife.data.remote.FirestoreDataSource
import app.tinygiants.getalife.domain.usecase.BudgetSelectionUseCase
import app.tinygiants.getalife.domain.usecase.GetCurrentBudgetUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.revenuecat.purchases.Purchases
import com.superwall.sdk.Superwall
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideGetALifeDatabase(@ApplicationContext context: Context): GetALifeDatabase =
        GetALifeDatabase.getDatabase(context)

    @Provides
    fun provideBudgetDao(database: GetALifeDatabase) = database.budgetDao()

    @Provides
    fun provideAccountDao(database: GetALifeDatabase) = database.accountsDao()

    @Provides
    fun provideCategoryDao(database: GetALifeDatabase) = database.categoryDao()

    @Provides
    fun provideGroupDao(database: GetALifeDatabase) = database.groupDao()

    @Provides
    fun provideTransactionDao(database: GetALifeDatabase) = database.transactionDao()

    @Provides
    fun provideCategoryMonthlyStatusDao(database: GetALifeDatabase) = database.categoryMonthlyStatusDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirestoreDataSource(firestore: FirebaseFirestore): FirestoreDataSource =
        FirestoreDataSource(firestore)

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: app.tinygiants.getalife.data.local.dao.BudgetDao,
        firestoreDataSource: FirestoreDataSource,
        @ApplicationScope externalScope: CoroutineScope
    ): app.tinygiants.getalife.data.repository.BudgetRepository =
        app.tinygiants.getalife.data.repository.BudgetRepository(budgetDao, firestoreDataSource, externalScope)

    @Provides
    @Singleton
    fun provideBudgetSelectionUseCase(
        @ApplicationContext context: Context,
        budgetRepository: app.tinygiants.getalife.data.repository.BudgetRepository,
        firebaseAuth: FirebaseAuth
    ): BudgetSelectionUseCase = BudgetSelectionUseCase(context, budgetRepository, firebaseAuth)

    @Provides
    @Singleton
    fun provideGetCurrentBudgetUseCase(
        budgetSelectionUseCase: BudgetSelectionUseCase
    ): GetCurrentBudgetUseCase = GetCurrentBudgetUseCase(budgetSelectionUseCase)

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    @Singleton
    fun providePurchases(): Purchases = Purchases.sharedInstance

    @Provides
    @Singleton
    fun provideSuperwall(): Superwall = Superwall.instance
}