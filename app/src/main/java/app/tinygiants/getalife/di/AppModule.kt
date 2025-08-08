package app.tinygiants.getalife.di

import android.content.Context
import app.tinygiants.getalife.BuildConfig
import app.tinygiants.getalife.R
import app.tinygiants.getalife.data.local.GetALifeDatabase
import app.tinygiants.getalife.data.remote.FirestoreDataSource
import app.tinygiants.getalife.data.remote.ai.ChatGptAi
import app.tinygiants.getalife.data.remote.ai.FirebaseAi
import app.tinygiants.getalife.data.security.DataEncryption
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.usecase.BudgetSelectionUseCase
import app.tinygiants.getalife.domain.usecase.GetCurrentBudgetUseCase
import com.aallam.openai.client.OpenAI
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideAppUpdateManager(@ApplicationContext appContext: Context) = AppUpdateManagerFactory.create(appContext)

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    @Singleton
    fun provideDataEncryption(): DataEncryption = DataEncryption()

    @Provides
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        return remoteConfig
    }

    @ChatGPT
    @Provides
    fun provideChatGPT(): AiRepository = ChatGptAi(openAi = OpenAI(BuildConfig.CHATGPT_API_KEY))

    @FirebaseGemini
    @Provides
    fun provideFirebaseAi(
        crashlytics: FirebaseCrashlytics,
        dataEncryption: DataEncryption
    ): AiRepository = FirebaseAi(
        generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel("gemini-1.5-flash"),
        crashlytics = crashlytics,
        dataEncryption = dataEncryption
    )

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
    fun providePurchases(): Purchases = Purchases.sharedInstance

    @Provides
    @Singleton
    fun provideSuperwall(): Superwall = Superwall.instance
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChatGPT

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FirebaseGemini

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope