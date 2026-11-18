package com.clemente.centrotareas

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.lifecycleScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clemente.centrotareas.data.TareasRepositoryLocal
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
}