package com.clemente.centrotareas

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clemente.centrotareas.data.TareasRepositoryLocal
import com.clemente.centrotareas.model.AvancePrimos
import com.clemente.centrotareas.model.PrimosUi
import com.clemente.centrotareas.viewmodel.TareasViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class MejoraIaTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun flujoDeProgresoEmiteDeCeroACienYSePuedeRecolectarOtraVez() {
        val repo = TareasRepositoryLocal()
        val valores = CopyOnWriteArrayList<Float>()
        lateinit var trabajo: Job
        compose.runOnIdle {
            trabajo = compose.activity.lifecycleScope.launch {
                repo.progresoTarea().collect { valores.add(it) }
            }
        }
        compose.waitUntil(7000) { trabajo.isCompleted }
        assertTrue(!trabajo.isCancelled)
        assertEquals(101, valores.size)
        assertEquals(0f, valores.first())
        assertEquals(1f, valores.last())
        assertTrue(valores.zipWithNext().all { (antes, despues) -> despues >= antes })

        valores.clear()
        compose.runOnIdle {
            trabajo = compose.activity.lifecycleScope.launch {
                repo.progresoTarea().take(3).collect { valores.add(it) }
            }
        }
        compose.waitUntil(3000) { trabajo.isCompleted }
        assertTrue(!trabajo.isCancelled)
        assertEquals(listOf(0f, 0.01f, 0.02f), valores.toList())
    }

    @Test
    fun cancelarLaRecoleccionDetieneElFlujoDelRepository() {
        val valores = CopyOnWriteArrayList<Float>()
        lateinit var trabajo: Job
        compose.runOnIdle {
            trabajo = compose.activity.lifecycleScope.launch {
                TareasRepositoryLocal().progresoTarea().collect { valores.add(it) }
            }
        }
        compose.waitUntil(3000) { valores.size >= 5 }
        compose.runOnIdle { trabajo.cancel() }
        compose.waitUntil(3000) { trabajo.isCompleted }
        assertTrue(trabajo.isCancelled)
        assertTrue(valores.last() < 1f)
    }

    @Test
    fun primosEmitePorcentajesMonotonosYResultadosExactos() {
        for ((limite, esperado) in listOf(2 to 1, 3 to 2, 1000 to 168)) {
            val valores = CopyOnWriteArrayList<AvancePrimos>()
            lateinit var trabajo: Job
            compose.runOnIdle {
                trabajo = compose.activity.lifecycleScope.launch {
                    TareasRepositoryLocal().contarPrimos(limite).collect { valores.add(it) }
                }
            }
            compose.waitUntil(3000) { trabajo.isCompleted }
            assertTrue(!trabajo.isCancelled)
            assertEquals(AvancePrimos(0, 0), valores.first())
            assertEquals(AvancePrimos(100, esperado), valores.last())
            assertTrue(valores.size <= 101)
            assertTrue(valores.zipWithNext().all { (antes, despues) ->
                despues.porcentaje > antes.porcentaje && despues.cantidad >= antes.cantidad
            })
        }
    }

    @Test
    fun primosMuestraAvanceEvitaDobleInicioYPermiteCancelarYReiniciar() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        compose.runOnIdle { viewModel.contar(10000000) }
        compose.waitUntil(5000) {
            (viewModel.primos.value as? PrimosUi.Calculando)?.porcentaje in 1..99
        }
        compose.onNodeWithTag("progresoPrimos").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            viewModel.contar(1000)
            assertTrue(viewModel.primos.value is PrimosUi.Calculando)
            viewModel.cancelarPrimos()
        }
        compose.waitUntil(3000) { viewModel.primos.value == PrimosUi.Cancelado }
        compose.onNodeWithText("Cálculo cancelado").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { viewModel.contar(1000) }
        compose.waitUntil(3000) { viewModel.primos.value is PrimosUi.Listo }
        assertEquals(168, (viewModel.primos.value as PrimosUi.Listo).cantidad)
    }
}