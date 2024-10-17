package app.tinygiants.getalife.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Main
    @Provides
    fun provideMainDispatcher() = Dispatchers.Main

    @Io
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Default
    @Provides
    fun provideDefaultDispatcher() = Dispatchers.Default

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