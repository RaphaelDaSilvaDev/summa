package com.omna.summa.ui.shoppingList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.omna.summa.databinding.ListShoppingBinding
import com.omna.summa.domain.model.ShoppingList
import java.time.format.DateTimeFormatter

class AllShoppingListAdapter(
    private val items: MutableList<ShoppingList>,
    private val onItemClick: (ShoppingList) -> Unit,
    private val onItemChanged: (ShoppingList) -> Unit
) : RecyclerView.Adapter<AllShoppingListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ListShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(private val binding: ListShoppingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShoppingList) =
            with(binding) {
                val totalItems = item.items.size
                val completedItems = item.items.count { item -> item.unitPrice != null && item.unitPrice!! > 0 && item.quantity > 0}
                etName.setText(item.name)
                tvTotal.text = item.totalPrice.toString()
                tvTagAmount.text = "${completedItems}/${totalItems}"
                tvDate.text = item.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                val focusListener = View.OnFocusChangeListener { _, hasFocus ->
                    if(!hasFocus){
                        val updatedItem = item.copy(
                            name = etName.text.toString(),
                        )
                        onItemChanged(updatedItem)
                    }
                }

                etName.onFocusChangeListener = focusListener

                etName.setOnEditorActionListener { v, actionId, event ->
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        etName.clearFocus()
                        true
                    }else false
                }

                root.setOnClickListener { onItemClick(item) }
            }
    }

    fun updateItems(newItems: List<ShoppingList>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItemByPosition(position: Int): ShoppingList{
        return items[position]
    }
}