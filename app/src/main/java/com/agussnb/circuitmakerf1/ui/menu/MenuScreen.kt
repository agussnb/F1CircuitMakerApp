package com.agussnb.circuitmakerf1.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agussnb.circuitmakerf1.ui.theme.F1Red
import com.agussnb.circuitmakerf1.ui.theme.SectorColors

/** Menú principal. No tiene estado propio, así que no necesita ViewModel. */
@Composable
fun MenuScreen(
    onMyTracksClick: () -> Unit,
    onCreateTrackClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CIRCUIT",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "MAKER",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            color = F1Red
        )
        Spacer(Modifier.height(12.dp))
        SectorStripe()
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Creá tu circuito y corré contra el reloj",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(48.dp))

        MenuButton("Mis circuitos", Icons.AutoMirrored.Filled.List, onMyTracksClick, primary = false)
        Spacer(Modifier.height(12.dp))
        MenuButton("Crear circuito", Icons.Filled.AddCircle, onCreateTrackClick, primary = true)
        Spacer(Modifier.height(12.dp))
        MenuButton("Opciones", Icons.Filled.Settings, onOptionsClick, primary = false)
    }
}

/** Tres franjas con los colores de los sectores. */
@Composable
private fun SectorStripe() {
    Row {
        SectorColors.forEach { color ->
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .background(color)
            )
        }
    }
}

@Composable
private fun MenuButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    primary: Boolean
) {
    val modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
    val content: @Composable () -> Unit = {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    if (primary) {
        Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors()) { content() }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { content() }
    }
}
