package compose.project.data.di

import compose.project.data.DatabaseFactory
import compose.project.data.HabitRepositoryImpl
import compose.project.data.local.HabitDao
import compose.project.data.local.HabitDayDao
import compose.project.data.local.HabitTrackerDatabase
import compose.project.domain.HabitRepository
import org.koin.dsl.module


val dataModule = module {
    includes(platformDataModule)

    single {
        get<DatabaseFactory>().create()
    }

    single<HabitDao> {
        get<HabitTrackerDatabase>().habitDao()
    }

    single<HabitDayDao> {
        get<HabitTrackerDatabase>().habitDayDao()
    }

    single<HabitRepository> {
        HabitRepositoryImpl(
            habitDayDao = get(),
            habitDao = get()
        )
    }
}