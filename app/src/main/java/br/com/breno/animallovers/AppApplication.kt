package br.com.breno.animallovers

import android.app.Application
import br.com.breno.animallovers.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AppApplication)
            modules(
                listOf(
                    viewModelModule
                )
            )
        }
    }

}