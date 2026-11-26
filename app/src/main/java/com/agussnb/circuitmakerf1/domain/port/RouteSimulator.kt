package com.agussnb.circuitmakerf1.domain.port

import com.agussnb.circuitmakerf1.domain.model.GeoPoint

/**
 * Controla el GPS simulado (solo tiene efecto cuando la opción "GPS simulado" está activa).
 */
interface RouteSimulator {
    /**
     * Recorrido que va a seguir la próxima simulación.
     * - Con un circuito: arranca un poco antes de la meta y da vueltas sobre él.
     * - Con `null`: usa un recorrido de demostración (para grabar un circuito nuevo).
     */
    fun useRoute(route: List<GeoPoint>?)
}
