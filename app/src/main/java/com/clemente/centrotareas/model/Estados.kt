package com.clemente.centrotareas.model

data class AvancePrimos(val porcentaje: Int, val cantidad: Int)

data class ComparacionTiempos(
    val numero: Int,
    val unaPorUna: Long? = null,
    val aLaVez: Long? = null
)

sealed interface PrimosUi {
    data object Inicial : PrimosUi
    data class Calculando(val porcentaje: Int = 0) : PrimosUi
    data class Listo(val cantidad: Int, val milisegundos: Long) : PrimosUi
    data object Cancelado : PrimosUi
}

sealed interface CargaUi {
    data object Inicial : CargaUi
    data object Cargando : CargaUi
    data class Exito(
        val modo: String,
        val perfil: String,
        val pedidos: Int,
        val milisegundos: Long
    ) : CargaUi
    data class Error(val mensaje: String) : CargaUi
}