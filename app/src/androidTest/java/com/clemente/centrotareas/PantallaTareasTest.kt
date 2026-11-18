package com.clemente.centrotareas

import android.os.SystemClock
import android.graphics.Bitmap
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
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.ViewModelProvider
import com.clemente.centrotareas.viewmodel.TareasViewModel
import com.clemente.centrotareas.model.PrimosUi
import com.clemente.centrotareas.model.CargaUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
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
        capturar("01-progreso")
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
        capturar("02-cancelada")
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
            if (limite == 1000) capturar("03-primos")
        }
    }

    @Test
    fun calculoGrandeRespondeYSeCancela() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        compose.onNodeWithTag("limitePrimos").performScrollTo().performTextReplacement("2147483647")
        compose.onNodeWithText("Contar", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Tócame: 0").performScrollTo().performClick()
        compose.onNodeWithText("Tócame: 1").assertIsDisplayed()
        assertTrue(viewModel.primos.value is PrimosUi.Calculando)
        compose.onNodeWithTag("cancelarPrimos").performScrollTo().performClick()
        compose.waitUntil(3000) { viewModel.primos.value == PrimosUi.Cancelado }
        compose.onNodeWithText("Cálculo cancelado").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Contar", substring = false).assertIsEnabled()
    }

    @Test
    fun comparaCargasSecuencialesYParalelasSinBloquear() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        for ((modo, minimo, maximo) in listOf(
            Triple("Una por una", 4900L, 8000L),
            Triple("A la vez", 2900L, 5000L)
        )) {
            compose.onNodeWithText(modo, substring = false).performScrollTo().performClick()
            compose.onNodeWithText("Una por una", substring = false).assertIsNotEnabled()
            compose.onNodeWithText("A la vez", substring = false).assertIsNotEnabled()
            compose.onNodeWithText("Tócame:", substring = true).performScrollTo().performClick()
            compose.waitUntil(10000) { viewModel.carga.value is CargaUi.Exito }
            val resultado = viewModel.carga.value as CargaUi.Exito
            assertEquals(modo, resultado.modo)
            assertEquals("Ana Torres (ficticia)", resultado.perfil)
            assertEquals(3, resultado.pedidos)
            assertTrue("Tiempo inesperado: ${resultado.milisegundos}", resultado.milisegundos in minimo..maximo)
            android.util.Log.d("PRUEBA", "$modo: ${resultado.milisegundos} ms")
            compose.onNodeWithText("Pedidos: 3", substring = true).performScrollTo().assertIsDisplayed()
            capturar(if (modo == "Una por una") "04-secuencial" else "04-paralela")
        }
        val comparacion = viewModel.comparaciones.value.single()
        assertTrue(checkNotNull(comparacion.unaPorUna) > checkNotNull(comparacion.aLaVez))
        compose.onNodeWithTag("comparacion-1").performScrollTo().assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        assertEquals(listOf(comparacion), viewModel.comparaciones.value)
        compose.onNodeWithText("Comparación de tiempos").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("comparacion-1").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun errorSimuladoEnAmbosModosPermiteReintentar() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        for (modo in listOf("Una por una", "A la vez")) {
            val antesDelError = viewModel.comparaciones.value
            compose.onNodeWithTag("simularError").performScrollTo().performClick()
            compose.onNodeWithText(modo, substring = false).performScrollTo().performClick()
            compose.waitUntil(8000) { viewModel.carga.value is CargaUi.Error }
            assertEquals(antesDelError, viewModel.comparaciones.value)
            compose.onNodeWithText("No se pudo cargar: Sin conexión").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Reintentar").performScrollTo().assertIsDisplayed()
            if (modo == "Una por una") capturar("05-error")
            compose.onNodeWithTag("simularError").performScrollTo().performClick()
            compose.onNodeWithText("Reintentar").performScrollTo().performClick()
            compose.waitUntil(6000) { viewModel.carga.value is CargaUi.Exito }
            assertEquals("A la vez", (viewModel.carga.value as CargaUi.Exito).modo)
            compose.onNodeWithText("Pedidos: 3", substring = true).performScrollTo().assertIsDisplayed()
            if (modo == "Una por una") capturar("05-reintento")
        }
    }

    @Test
    fun recrearPantallaConservaViewModelYProgreso() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        compose.onNodeWithText("Iniciar").performClick()
        compose.waitUntil(3000) { viewModel.progreso.value >= 0.15f }
        val progresoAnterior = viewModel.progreso.value
        compose.activityRule.scenario.recreate()
        assertSame(viewModel, ViewModelProvider(compose.activity)[TareasViewModel::class.java])
        assertTrue(viewModel.progreso.value >= progresoAnterior)
        compose.waitUntil(7000) { !viewModel.corriendo.value }
        compose.onNodeWithText("100 %").assertIsDisplayed()
        compose.onNodeWithText("Terminó").assertIsDisplayed()
    }

    @Test
    fun mideLimiteInicialDePrimos() {
        val viewModel = ViewModelProvider(compose.activity)[TareasViewModel::class.java]
        compose.runOnIdle { viewModel.contar(10000000) }
        compose.waitUntil(10000) { viewModel.primos.value is PrimosUi.Listo }
        val resultado = viewModel.primos.value as PrimosUi.Listo
        assertEquals(664579, resultado.cantidad)
        android.util.Log.d("PRUEBA", "Primos hasta 10000000: ${resultado.milisegundos} ms")
    }

    private fun capturar(nombre: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directorio = File(instrumentation.targetContext.getExternalFilesDir(null), "evidencias")
        check(directorio.mkdirs() || directorio.isDirectory)
        val imagen = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        FileOutputStream(File(directorio, "$nombre.png")).use {
            check(imagen.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        imagen.recycle()
    }
}