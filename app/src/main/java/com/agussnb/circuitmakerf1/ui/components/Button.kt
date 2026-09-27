package com.agussnb.circuitmakerf1.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Añadir circuito",
    enabled: Boolean = true
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text = label)
    }
}