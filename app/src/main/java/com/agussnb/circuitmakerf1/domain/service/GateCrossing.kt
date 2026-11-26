package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import com.agussnb.circuitmakerf1.domain.model.TimingGate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

/**
 * Detecta si el tramo entre dos lecturas GPS consecutivas cruza la línea [gate]
 * **en el sentido de carrera**, y devuelve el instante exacto del cruce (interpolado).
 *
 * Cómo funciona:
 * 1. Pasamos todo a un plano en metros con origen en la línea.
 * 2. Medimos cuánto "avanzó" cada lectura en la dirección de carrera: negativo = antes de
 *    la línea, positivo = después. Si pasa de negativo a positivo, se cruzó.
 * 3. La fracción `t` del tramo en la que se cruza nos da el tiempo: a 1 lectura por segundo,
 *    sin interpolar habría hasta 1 s de error en cada sector.
 * 4. Verificamos que el cruce esté dentro del ancho de la línea (si no, pasaste por otro lado).
 *
 * Devuelve `null` si no hubo cruce.
 */
fun crossingTimeMillis(previous: GpsPoint, current: GpsPoint, gate: TimingGate): Long? {
    val origin = gate.position
    val p1 = previous.position.toPlanar(origin)
    val p2 = current.position.toPlanar(origin)

    // Vector unitario en la dirección de carrera (x = este, y = norte).
    val bearing = gate.bearingDegrees.toRadians()
    val dirX = sin(bearing)
    val dirY = cos(bearing)

    val along1 = p1.x * dirX + p1.y * dirY
    val along2 = p2.x * dirX + p2.y * dirY
    if (!(along1 < 0.0 && along2 >= 0.0)) return null

    val t = along1 / (along1 - along2)
    val crossX = p1.x + t * (p2.x - p1.x)
    val crossY = p1.y + t * (p2.y - p1.y)

    // Distancia lateral al centro de la línea (componente perpendicular a la dirección).
    val lateral = abs(crossX * dirY - crossY * dirX)
    if (lateral > gate.halfWidthMeters) return null

    val elapsed = current.timestampMillis - previous.timestampMillis
    return previous.timestampMillis + (t * elapsed).roundToLong()
}
