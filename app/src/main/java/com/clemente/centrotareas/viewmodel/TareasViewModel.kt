package com.clemente.centrotareas.viewmodel

import androidx.lifecycle.ViewModel
import com.clemente.centrotareas.data.TareasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    // Demostración temporal del Paso 1: deliberadamente bloquea el hilo principal.
    fun tareaLargaMala() {
        _mensaje.value = "Trabajando..."
        Thread.sleep(4000)
        _mensaje.value = "Terminó"
    }
}