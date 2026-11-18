package com.clemente.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clemente.centrotareas.data.TareasRepository
import com.clemente.centrotareas.model.PrimosUi
import com.clemente.centrotareas.model.CargaUi
import com.clemente.centrotareas.model.ComparacionTiempos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()
    private var trabajo: Job? = null
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()
    private var trabajoPrimos: Job? = null
    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()
    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()
    private val _comparaciones = MutableStateFlow<List<ComparacionTiempos>>(emptyList())
    val comparaciones: StateFlow<List<ComparacionTiempos>> = _comparaciones.asStateFlow()

    fun tareaLarga() {
        if (_corriendo.value) return
        _corriendo.value = true
        trabajo = viewModelScope.launch {
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")
                repo.progresoTarea().collect { avance ->
                    _progreso.value = avance
                }
                Log.d("HILO", "Después: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } finally {
                _corriendo.value = false
            }
        }
    }

    fun cancelar() {
        if (!_corriendo.value) return
        trabajo?.cancel()
        _mensaje.value = "Cancelado"
    }

    fun contar(hasta: Int) {
        if (hasta < 2 || _primos.value is PrimosUi.Calculando) return
        _primos.value = PrimosUi.Calculando()
        trabajoPrimos = viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                var cantidad = 0
                repo.contarPrimos(hasta).collect { avance ->
                    cantidad = avance.cantidad
                    _primos.value = PrimosUi.Calculando(avance.porcentaje)
                }
                _primos.value = PrimosUi.Listo(cantidad, System.currentTimeMillis() - inicio)
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
    }

    fun cargarUnaPorUna() {
        if (_carga.value == CargaUi.Cargando) return
        _carga.value = CargaUi.Cargando
        val errorSimulado = _simularError.value
        viewModelScope.launch {
            val inicio = System.nanoTime()
            try {
                val perfil = repo.cargarPerfil()
                val pedidos = repo.cargarPedidos(errorSimulado)
                registrarCarga(CargaUi.Exito(
                    "Una por una", perfil, pedidos.size, (System.nanoTime() - inicio) / 1_000_000
                ))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _carga.value = CargaUi.Error("No se pudo cargar: ${e.message ?: "Error desconocido"}")
            }
        }
    }

    fun cargarALaVez() {
        if (_carga.value == CargaUi.Cargando) return
        _carga.value = CargaUi.Cargando
        val errorSimulado = _simularError.value
        viewModelScope.launch {
            val inicio = System.nanoTime()
            try {
                val (perfil, pedidos) = coroutineScope {
                    val perfil = async { repo.cargarPerfil() }
                    val pedidos = async { repo.cargarPedidos(errorSimulado) }
                    perfil.await() to pedidos.await()
                }
                registrarCarga(CargaUi.Exito(
                    "A la vez", perfil, pedidos.size, (System.nanoTime() - inicio) / 1_000_000
                ))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _carga.value = CargaUi.Error("No se pudo cargar: ${e.message ?: "Error desconocido"}")
            }
        }
    }

    private fun registrarCarga(resultado: CargaUi.Exito) {
        val secuencial = resultado.modo == "Una por una"
        val filas = _comparaciones.value.toMutableList()
        val pendiente = filas.indexOfFirst {
            if (secuencial) it.unaPorUna == null else it.aLaVez == null
        }
        val indice = if (pendiente >= 0) pendiente else filas.size.also {
            filas.add(ComparacionTiempos(numero = it + 1))
        }
        val fila = filas[indice]
        filas[indice] = if (secuencial) {
            fila.copy(unaPorUna = resultado.milisegundos)
        } else {
            fila.copy(aLaVez = resultado.milisegundos)
        }
        _comparaciones.value = filas.toList()
        _carga.value = resultado
    }

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }
}