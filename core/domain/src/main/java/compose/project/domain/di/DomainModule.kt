package compose.project.domain.di

import compose.project.data.HabitRepository
import compose.project.domain.HabitInteractor
import compose.project.domain.HabitInteractorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun provideHabitInteractor(habitRepository: HabitRepository): HabitInteractor =
        HabitInteractorImpl(habitRepository)

}