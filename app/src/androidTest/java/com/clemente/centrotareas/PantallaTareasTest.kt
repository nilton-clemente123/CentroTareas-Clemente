package com.clemente.centrotareas

import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.ViewModelProvider
import com.clemente.centrotareas.viewmodel.TareasViewModel
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
        compose.onNodeWithText("Dos cargas").assertIsDisplayed()
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
        compose.onNodeWithText("Cancelar").assertIsNotEnabled()
        compose.onNodeWithText("Iniciar").performClick()
        compose.onNodeWithText("Iniciar").assertIsNotEnabled()
        compose.waitUntil(3000) { viewModel.progreso.value >= 0.2f }
        compose.runOnIdle {
            val progreso = viewModel.progreso.value
            viewModel.tareaLarga()
            assertTrue(viewModel.progreso.value >= progreso)
        }
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Cancelado").assertIsDisplayed()
        compose.onNodeWithText("Iniciar").assertIsEnabled()
        compose.onNodeWithText("Cancelar").assertIsNotEnabled()
        val detenido = viewModel.progreso.value
        val antes = SystemClock.elapsedRealtime()
        compose.waitUntil(2000) { SystemClock.elapsedRealtime() - antes >= 300 }
        assertEquals(detenido, viewModel.progreso.value)
        compose.onNodeWithText("Iniciar").performClick()
        assertTrue(viewModel.progreso.value < detenido)
        compose.onNodeWithText("Cancelar").performClick()
    }
}