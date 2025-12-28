package com.omna.summa.ui.shoppingItem

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.omna.summa.databinding.ItemShoppingBinding
import com.omna.summa.domain.model.ShoppingItem
import com.omna.summa.ui.converters.formatCurrencyBR
import com.omna.summa.ui.converters.formatQuantity

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
                edtAmount.setText(formatQuantity(item.quantity))
                slcUnit.setText(item.unit, false)
                etValor.setText(if (item.unitPrice != null) formatCurrencyBR(item.unitPrice!!) else formatCurrencyBR(0.0))
                tvTotal.text = if (item.unitPrice != null) formatCurrencyBR(item.totalPrice()) else formatCurrencyBR(0.0)

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

                        edtAmount.setText(formatQuantity(updatedItem.quantity))
                        etValor.setText(if (updatedItem.unitPrice != null) formatCurrencyBR(updatedItem.unitPrice!!) else formatCurrencyBR(0.0))

                        onItemChanged(updatedItem)
                    }
                }

                etName.onFocusChangeListener = focusListener
                edtAmount.onFocusChangeListener = focusListener
                etValor.onFocusChangeListener = focusListener

                edtAmount.addTextChangedListener { text ->
                    item.quantity = text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                    tvTotal.text = formatCurrencyBR(item.totalPrice())
                    onAmountChanged()
                }

                edtAmount.setOnEditorActionListener { _, actionId, _ ->
                    if(actionId == EditorInfo.IME_ACTION_NEXT){
                        etValor.requestFocus()
                        etValor.selectAll()
                        true
                    }else false
                }

                etValor.addTextChangedListener { text ->
                    item.unitPrice = text.toString().replace(",", ".").toDoubleOrNull()
                    tvTotal.text = formatCurrencyBR(item.totalPrice())
                    onAmountChanged()
                }

                etValor.setOnEditorActionListener { v, actionId, _ ->
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        etValor.clearFocus()

                        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)

                        true
                    }else false
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