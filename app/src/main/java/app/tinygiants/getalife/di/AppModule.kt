package app.tinygiants.getalife.di

import android.content.Context
import app.tinygiants.getalife.BuildConfig
import app.tinygiants.getalife.R
import app.tinygiants.getalife.data.local.GetALifeDatabase
import app.tinygiants.getalife.data.remote.ai.ChatGptAi
import app.tinygiants.getalife.data.remote.ai.FirebaseVertexAi
import app.tinygiants.getalife.domain.repository.AiRepository
import com.aallam.openai.client.OpenAI
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.vertexai.FirebaseVertexAI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideGroupDao(database: GetALifeDatabase) = database.groupDao()

    @Provides
    fun provideCategoryDao(database: GetALifeDatabase) = database.categoryDao()

    @Provides
    fun provideAccountDao(database: GetALifeDatabase) = database.accountsDao()

    @Provides
    fun provideTransactionDao(database: GetALifeDatabase) = database.transactionDao()

    @Provides
    fun provideAppUpdateManager(@ApplicationContext appContext: Context) = AppUpdateManagerFactory.create(appContext)

    @Provides
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        return remoteConfig
    }

    // endregion

    // region AI

    @Vertex
    @Provides
    fun provideFirebaseVertexAi(): AiRepository = FirebaseVertexAi(
        generativeModel = FirebaseVertexAI.instance.generativeModel(modelName = "gemini-1.5-flash")
    )

    @ChatGPT
    @Provides
    fun provideChatGPT(): AiRepository = ChatGptAi(openAi = OpenAI(BuildConfig.CHATGPT_API_KEY))

    // endregion
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Vertex

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChatGPT