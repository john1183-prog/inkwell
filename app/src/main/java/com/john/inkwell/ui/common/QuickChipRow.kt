package com.john.inkwell.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A horizontally-scrolling row of quick-select chips (the user's saved
 * tags or moods, managed in Settings). Selecting one toggles it; passing
 * null clears the selection. Kept optional in the composer — most days
 * you'll just write and skip this entirely.
 */
@Composable
fun QuickChipRow(
    options: Set<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(if (option == selected) null else option) },
                label = { Text(option) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(),
                modifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}
