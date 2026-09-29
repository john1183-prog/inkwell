package com.john.inkwell.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.ui.theme.PaperGrain
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

@Composable
fun HistoryScreen(repository: InkwellRepository, onDayClick: (String) -> Unit) {
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))
    val counts by viewModel.entryDateCounts.collectAsState()
    val today = repository.todayKey()

    Box(modifier = Modifier.fillMaxSize()) {
        PaperGrain(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            if (counts.isEmpty()) {
                Text(
                    text = "No entries yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp)) {
                items(counts, key = { it.entryDate }) { entry ->
                    val label = remember(entry.entryDate, today) { dayLabel(entry.entryDate, today) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDayClick(entry.entryDate) }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${entry.count} ${if (entry.count == 1) "block" else "blocks"}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                }
            }
        }
    }
}

private fun dayLabel(isoDate: String, todayIso: String): String {
    if (isoDate == todayIso) return "Today"
    val date = LocalDate.parse(isoDate)
    val yesterday = LocalDate.parse(todayIso).minusDays(1)
    if (date == yesterday) return "Yesterday"
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$weekday, ${date.format(displayFormatter)}"
}
