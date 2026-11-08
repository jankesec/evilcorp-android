package com.evilcorp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.model.Difficulty

val Green = Color(0xFF42D77D)
val Amber = Color(0xFFFFB020)
val Red = Color(0xFFFF4D4F)
val Panel = Color(0xFF111111)
val Muted = Color(0xFF9A9A9A)

fun difficultyColor(d: Difficulty): Color = when (d) {
    Difficulty.Easy -> Green
    Difficulty.Medium -> Amber
    Difficulty.Hard -> Red
}

@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Panel,
        contentColor = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF262626)),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun Chip(text: String, color: Color = Color.White) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
    ) {
        Text(
            text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text.uppercase(), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
fun Mono(text: String, modifier: Modifier = Modifier, color: Color = Color(0xFFD0D0D0)) {
    SelectionContainer {
        Text(
            text,
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                .padding(10.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = color,
        )
    }
}

@Composable
fun Warning(text: String) {
    Surface(
        color = Red.copy(alpha = 0.10f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Red.copy(alpha = 0.45f)),
    ) {
        Text(text, color = Color(0xFFFFB3B4), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) { Text(text) }
}

@Composable
fun StatRow(vararg items: Pair<String, String>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (label, value) ->
            Card(Modifier.weight(1f)) {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
