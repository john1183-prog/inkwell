package com.john.inkwell.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.ui.common.BlockRow
import com.john.inkwell.ui.theme.PaperGrain

@Composable
fun SearchScreen(repository: InkwellRepository) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(repository))
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        PaperGrain(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            BasicTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            )

            if (query.isNotBlank() && results.isEmpty()) {
                Text(
                    text = "No matches for \"$query\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp)) {
                items(results, key = { it.id }) { block ->
                    BlockRow(block = block, onTextCommitted = {}, showDate = true, editable = false)
                }
            }
        }
    }
}
