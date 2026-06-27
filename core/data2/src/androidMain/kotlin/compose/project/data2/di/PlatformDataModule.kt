package compose.project.data2.di

import compose.project.data2.DatabaseFactory
import org.koin.dsl.module

actual val platformDataModule = module {
    single { DatabaseFactory(get()) }
}
