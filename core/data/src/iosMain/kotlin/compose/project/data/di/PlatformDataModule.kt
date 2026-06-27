package compose.project.data.di

import compose.project.data.DatabaseFactory
import org.koin.dsl.module

actual val platformDataModule = module {
    single { DatabaseFactory() }
}
