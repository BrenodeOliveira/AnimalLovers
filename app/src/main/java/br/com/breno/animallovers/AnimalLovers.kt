package br.com.breno.animallovers

import android.app.Application
import java.io.File
import java.io.IOException

class AnimalLovers : Application() {

    override fun onCreate() {
        super.onCreate()

        getExternalFilesDir(null)?.let { publicAppDirectory -> // getExternalFilesDir don't need storage permission
            val logDirectory = File("${publicAppDirectory.absolutePath}/logs")
            if (!logDirectory.exists()) {
                logDirectory.mkdir()
            }

            val logFile = File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt")
            // clear the previous logcat and then write the new one to the file
            try {
                Runtime.getRuntime().exec("logcat -c")
                Runtime.getRuntime().exec("logcat -f $logFile")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}