package compose.project.home2.di

import compose.project.home2.HabitViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        HabitViewModel(habitInteractor = get())
    }
}
