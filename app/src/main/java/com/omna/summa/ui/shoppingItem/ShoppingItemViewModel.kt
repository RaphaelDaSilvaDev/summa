package com.omna.summa.ui.shoppingItem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omna.summa.data.local.mapper.toDomain
import com.omna.summa.data.local.mapper.toEntry
import com.omna.summa.data.repository.ShoppingItemRepository
import com.omna.summa.data.repository.ShoppingListRepository
import com.omna.summa.domain.model.ShoppingItem
import com.omna.summa.domain.model.ShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingItemViewModel @Inject constructor(
    private val repository: ShoppingItemRepository,
    private val listRepository: ShoppingListRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val listId: Long = savedStateHandle["listId"] ?: 0

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    val allItems: StateFlow<List<ShoppingItem>> = _items
    val items: StateFlow<List<ShoppingItem>> = combine(_items, _searchQuery){ items, query ->
        if(query.isBlank()){
            items
        }else{
            items.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadItems()
    }

    private fun loadItems(){
        viewModelScope.launch {
            repository.getItemsByList(listId).collect { entities ->
                _items.value = entities.map { it.toDomain() }
            }
        }
    }

    fun updateItem(item: ShoppingItem){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(item.toEntry(listId))
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

    fun getListById(id: Long, onResult: (ShoppingList) -> Unit ){
        viewModelScope.launch {
            val item = listRepository.getListById(id).toDomain()
            onResult(item)
        }
    }

    fun updateList(item: ShoppingList){
        viewModelScope.launch {
            listRepository.updateList(item.toEntry())
        }
    }

    fun onSearchQueryChanged(newQuery: String){
        _searchQuery.value = newQuery
    }
}