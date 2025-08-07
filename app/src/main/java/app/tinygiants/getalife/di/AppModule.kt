package app.tinygiants.getalife.di

import android.content.Context
import app.tinygiants.getalife.BuildConfig
import app.tinygiants.getalife.R
import app.tinygiants.getalife.data.remote.ai.ChatGptAi
import app.tinygiants.getalife.data.remote.ai.FirebaseAi
import app.tinygiants.getalife.domain.repository.AiRepository
import com.aallam.openai.client.OpenAI
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideFirebaseAi(crashlytics: FirebaseCrashlytics): AiRepository = FirebaseAi(
        generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel("gemini-1.5-flash"),
        crashlytics = crashlytics
    )
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChatGPT

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FirebaseGemini