package com.evilcorp.android.labs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.ui.Card
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Muted
import com.evilcorp.android.ui.SectionTitle

/** Common frame: what the lab does, the interactive part, and how to fix it. */
@Composable
fun LabFrame(
    scenario: String,
    remediation: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card {
            SectionTitle("Scenario")
            Text(scenario, fontSize = 14.sp)
        }
        Card {
            SectionTitle("Lab")
            content()
        }
        Card {
            SectionTitle("Remediation")
            Text(remediation, fontSize = 13.sp, color = Muted)
        }
    }
}

@Composable
fun LabField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    secret: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun Status(text: String, ok: Boolean = false) {
    Text(text, fontSize = 13.sp, color = if (ok) Green else Muted)
}
