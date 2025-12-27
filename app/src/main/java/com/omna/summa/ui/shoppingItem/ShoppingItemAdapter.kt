package com.omna.summa.ui.shoppingItem

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.omna.summa.databinding.ItemShoppingBinding
import com.omna.summa.domain.model.ShoppingItem

class ShoppingItemAdapter(
    private val items: MutableList<ShoppingItem>,
    private val unitAdapter: ArrayAdapter<String>,
    private val dropdownBackground: Drawable?,
    private val onAmountChanged: () -> Unit,
    private val onItemChanged: (ShoppingItem) -> Unit
) : RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder>() {
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
                etName.setText(item.name)
                edtAmount.setText(item.quantity.toString())
                slcUnit.setText(item.unit, false)
                etValor.setText(item.unitPrice?.toString() ?: "")
                tvTotal.text = if (item.unitPrice != null) "R$ %.2f".format(item.totalPrice()) else "-"

                edtAmount.setSelectAllOnFocus(true)
                etValor.setSelectAllOnFocus(true)

                val focusListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val updatedItem = item.copy(
                            name = etName.text.toString(),
                            quantity = edtAmount.text.toString().toDoubleOrNull() ?: 0.0,
                            unit = slcUnit.text.toString(),
                            unitPrice = etValor.text.toString().replace(",", ".").toDoubleOrNull()
                        )
                        onItemChanged(updatedItem)
                    }
                }

                etName.onFocusChangeListener = focusListener
                edtAmount.onFocusChangeListener = focusListener
                etValor.onFocusChangeListener = focusListener

                edtAmount.addTextChangedListener { text ->
                    item.quantity = text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                    tvTotal.text = "R$ %.2f".format(item.totalPrice())
                    onAmountChanged()
                }

                edtAmount.setOnEditorActionListener { v, actionId, event ->
                    if(actionId == EditorInfo.IME_ACTION_NEXT){
                        etValor.requestFocus()
                        etValor.selectAll()
                        true
                    }else false
                }

                etValor.addTextChangedListener { text ->
                    item.unitPrice = text.toString().replace(",", ".").toDoubleOrNull()
                    tvTotal.text = "R$ %.2f".format(item.totalPrice())
                    onAmountChanged()
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

    fun getItemByPositon(position: Int): ShoppingItem = items[position]

    fun updateItems(newItems: List<ShoppingItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

}