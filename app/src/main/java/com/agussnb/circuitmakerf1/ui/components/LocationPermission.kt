package com.agussnb.circuitmakerf1.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Devuelve una función que, al llamarla, se asegura de tener ubicación precisa y llama a
 * [onGranted]; si el usuario la niega, llama a [onDenied].
 *
 * En Android 13+ también pide permiso de notificaciones (para la notificación de grabación).
 * Si lo niega, la app funciona igual: solo no se ve la notificación.
 */
@Composable
fun rememberLocationPermissionRequest(
    onGranted: () -> Unit,
    onDenied: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    // rememberUpdatedState: el launcher se crea una vez, pero siempre llama a la versión
    // más nueva de los callbacks.
    val currentOnGranted = rememberUpdatedState(onGranted)
    val currentOnDenied = rememberUpdatedState(onDenied)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            currentOnGranted.value()
        } else {
            currentOnDenied.value()
        }
    }

    return {
        val missing = requiredPermissions().filterNot { context.isGranted(it) }
        if (context.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) && missing.isEmpty()) {
            currentOnGranted.value()
        } else {
            launcher.launch(
                (missing + Manifest.permission.ACCESS_COARSE_LOCATION).distinct().toTypedArray()
            )
        }
    }
}

private fun requiredPermissions(): List<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
