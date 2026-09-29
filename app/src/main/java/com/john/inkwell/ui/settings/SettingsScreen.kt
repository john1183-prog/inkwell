package com.john.inkwell.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.john.inkwell.data.FontPairing
import com.john.inkwell.data.UserPreferences
import com.john.inkwell.data.drive.DriveSyncManager
import com.john.inkwell.ui.theme.AccentPalette
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    driveSyncManager: DriveSyncManager,
    onSignInClick: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(preferences, driveSyncManager)
    )
    val settings by viewModel.settings.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))
        }

        item { SectionLabel("Accent color") }
        item {
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                AccentPalette.forEachIndexed { index, color ->
                    val selected = index == settings.accentIndex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = CircleShape
                            )
                            .clickable { viewModel.setAccentIndex(index) }
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item { SectionLabel("Font pairing") }
        item {
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                FontPairing.entries.forEach { pairing ->
                    FilterChip(
                        selected = pairing == settings.fontPairing,
                        onClick = { viewModel.setFontPairing(pairing) },
                        label = { Text(pairing.label) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item { SectionLabel("Paper grain intensity") }
        item {
            Slider(
                value = settings.grainIntensity,
                onValueChange = { viewModel.setGrainIntensity(it) },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item { SectionLabel("Quick tags") }
        item {
            ChipManager(
                items = settings.quickTags,
                onAdd = viewModel::addTag,
                onRemove = viewModel::removeTag
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item { SectionLabel("Quick moods") }
        item {
            ChipManager(
                items = settings.quickMoods,
                onAdd = viewModel::addMood,
                onRemove = viewModel::removeMood
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item { SectionLabel("Privacy") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Require biometric/PIN lock", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Off by default so opening Inkwell stays instant.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Switch(checked = settings.lockEnabled, onCheckedChange = { viewModel.setLockEnabled(it) })
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item { SectionLabel("Google Drive sync") }
        item {
            val email = settings.driveEmail ?: viewModel.signedInEmail
            if (email == null) {
                Text(
                    "Not signed in. Your entries stay on-device only.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(onClick = onSignInClick) { Text("Sign in with Google") }
            } else {
                Text("Signed in as $email", style = MaterialTheme.typography.bodyLarge)
                settings.lastSyncedAt?.let {
                    Text(
                        "Last synced: ${formatSyncTime(it)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row(modifier = Modifier.padding(top = 12.dp)) {
                    Button(onClick = { viewModel.sync() }, modifier = Modifier.padding(end = 8.dp)) {
                        Text(if (syncState is SyncUiState.Syncing) "Syncing…" else "Sync now")
                    }
                    OutlinedButton(onClick = { viewModel.signOut() }) {
                        Text("Sign out")
                    }
                }
                (syncState as? SyncUiState.Done)?.let {
                    Text(
                        it.message,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ChipManager(items: Set<String>, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    var draft by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        items.forEach { item ->
            FilterChip(
                selected = true,
                onClick = { onRemove(item) },
                label = { Text(item) },
                trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove $item", modifier = Modifier.size(16.dp)) },
                modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
            )
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = draft,
            onValueChange = { draft = it },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp)
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = {
            onAdd(draft)
            draft = ""
        }) { Text("Add") }
    }
}

private fun formatSyncTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
    return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(formatter)
}
