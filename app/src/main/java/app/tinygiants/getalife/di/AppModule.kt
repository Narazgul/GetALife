package app.tinygiants.getalife.di

import android.content.Context
import app.tinygiants.getalife.data.local.AppDatabase
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
    fun provideHeaderDao(@ApplicationContext appContext: Context) = AppDatabase.getDatabase(appContext).headerDao()

    @Provides
    fun provideCategoryDao(@ApplicationContext appContext: Context) = AppDatabase.getDatabase(appContext).categoryDao()

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