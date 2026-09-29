package com.john.inkwell.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.john.inkwell.data.Block
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

/**
 * One timestamped block: time header (+ optional date, for contexts like
 * search results that span many days), editable body text, and tag/mood
 * chips if the block has them.
 *
 * [onTextCommitted] fires when the field loses focus with changed text —
 * editing is always in place, there's no separate "edit mode".
 */
@Composable
fun BlockRow(
    block: Block,
    onTextCommitted: (String) -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
    editable: Boolean = true
) {
    var text by remember(block.id) { mutableStateOf(block.text) }
    val time = remember(block.timestamp) {
        Instant.ofEpochMilli(block.timestamp).atZone(ZoneId.systemDefault()).format(timeFormatter)
    }
    val date = remember(block.timestamp) {
        Instant.ofEpochMilli(block.timestamp).atZone(ZoneId.systemDefault()).format(dateFormatter)
    }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(
            text = if (showDate) "$date · $time" else time,
            style = MaterialTheme.typography.labelMedium
        )

        if (editable) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) onTextCommitted(text)
                    }
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (block.tag != null || block.mood != null) {
            Row(modifier = Modifier.padding(top = 6.dp)) {
                block.tag?.let { ChipLabel(it) }
                block.mood?.let { ChipLabel(it, leadingSpace = block.tag != null) }
            }
        }
    }
}

@Composable
private fun ChipLabel(label: String, leadingSpace: Boolean = false) {
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        shape = RoundedCornerShape(50),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.padding(start = if (leadingSpace) 6.dp else 0.dp)
    )
}
