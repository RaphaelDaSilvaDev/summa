package com.omna.summa.ui.shoppingList

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.omna.summa.R
import com.omna.summa.databinding.ListShoppingBinding
import com.omna.summa.domain.model.ShoppingList
import com.omna.summa.ui.components.showDatePicker
import com.omna.summa.ui.converters.formatCurrencyBR
import com.omna.summa.ui.converters.formatPlannedDate
import java.time.LocalDate

class AllShoppingListAdapter(
    private val items: MutableList<ShoppingList>,
    private val onItemClick: (ShoppingList) -> Unit,
    private val onItemChanged: (ShoppingList) -> Unit,
    private val onItemDeleted: (ShoppingList) -> Unit,
    private val onDuplicateList: (ShoppingList) -> Unit
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

    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(private val binding: ListShoppingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShoppingList) =
            with(binding) {
                val totalItems = item.items.size
                val completedItems = item.items.count { item -> item.isDone}
                etName.setText(item.name)
                tvTotal.text = formatCurrencyBR(item.totalPrice)
                tvTagAmount.text = "${completedItems}/${totalItems}"
                if (item.plannedAt != null){
                    tvDate.text = formatPlannedDate(item.plannedAt!!)
                }
                else{
                    tvDate.text = getString(binding.root.context, R.string.adicione_uma_data)
                }

                val focusListener = View.OnFocusChangeListener { _, hasFocus ->
                    if(!hasFocus){
                        val updatedItem = item.copy(
                            name = etName.text.toString().trim()
                        )

                        if (updatedItem.name.isEmpty()){
                            etName.error =  getString(itemView.context,R.string.o_nome_da_lista_nao_pode_ser_vazio)
                            return@OnFocusChangeListener
                        }
                        onItemChanged(updatedItem)

                        val imm = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(itemView.windowToken, 0)
                    }
                }

                etName.onFocusChangeListener = focusListener

                etName.setOnEditorActionListener { _, actionId, _ ->
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        etName.clearFocus()
                        true
                    }else false
                }

                tvDate.setOnClickListener {
                    showDatePicker(it.context, initialDate = item.plannedAt ?: LocalDate.now()){ returnedDate ->
                        tvDate.text = formatPlannedDate(returnedDate)
                        val updatedItem = item.copy(
                            plannedAt = returnedDate
                        )

                        onItemChanged(updatedItem)
                    }
                }

                ibMenu.setOnClickListener { view ->
                    val popup = PopupMenu(view.context, ibMenu, Gravity.RIGHT, 0 , R.style.CustomPopupMenu)
                    popup.menuInflater.inflate(R.menu.list_menu, popup.menu)

                    popup.setOnMenuItemClickListener { menuItem ->
                        when(menuItem.itemId){
                            R.id.itDelete -> {
                                val removedItem = item.copy(isActive = false)
                                onItemChanged(removedItem)

                                Snackbar.make(
                                    binding.root,
                                    getString(view.context,R.string.lista_removida),
                                    Snackbar.LENGTH_LONG
                                )
                                    .setAction(getString(view.context, R.string.desfazer)) {
                                        val returnItem = removedItem.copy(isActive = true)
                                        onItemChanged(returnItem)
                                    }
                                    .addCallback(object : Snackbar.Callback(){
                                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                            super.onDismissed(transientBottomBar, event)

                                            if(event != DISMISS_EVENT_ACTION){
                                                onItemDeleted(removedItem)
                                            }
                                        }
                                    })
                                    .setActionTextColor(ContextCompat.getColor(view.context, R.color.red))
                                    .show()
                                true
                            }
                            R.id.itDuplicate -> {
                                onDuplicateList(item.copy())
                                true
                            }
                            else -> false
                        }
                    }

                    popup.show()
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