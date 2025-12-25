package com.omna.summa.ui.shoppingList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omna.summa.databinding.ListShoppingBinding
import com.omna.summa.domain.model.ShoppingList
import java.time.format.DateTimeFormatter

class AllShoppingItemAdapter (private val items: MutableList<ShoppingList>): RecyclerView.Adapter<AllShoppingItemAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ListShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    class ViewHolder(private val binding: ListShoppingBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: ShoppingList) = with(binding){
            tvTotal.text = item.totalPrice.toString()
            tvTagAmount.text = "${item.items.count { item -> item.quantity > 0 && item.unitPrice != null }}/${item.items.size}"
            tvDate.text = item.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/YYYY"))
        }
    }

    fun addItem(item: ShoppingList){
        items.add(item)
        notifyItemInserted(items.size - 1)
    }
}