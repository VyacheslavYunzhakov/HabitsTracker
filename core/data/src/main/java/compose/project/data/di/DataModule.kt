package compose.project.data.di

import android.content.Context
import androidx.room.Room
import compose.project.data.HabitRepository
import compose.project.data.HabitRepositoryImpl
import compose.project.data.local.HabitDao
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
        ).addMigrations(HabitTrackerDatabase.MIGRATION_1_2,HabitTrackerDatabase.MIGRATION_2_3,
            HabitTrackerDatabase.MIGRATION_3_4)
            .build()
    }

    @Provides
    fun provideHabitDayDao(database: HabitTrackerDatabase): HabitDayDao = database.habitDayDao()

    @Provides
    fun provideHabitDao(database: HabitTrackerDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitRepository(habitDayDao: HabitDayDao, habitDao: HabitDao): HabitRepository = HabitRepositoryImpl(habitDayDao, habitDao)

}
