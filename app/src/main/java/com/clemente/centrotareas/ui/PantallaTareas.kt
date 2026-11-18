package com.clemente.centrotareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.clemente.centrotareas.viewmodel.TareasViewModel
import com.clemente.centrotareas.model.PrimosUi
import com.clemente.centrotareas.model.CargaUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(viewModel: TareasViewModel) {
    val mensaje by viewModel.mensaje.collectAsState()
    val progreso by viewModel.progreso.collectAsState()
    val corriendo by viewModel.corriendo.collectAsState()
    val primos by viewModel.primos.collectAsState()
    val carga by viewModel.carga.collectAsState()
    val cargando = carga == CargaUi.Cargando
    var hasta by rememberSaveable { mutableStateOf("500000") }
    val limite = hasta.toIntOrNull()
    val entradaValida = limite != null && limite >= 2
    val calculando = primos == PrimosUi.Calculando
    var toques by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaTarea("Tarea con progreso") {
                Button(onClick = { toques++ }) { Text("Tócame: $toques") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::tareaLarga, enabled = !corriendo) { Text("Iniciar") }
                    OutlinedButton(
                        onClick = viewModel::cancelar,
                        enabled = corriendo,
                        modifier = Modifier.testTag("cancelarProgreso")
                    ) { Text("Cancelar") }
                }
                LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
                Text("${(progreso * 100).toInt()} %")
                Text(mensaje)
            }
            TarjetaTarea("Contar números primos") {
                OutlinedTextField(
                    value = hasta,
                    onValueChange = { hasta = it },
                    modifier = Modifier.fillMaxWidth().testTag("limitePrimos"),
                    label = { Text("Hasta N") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !calculando,
                    isError = !entradaValida,
                    supportingText = {
                        if (!entradaValida) Text("Ingresa un entero entre 2 y 2147483647")
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { limite?.let(viewModel::contar) },
                        enabled = entradaValida && !calculando
                    ) { Text("Contar") }
                    OutlinedButton(
                        onClick = viewModel::cancelarPrimos,
                        enabled = calculando,
                        modifier = Modifier.testTag("cancelarPrimos")
                    ) { Text("Cancelar") }
                }
                when (val estado = primos) {
                    PrimosUi.Inicial -> Text("Ingresa un límite para contar los números primos.")
                    PrimosUi.Calculando -> Text("Calculando...")
                    PrimosUi.Cancelado -> Text("Cálculo cancelado")
                    is PrimosUi.Listo -> RecuadroResultado(
                        "${estado.cantidad} primos · ${estado.milisegundos} ms"
                    )
                }
            }
            TarjetaTarea("Dos cargas") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = viewModel::cargarUnaPorUna, enabled = !cargando) {
                        Text("Una por una")
                    }
                    Button(onClick = viewModel::cargarALaVez, enabled = !cargando) {
                        Text("A la vez")
                    }
                }
                when (val estado = carga) {
                    CargaUi.Inicial -> Text("Perfil: 2 s · Pedidos: 3 s")
                    CargaUi.Cargando -> {
                        CircularProgressIndicator()
                        Text("Cargando...")
                    }
                    is CargaUi.Exito -> RecuadroResultado(
                        "${estado.modo}\n${estado.perfil}\nPedidos: ${estado.pedidos}\n${estado.milisegundos} ms"
                    )
                    is CargaUi.Error -> Text(estado.mensaje)
                }
            }
        }
    }
}

@Composable
private fun RecuadroResultado(texto: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFD9F2DF),
        contentColor = Color(0xFF123C22)
    ) {
        Text(texto, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun TarjetaTarea(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}