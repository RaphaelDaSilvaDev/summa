package com.omna.summa.ui.shoppingItem

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.omna.summa.R
import com.omna.summa.databinding.ItemShoppingBinding
import com.omna.summa.domain.model.ShoppingItem
import com.omna.summa.ui.converters.formatCurrencyBR
import com.omna.summa.ui.converters.formatQuantity
import com.omna.summa.ui.converters.parseCurrencyBRToCents

class ShoppingItemAdapter(
    private val items: MutableList<ShoppingItem>,
    private val unitAdapter: ArrayAdapter<String>,
    private val dropdownBackground: Drawable?,
    private val onAmountChanged: () -> Unit,
    private val onItemChanged: (ShoppingItem) -> Unit,
    private val onRemoveItem: (ShoppingItem) -> Unit,
    private val onItemInsert: (ShoppingItem) -> Unit
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

    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(private val binding: ItemShoppingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isUpdating = false

        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: ShoppingItem) =
            with(binding) {
                chkIsDone.setOnCheckedChangeListener(null)

                etName.setText(item.name)
                edtAmount.setText(formatQuantity(item.quantity))
                slcUnit.setText(item.unit, false)
                etValor.setText(formatCurrencyBR(item.unitPrice ?: 0L))
                tvTotal.text = formatCurrencyBR(item.totalPriceInCents())
                chkIsDone.isChecked = item.isDone

                edtAmount.setSelectAllOnFocus(true)
                etValor.setSelectAllOnFocus(true)

                val focusListener = View.OnFocusChangeListener { view, hasFocus ->
                    if(view.id == edtAmount.id || view.id == slcUnit.id){
                        clQuantity.isSelected = hasFocus
                    }

                    if (!hasFocus) {

                        if (etName.text.toString().isEmpty()){
                            etName.error = itemView.context.getString(R.string.nome_da_lista)
                            return@OnFocusChangeListener
                        }

                        val updatedItem = item.copy(
                            name = etName.text.toString().trim(),
                            quantity = edtAmount.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0,
                            unit = slcUnit.text.toString(),
                            unitPrice = parseCurrencyBRToCents(etValor.text.toString())
                        )

                        edtAmount.setText(formatQuantity(updatedItem.quantity))

                        onItemChanged(updatedItem)
                    }
                }

                etName.onFocusChangeListener = focusListener
                edtAmount.onFocusChangeListener = focusListener
                etValor.onFocusChangeListener = focusListener

                edtAmount.addTextChangedListener { text ->
                    item.quantity = text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                    tvTotal.text = formatCurrencyBR(item.totalPriceInCents())
                    onAmountChanged()
                }

                edtAmount.setOnEditorActionListener { _, actionId, _ ->
                    if(actionId == EditorInfo.IME_ACTION_NEXT){
                        etValor.requestFocus()
                        etValor.selectAll()
                        true
                    }else false
                }

                val currencyWatcher = object : TextWatcher {

                    override fun afterTextChanged(s: Editable?) {
                        if (isUpdating) return
                        isUpdating = true

                        val clean = s
                            ?.toString()
                            ?.replace("[^0-9]".toRegex(), "")
                            ?: ""

                        val cents = clean.toLongOrNull() ?: 0L

                        val formatted = formatCurrencyBR(cents)

                        if (formatted != s.toString()) {
                            etValor.setText(formatted)
                            etValor.setSelection(formatted.length)
                        }

                        item.unitPrice = cents
                        tvTotal.text = formatCurrencyBR(item.totalPriceInCents())
                        onAmountChanged()

                        isUpdating = false
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }

                etValor.removeTextChangedListener(currencyWatcher)
                etValor.setText(formatCurrencyBR(item.unitPrice ?: 0L))
                etValor.addTextChangedListener(currencyWatcher)

                etValor.setOnEditorActionListener { v, actionId, _ ->
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        etValor.clearFocus()

                        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)

                        true
                    }else false
                }

                chkIsDone.setOnClickListener {
                    val isChecked = (it as CheckBox).isChecked
                    if(isChecked != item.isDone){
                        onItemChanged(item.copy(isDone = isChecked))
                    }
                }

                ibMenu.setOnClickListener { view ->
                    val popup = PopupMenu(view.context, ibMenu, Gravity.RIGHT, 0, R.style.CustomPopupMenu)
                    popup.menuInflater.inflate(R.menu.item_menu, popup.menu)

                    popup.setOnMenuItemClickListener { menuItem ->
                        when(menuItem.itemId){
                            R.id.itDelete -> {
                                val removedItem = item.copy()

                                onRemoveItem(removedItem)

                                Snackbar.make(binding.root, getString(view.context,R.string.desfazer), Snackbar.LENGTH_LONG)
                                    .setAction(getString(view.context,R.string.desfazer)) {
                                        onItemInsert(removedItem)
                                    }
                                    .setActionTextColor(ContextCompat.getColor(view.context, R.color.red))
                                    .show()

                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }

                with(slcUnit) {
                    setAdapter(unitAdapter)
                    setOnTouchListener { _,_ ->
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(windowToken, 0)

                        setDropDownBackgroundDrawable(dropdownBackground)
                        showDropDown()
                        true
                    }

                    setOnItemClickListener { _, _, position, _ ->
                        item.unit = unitAdapter.getItem(position).orEmpty()
                        onItemChanged(item)
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