package compose.project.shared

import compose.project.data.di.dataModule
import compose.project.domain.di.domainModule
import compose.project.home.di.viewModelModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(dataModule, domainModule, viewModelModule)
    }
}
