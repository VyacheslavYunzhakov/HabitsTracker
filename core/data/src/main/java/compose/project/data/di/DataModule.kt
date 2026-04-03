package compose.project.data.di

import android.content.Context
import androidx.room.Room
import compose.project.data.HabitRepository
import compose.project.data.HabitRepositoryImpl
import compose.project.data.local.HabitDayDao
import compose.project.data.local.HabitTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideHabitTrackerDatabase(@ApplicationContext context: Context): HabitTrackerDatabase {
        return Room.databaseBuilder(
            context,
            HabitTrackerDatabase::class.java,
            "habit_tracker.db",
        ).build()
    }

    @Provides
    fun provideHabitDayDao(database: HabitTrackerDatabase): HabitDayDao = database.habitDayDao()

    @Provides
    fun provideHabitRepository(habitDayDao: HabitDayDao): HabitRepository = HabitRepositoryImpl(habitDayDao)
}
