package com.omna.summa.ui.shoppingList

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.omna.summa.databinding.ItemShoppingBinding
import com.omna.summa.domain.model.ShoppingItem

class ItemAdapter(
    private val items: MutableList<ShoppingItem>,
    private val unitAdapter: ArrayAdapter<String>,
    private val dropdownBackground: Drawable?,
    private val onItemsChanged: (List<ShoppingItem>) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemShoppingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShoppingItem) =
            with(binding) {
                edtAmount.setText(item.quantity.toString())
                slcUnit.setText(item.unit)
                etName.setText(item.name)

                if (item.unitPrice != null) {
                    etValor.setText(item.unitPrice.toString())
                    tvTotal.text = "R$ %.2f".format(item.totalPrice())
                    onItemsChanged(items)
                } else {
                    tvTotal.text = "-"
                }

                edtAmount.setSelectAllOnFocus(true)

                edtAmount.addTextChangedListener { text ->
                    item.quantity = text.toString().toIntOrNull() ?: 0
                    tvTotal.text = "R$ %.2f".format(item.totalPrice())
                    onItemsChanged(items)
                }

                etValor.setSelectAllOnFocus(true)

                etValor.addTextChangedListener { text ->
                    item.unitPrice = text.toString().toDoubleOrNull()
                    tvTotal.text = "R$ %.2f".format(item.totalPrice())
                    onItemsChanged(items)
                }

                etName.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus && !etName.text.isNullOrEmpty()) {
                        item.name = etName.text.toString()
                    }
                }

                with(slcUnit) {
                    setAdapter(unitAdapter)
                    setOnClickListener {
                        setDropDownBackgroundDrawable(dropdownBackground)
                        showDropDown()
                    }

                    setOnItemClickListener { _, _, position, _ ->
                        item.unit = unitAdapter.getItem(position).orEmpty()
                    }
                }
            }
    }


    fun addItem(item: ShoppingItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(position: Int): ShoppingItem {
        val removed = items.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }

    fun restoreItem(item: ShoppingItem, position: Int) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun getItems(): List<ShoppingItem> = items

}