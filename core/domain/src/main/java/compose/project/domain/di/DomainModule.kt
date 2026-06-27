package compose.project.domain.di

import compose.project.domain.HabitInteractor
import compose.project.domain.HabitInteractorImpl
import org.koin.dsl.module

val domainModule = module {

    single<HabitInteractor> {
        HabitInteractorImpl(habitRepository = get())
    }
}