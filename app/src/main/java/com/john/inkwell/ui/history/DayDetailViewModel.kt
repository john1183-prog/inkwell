package com.john.inkwell.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.john.inkwell.data.Block
import com.john.inkwell.data.InkwellRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DayDetailViewModel(private val repository: InkwellRepository, date: String) : ViewModel() {
    val blocks: StateFlow<List<Block>> = repository.blocksForDate(date)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun editBlockText(block: Block, newText: String) {
        val trimmed = newText.trim()
        if (trimmed.isEmpty() || trimmed == block.text) return
        viewModelScope.launch { repository.editBlock(block, newText = trimmed) }
    }
}

class DayDetailViewModelFactory(private val repository: InkwellRepository, private val date: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DayDetailViewModel(repository, date) as T
    }
}
