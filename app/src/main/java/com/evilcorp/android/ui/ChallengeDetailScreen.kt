package com.evilcorp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.labs.LabRegistry
import com.evilcorp.android.model.Challenge
import com.evilcorp.android.model.Scoring

@Composable
fun ChallengeDetailScreen(c: Challenge, store: ProgressStore, onBack: () -> Unit) {
    val hintsUsed = store.hintsUsed(c.id)
    val solvedPoints = store.solved[c.id]
    var flag by rememberSaveable(c.id) { mutableStateOf("") }
    var feedback by rememberSaveable(c.id) { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Labs", color = Muted) }
        Text(c.title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(c.description, fontSize = 14.sp, color = Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(c.difficulty.label, difficultyColor(c.difficulty))
            Chip(c.area.label)
            Chip("${c.difficulty.points} pts", Muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(c.cwe, Muted)
            Chip(c.masvs, Muted)
            Chip(c.maswe, Muted)
        }
        Text("CVSS ${c.cvss}", fontSize = 12.sp, color = Muted)

        LabRegistry.entries[c.id]?.content?.invoke()

        Card {
            SectionTitle("Hints ($hintsUsed/${c.hints.size}, −${Scoring.HINT_PENALTY} pts each)")
            c.hints.take(hintsUsed).forEachIndexed { i, h -> Text("${i + 1}. $h", fontSize = 13.sp) }
            if (hintsUsed < c.hints.size && solvedPoints == null) {
                GhostButton("Reveal hint ${hintsUsed + 1}") { store.revealHint(c.id, c.hints.size) }
            }
        }

        Card {
            SectionTitle("Tooling")
            Mono(c.tooling)
        }

        Card {
            SectionTitle("Submit flag")
            if (solvedPoints != null) {
                Text("Solved · +$solvedPoints pts", color = Green, fontWeight = FontWeight.SemiBold)
            } else {
                OutlinedTextField(
                    value = flag, onValueChange = { flag = it }, singleLine = true,
                    label = { Text("EVILCORP{…}") }, modifier = Modifier.padding(0.dp),
                )
                PrimaryButton("Submit") {
                    feedback = if (LabRegistry.isCorrect(c.id, flag)) {
                        store.markSolved(c)
                        "Correct!"
                    } else {
                        "Not quite. Flags look like EVILCORP{...}."
                    }
                }
                if (feedback.isNotEmpty()) Text(feedback, color = if (feedback == "Correct!") Green else Red, fontSize = 13.sp)
            }
        }
    }
}
