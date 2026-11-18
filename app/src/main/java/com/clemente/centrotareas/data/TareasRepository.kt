package com.clemente.centrotareas.data

interface TareasRepository {
    suspend fun contarPrimos(hasta: Int): Int
}