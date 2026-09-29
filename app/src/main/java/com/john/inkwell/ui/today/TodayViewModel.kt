package com.john.inkwell.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.john.inkwell.data.Block
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayViewModel(
    private val repository: InkwellRepository,
    val preferences: UserPreferences
) : ViewModel() {

    val todayKey: String = repository.todayKey()

    val blocks: StateFlow<List<Block>> = repository.blocksForDate(todayKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Called when the user taps "+" and submits the new block's text. */
    fun addBlock(text: String, tag: String?, mood: String?) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addBlock(trimmed, tag, mood)
        }
    }

    fun editBlockText(block: Block, newText: String) {
        val trimmed = newText.trim()
        if (trimmed.isEmpty() || trimmed == block.text) return
        viewModelScope.launch {
            repository.editBlock(block, newText = trimmed)
        }
    }
}
