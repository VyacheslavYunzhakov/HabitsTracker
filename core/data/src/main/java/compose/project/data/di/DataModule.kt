package compose.project.data.di

import compose.project.data.HabitRepository
import compose.project.data.HabitRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideHabitRepository (): HabitRepository = HabitRepositoryImpl()

}