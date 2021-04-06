package br.com.breno.animallovers.broadcast

import android.app.Service
import android.content.Intent

import android.os.IBinder
import android.util.Log
import br.com.breno.animallovers.model.Login
import br.com.breno.animallovers.service.DonoService
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ManageDisconnectionFromApp : Service() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database = Firebase.database.reference

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ClearFromRecentService", "Service Started")
        return START_NOT_STICKY
    }

    override fun onDestroy() {

        if(FirebaseAuth.getInstance().currentUser != null) {
            val donoService = DonoService()
            val loginModel = Login()

            loginModel.authUid = auth.uid.toString()
            loginModel.logged = false
            loginModel.lastLogin = System.currentTimeMillis() / 1000

            donoService.persistOwnerLoginStatus(loginModel)
        }

        super.onDestroy()
        Log.d("ClearFromRecentService", "Service Destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {

        Log.e("ClearFromRecentService", "END")
        //Code here
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
    }
}