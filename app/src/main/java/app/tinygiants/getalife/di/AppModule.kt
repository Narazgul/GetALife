package app.tinygiants.getalife.di

import android.content.Context
import app.tinygiants.getalife.BuildConfig
import app.tinygiants.getalife.data.local.GetALifeDatabase
import app.tinygiants.getalife.data.remote.ai.ChatGptAi
import app.tinygiants.getalife.data.remote.ai.GoogleGeminiAi
import app.tinygiants.getalife.domain.repository.AiRepository
import com.aallam.openai.client.OpenAI
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // region Data

    @Provides
    fun provideFirebaseFirestore() = Firebase.firestore

    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context) = GetALifeDatabase.getDatabase(appContext)

    @Provides
    fun provideHeaderDao(database: GetALifeDatabase) = database.headerDao()

    @Provides
    fun provideCategoryDao(database: GetALifeDatabase) = database.categoryDao()

    @Provides
    fun provideAccountDao(database: GetALifeDatabase) = database.accountsDao()

    @Provides
    fun provideTransactionDao(database: GetALifeDatabase) = database.transactionDao()

    // endregion

    // region AI

    @Provides
    fun provideGeminiProGenerativeModel() = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = BuildConfig.GEMINI_API_KEY)

    @Gemini
    @Provides
    fun provideGeminiAi(generativeModel: GenerativeModel): AiRepository = GoogleGeminiAi(generativeModel = generativeModel)

    @Provides
    fun provideOpenAi(): OpenAI = OpenAI(BuildConfig.CHATGPT_API_KEY)

    @ChatGPT
    @Provides
    fun provideChatGPT(openAi: OpenAI): AiRepository = ChatGptAi(openAi = openAi)

    // endregion

    // region Dispatchers

    @Main
    @Provides
    fun provideMainDispatcher() = Dispatchers.Main

    @Io
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Default
    @Provides
    fun provideDefaultDispatcher() = Dispatchers.Default

    // endregion
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Main

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Io

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Default

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Gemini

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChatGPT