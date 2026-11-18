package com.clemente.centrotareas

import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.ViewModelProvider
import com.clemente.centrotareas.viewmodel.TareasViewModel
import com.clemente.centrotareas.model.PrimosUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PantallaTareasTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun muestraTresTarjetasYCuentaToques() {
        compose.onNodeWithText("CentroTareas").assertIsDisplayed()
        compose.onNodeWithText("Tarea con progreso").assertIsDisplayed()
        compose.onNodeWithText("Contar números primos").assertIsDisplayed()
        compose.onNodeWithText("Dos cargas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Tócame: 0").performScrollTo()
        compose.onNodeWithText("Tócame: 0").performClick()
        compose.onNodeWithText("Tócame: 1").assertIsDisplayed()
    }

    @Test
    fun tareaLargaNoBloqueaLosToques() {
        val antes = SystemClock.elapsedRealtime()
        compose.onNodeWithText("0 %").assertIsDisplayed()
        compose.onNodeWithText("Iniciar").performClick()
        compose.onNodeWithText("Trabajando...").assertIsDisplayed()
        compose.onNodeWithText("Tócame: 0").performClick()
        compose.onNodeWithText("Tócame: 1").assertIsDisplayed()
        compose.onNodeWithText("Trabajando...").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 7000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("Terminó")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Terminó").assertIsDisplayed()
        compose.onNodeWithText("100 %").assertIsDisplayed()
        assertTrue(SystemClock.elapsedRealtime() - antes >= 4000)
    }

    @Test
    fun cancelaElProgresoYPermiteReiniciarSinDobleInicio() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        compose.onNodeWithTag("cancelarProgreso").assertIsNotEnabled()
        compose.onNodeWithText("Iniciar").performClick()
        compose.onNodeWithText("Iniciar").assertIsNotEnabled()
        compose.waitUntil(3000) { viewModel.progreso.value >= 0.2f }
        compose.runOnIdle {
            val progreso = viewModel.progreso.value
            viewModel.tareaLarga()
            assertTrue(viewModel.progreso.value >= progreso)
        }
        compose.onNodeWithTag("cancelarProgreso").performClick()
        compose.onNodeWithText("Cancelado").assertIsDisplayed()
        compose.onNodeWithText("Iniciar").assertIsEnabled()
        compose.onNodeWithTag("cancelarProgreso").assertIsNotEnabled()
        val detenido = viewModel.progreso.value
        val antes = SystemClock.elapsedRealtime()
        compose.waitUntil(2000) { SystemClock.elapsedRealtime() - antes >= 300 }
        assertEquals(detenido, viewModel.progreso.value)
        compose.onNodeWithText("Iniciar").performClick()
        assertTrue(viewModel.progreso.value < detenido)
        compose.onNodeWithTag("cancelarProgreso").performClick()
    }

    @Test
    fun cuentaPrimosConResultadosDeReferencia() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        for ((limite, esperado) in listOf(100 to 25, 1000 to 168, 10000 to 1229)) {
            compose.onNodeWithTag("limitePrimos").performScrollTo().performTextReplacement("$limite")
            compose.onNodeWithText("Contar", substring = false).performScrollTo().performClick()
            compose.waitUntil(5000) { viewModel.primos.value is PrimosUi.Listo }
            assertEquals(esperado, (viewModel.primos.value as PrimosUi.Listo).cantidad)
            compose.onNodeWithText("$esperado primos", substring = true).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun calculoGrandeRespondeYSeCancela() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        compose.onNodeWithTag("limitePrimos").performScrollTo().performTextReplacement("2147483647")
        compose.onNodeWithText("Contar", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Tócame: 0").performScrollTo().performClick()
        compose.onNodeWithText("Tócame: 1").assertIsDisplayed()
        assertEquals(PrimosUi.Calculando, viewModel.primos.value)
        compose.onNodeWithTag("cancelarPrimos").performScrollTo().performClick()
        compose.waitUntil(3000) { viewModel.primos.value == PrimosUi.Cancelado }
        compose.onNodeWithText("Cálculo cancelado").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Contar", substring = false).assertIsEnabled()
    }
}