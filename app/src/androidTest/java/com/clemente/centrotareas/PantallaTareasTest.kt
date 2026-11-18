package com.clemente.centrotareas

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clemente.centrotareas.data.TareasRepositoryLocal
import com.clemente.centrotareas.viewmodel.TareasViewModel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun tareaMalaBloqueaElHiloPrincipalDuranteCuatroSegundos() {
        val viewModel = TareasViewModel(TareasRepositoryLocal())
        val inicio = CountDownLatch(1)
        val terminado = CountDownLatch(1)
        val latido = CountDownLatch(1)
        val principal = Handler(Looper.getMainLooper())
        val antes = SystemClock.elapsedRealtime()
        principal.post {
            inicio.countDown()
            viewModel.tareaLargaMala()
            terminado.countDown()
        }
        assertTrue(inicio.await(2, TimeUnit.SECONDS))
        principal.post { latido.countDown() }
        assertFalse("El hilo principal no debe atender eventos durante el bloqueo", latido.await(1, TimeUnit.SECONDS))
        assertEquals("Trabajando...", viewModel.mensaje.value)
        assertTrue(terminado.await(6, TimeUnit.SECONDS))
        assertTrue(SystemClock.elapsedRealtime() - antes >= 4000)
        assertTrue(latido.await(2, TimeUnit.SECONDS))
        assertEquals("Terminó", viewModel.mensaje.value)
    }

    @Test
    fun botonMaloTerminaTrasLaEspera() {
        val antes = SystemClock.elapsedRealtime()
        compose.onNodeWithText("Tarea larga (MAL)").performClick()
        compose.onNodeWithText("Terminó").assertIsDisplayed()
        assertTrue(SystemClock.elapsedRealtime() - antes >= 4000)
    }
}