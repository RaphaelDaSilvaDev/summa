package com.omna.summa.ui.shoppingList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omna.summa.data.local.entity.ShoppingListEntity
import com.omna.summa.databinding.ListShoppingBinding
import com.omna.summa.domain.model.ShoppingList
import java.time.format.DateTimeFormatter

class AllShoppingListAdapter(
    private val items: MutableList<ShoppingList>,
    private val onItemClick: (ShoppingList) -> Unit
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
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(private val binding: ListShoppingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShoppingList, onItemClick: (ShoppingList) -> Unit) =
            with(binding) {
                val totalItems = item.items.size
                val completedItems = item.items.count { item -> item.unitPrice != null && item.unitPrice!! > 0 && item.quantity > 0}
                tvTotal.text = item.totalPrice.toString()
                tvTagAmount.text = "${completedItems}/${totalItems}"
                tvDate.text = item.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                root.setOnClickListener { onItemClick(item) }
            }
    }

    fun updateItems(newItems: List<ShoppingList>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}