package compose.project.habitstracker

import android.app.Application
import compose.project.data2.di.dataModule
import compose.project.domain2.di.domainModule
import compose.project.home2.di.viewModelModule
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