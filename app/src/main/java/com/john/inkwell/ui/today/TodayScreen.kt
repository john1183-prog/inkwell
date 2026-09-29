package com.john.inkwell.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.data.UserPreferences
import com.john.inkwell.ui.common.BlockRow
import com.john.inkwell.ui.common.QuickChipRow
import com.john.inkwell.ui.theme.PaperGrain
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(repository: InkwellRepository, preferences: UserPreferences) {
    val viewModel: TodayViewModel = viewModel(factory = TodayViewModelFactory(repository, preferences))
    val blocks by viewModel.blocks.collectAsState()
    val settings by preferences.settings.collectAsState(initial = com.john.inkwell.data.InkwellSettings())

    var composing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        PaperGrain(
            modifier = Modifier.fillMaxSize(),
            grainColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.035f * settings.grainIntensity * 2)
        )

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (!composing) {
                    FloatingActionButton(onClick = {
                        composing = true
                        scope.launch { focusRequester.requestFocus() }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add block")
                    }
                }
            }
        ) { padding: PaddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )

                if (blocks.isEmpty() && !composing) {
                    Text(
                        text = "Nothing written yet — tap + to start.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(blocks, key = { it.id }) { block ->
                        BlockRow(
                            block = block,
                            onTextCommitted = { newText -> viewModel.editBlockText(block, newText) }
                        )
                    }
                }

                if (composing) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        QuickChipRow(
                            options = settings.quickTags,
                            selected = selectedTag,
                            onSelect = { selectedTag = it }
                        )
                        QuickChipRow(
                            options = settings.quickMoods,
                            selected = selectedMood,
                            onSelect = { selectedMood = it }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BasicTextField(
                                value = draft,
                                onValueChange = { draft = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                            )
                            IconButton(onClick = {
                                viewModel.addBlock(draft, selectedTag, selectedMood)
                                draft = ""
                                selectedTag = null
                                selectedMood = null
                                composing = false
                            }) {
                                Icon(Icons.Filled.Check, contentDescription = "Save block")
                            }
                        }
                    }
                }
            }
        }
    }
}
