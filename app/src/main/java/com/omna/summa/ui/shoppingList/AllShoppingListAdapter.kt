package com.omna.summa.ui.shoppingList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.omna.summa.R
import com.omna.summa.databinding.ListShoppingBinding
import com.omna.summa.domain.model.ShoppingList
import com.omna.summa.ui.converters.formatCurrencyBR
import com.omna.summa.ui.converters.formatPlannedDate

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
                tvTotal.text = formatCurrencyBR(item.totalPrice)
                tvTagAmount.text = "${completedItems}/${totalItems}"
                if (item.plannedAt != null){
                    tvDate.visibility = View.VISIBLE
                    tvDate.text = formatPlannedDate(item.plannedAt!!)
                }
                else
                    tvDate.visibility = View.INVISIBLE

                val focusListener = View.OnFocusChangeListener { _, hasFocus ->
                    if(!hasFocus){
                        val updatedItem = item.copy(
                            name = etName.text.toString(),
                        )

                        if (updatedItem.name.isEmpty()){
                            etName.error =  getString(itemView.context,R.string.o_nome_da_lista_nao_pode_ser_vazio)
                            return@OnFocusChangeListener
                        }
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