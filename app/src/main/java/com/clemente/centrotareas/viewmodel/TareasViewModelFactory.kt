package com.clemente.centrotareas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clemente.centrotareas.data.TareasRepository

class TareasViewModelFactory(private val repo: TareasRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TareasViewModel::class.java)) {
            "ViewModel desconocido: ${modelClass.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return TareasViewModel(repo) as T
    }
}