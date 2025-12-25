package com.omna.summa.ui.shoppingList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omna.summa.data.local.entity.ShoppingListEntity
import com.omna.summa.data.local.mapper.toDomain
import com.omna.summa.data.local.relation.ShoppingListWithItems
import com.omna.summa.data.repository.ShoppingListRepository
import com.omna.summa.domain.model.ShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllShoppingListViewModel @Inject constructor(private val repository: ShoppingListRepository) : ViewModel() {

    private val _lists =  MutableStateFlow<List<ShoppingList>>(emptyList())
    val lists: StateFlow<List<ShoppingList>> = _lists

    private val _selectList = MutableStateFlow<ShoppingListWithItems?>(null)
    val selectedList: StateFlow<ShoppingListWithItems?> = _selectList

    init {
        loadLists()
    }

    private fun loadLists(){
        viewModelScope.launch {
            repository.getAllLists().collect { entities ->
                _lists.value = entities.map { it.toDomain() }
            }
        }
    }

    fun selectList(listId: Long){
        viewModelScope.launch {
            val listWithItems = repository.getListWithItems(listId)
            _selectList.value = listWithItems
        }
    }

    fun addList(onResult: (Long) -> Unit){
        viewModelScope.launch {
            val item = ShoppingListEntity()
            val generatedId = repository.insertList(item)
            onResult(generatedId)
        }
    }

    fun deleteItem(item: ShoppingListEntity){
        viewModelScope.launch {
            repository.deleteList(item)
        }
    }
}