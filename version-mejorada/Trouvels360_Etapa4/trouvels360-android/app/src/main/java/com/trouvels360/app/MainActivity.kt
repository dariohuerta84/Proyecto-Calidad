package com.trouvels360.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Session
import com.trouvels360.app.data.remote.ApiClient
import com.trouvels360.app.ui.navigation.AppNavHost
import com.trouvels360.app.ui.theme.TrouvelsTheme
import kotlinx.coroutines.launch

class TrouvelsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        Session.init(this)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Valida la cookie JWT guardada (equivalente a AuthService.initializeAuth() de la web)
        lifecycleScope.launch { Repository.restoreSession() }
        setContent {
            TrouvelsTheme { AppNavHost() }
        }
    }
}
