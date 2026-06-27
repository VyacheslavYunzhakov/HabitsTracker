package compose.project.data.di

import androidx.room.Room
import compose.project.data.HabitRepositoryImpl
import compose.project.data.local.HabitDao
import compose.project.data.local.HabitDayDao
import compose.project.data.local.HabitTrackerDatabase
import compose.project.domain.HabitRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            HabitTrackerDatabase::class.java,
            "habit_tracker.db"
        )
            .addMigrations(
                HabitTrackerDatabase.MIGRATION_1_2,
                HabitTrackerDatabase.MIGRATION_2_3,
                HabitTrackerDatabase.MIGRATION_3_4
            )
            .build()
    }

    single<HabitDayDao> {
        get<HabitTrackerDatabase>().habitDayDao()
    }

    single<HabitDao> {
        get<HabitTrackerDatabase>().habitDao()
    }

    single<HabitRepository> {
        HabitRepositoryImpl(
            habitDayDao = get(),
            habitDao = get()
        )
    }

}
