package com.omna.summa.ui.shoppingItem

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omna.summa.data.local.mapper.toDomain
import com.omna.summa.data.local.mapper.toEntry
import com.omna.summa.data.repository.ShoppingItemRepository
import com.omna.summa.domain.model.ShoppingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingItemViewModel @Inject constructor(
    private val repository: ShoppingItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val listId: Long = savedStateHandle["listId"] ?: 0

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items

    init {
        loadItems()
    }

    private fun loadItems(){
        viewModelScope.launch {
            repository.getItemsByList(listId).collect { entities ->
                _items.value = entities.map { it.toDomain() }.toList()
            }
        }
    }

    fun updateItem(item: ShoppingItem){
        viewModelScope.launch {
            repository.insetItem(item.toEntry(listId))
        }
    }

    fun insertItem(item: ShoppingItem) {
        viewModelScope.launch {
            val item = item.toEntry(listId)
            repository.insetItem(item)
        }
    }

    fun deleteItem(item: ShoppingItem){
        viewModelScope.launch {
            repository.deleteItem(item.toEntry(listId))
        }
    }
}