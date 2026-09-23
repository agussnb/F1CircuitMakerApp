package com.example.circuitmakerf1.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Añadir circuito"
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = label)
    }
}