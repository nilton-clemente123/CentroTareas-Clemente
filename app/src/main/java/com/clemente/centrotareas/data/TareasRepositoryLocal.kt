package com.clemente.centrotareas.data

import android.util.Log
import com.clemente.centrotareas.model.AvancePrimos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {
    override fun progresoTarea(): Flow<Float> = flow {
        Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
        emit(0f)
        for (i in 1..100) {
            delay(40)
            emit(i / 100f)
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun cargarPerfil(): String = withContext(Dispatchers.IO) {
        delay(2000)
        "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> = withContext(Dispatchers.IO) {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    override fun contarPrimos(hasta: Int): Flow<AvancePrimos> = flow {
        require(hasta >= 2) { "El límite debe ser al menos 2" }
        var cantidad = 0
        var ultimoPorcentaje = 0
        val total = hasta.toLong() - 1
        emit(AvancePrimos(0, 0))
        for (n in 2..hasta) {
            currentCoroutineContext().ensureActive()
            if (esPrimo(n)) cantidad++
            val porcentaje = (((n.toLong() - 1) * 100) / total).toInt()
            if (porcentaje > ultimoPorcentaje) {
                emit(AvancePrimos(porcentaje, cantidad))
                ultimoPorcentaje = porcentaje
            }
        }
    }.flowOn(Dispatchers.Default)

    private fun esPrimo(n: Int): Boolean {
        if (n < 2) return false
        if (n == 2) return true
        if (n % 2 == 0) return false
        var divisor = 3
        // Equivale a divisor * divisor <= n, sin desbordamiento de Int.
        while (divisor <= n / divisor) {
            if (n % divisor == 0) return false
            divisor += 2
        }
        return true
    }
}