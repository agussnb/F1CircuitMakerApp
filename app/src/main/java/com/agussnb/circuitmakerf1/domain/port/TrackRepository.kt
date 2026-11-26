package com.agussnb.circuitmakerf1.domain.port

import com.agussnb.circuitmakerf1.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    /** Lista de circuitos, sin el trazado (para que la lista cargue rápido). */
    val allTracks: Flow<List<Track>>

    /** Guarda el circuito con su trazado y devuelve el id generado. */
    suspend fun insertTrack(track: Track): Int

    /** Borra el circuito, su trazado y todas sus sesiones. */
    suspend fun deleteTrack(track: Track)

    /** Devuelve el circuito con su trazado completo. */
    suspend fun getTrackById(id: Int): Track?
}
