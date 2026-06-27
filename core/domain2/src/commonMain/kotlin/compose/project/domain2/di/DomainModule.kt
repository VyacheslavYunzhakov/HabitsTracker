package compose.project.domain2.di

import compose.project.domain2.HabitInteractor
import compose.project.domain2.HabitInteractorImpl
import org.koin.dsl.module

val domainModule = module {

    single<HabitInteractor> {
        HabitInteractorImpl(habitRepository = get())
    }
}