package compose.project.data2.di

import compose.project.data2.DatabaseFactory
import compose.project.data2.HabitRepositoryImpl
import compose.project.data2.local.HabitDao
import compose.project.data2.local.HabitDayDao
import compose.project.data2.local.HabitTrackerDatabase
import compose.project.domain2.HabitRepository
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