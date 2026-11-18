package com.clemente.centrotareas

import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}