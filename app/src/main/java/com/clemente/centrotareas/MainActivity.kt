package com.clemente.centrotareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clemente.centrotareas.data.TareasRepositoryLocal
import com.clemente.centrotareas.ui.PantallaTareas
import com.clemente.centrotareas.ui.theme.CentroTareasTheme
import com.clemente.centrotareas.viewmodel.TareasViewModel
import com.clemente.centrotareas.viewmodel.TareasViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CentroTareasTheme {
                val tareasViewModel: TareasViewModel = viewModel(
                    factory = TareasViewModelFactory(TareasRepositoryLocal())
                )
                PantallaTareas(tareasViewModel)
            }
        }
    }
}
