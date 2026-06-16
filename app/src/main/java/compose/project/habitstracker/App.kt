package compose.project.habitstracker

import android.app.Application
import compose.project.data.di.dataModule
import compose.project.domain.di.domainModule
import compose.project.home.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HabitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HabitApplication)

            modules(
                dataModule,
                domainModule,
                viewModelModule
            )
        }
    }
}