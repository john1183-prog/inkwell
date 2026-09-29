package com.john.inkwell.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.ui.common.BlockRow
import com.john.inkwell.ui.theme.PaperGrain
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val displayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")

@Composable
fun DayDetailScreen(repository: InkwellRepository, date: String, onBack: () -> Unit) {
    val viewModel: DayDetailViewModel = viewModel(factory = DayDetailViewModelFactory(repository, date))
    val blocks by viewModel.blocks.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        PaperGrain(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp, end = 20.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back to history")
                }
                Text(
                    text = LocalDate.parse(date).format(displayFormatter),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                items(blocks, key = { it.id }) { block ->
                    BlockRow(
                        block = block,
                        onTextCommitted = { newText -> viewModel.editBlockText(block, newText) }
                    )
                }
            }
        }
    }
}
