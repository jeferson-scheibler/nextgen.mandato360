// app/src/main/java/com/nextgen/mandato360/MandatoApp.kt
package com.nextgen.mandato360

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MandatoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Se já autenticado, checa se passou 24h desde o último login
        val auth = FirebaseAuth.getInstance()
        val prefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        val last = prefs.getLong("lastLogin", 0L)
        if (auth.currentUser != null && System.currentTimeMillis() - last > 24*60*60_000) {
            auth.signOut()
            prefs.edit().remove("lastLogin").apply()
        }
    }
}