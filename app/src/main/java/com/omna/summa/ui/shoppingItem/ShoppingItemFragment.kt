package com.omna.summa.ui.shoppingItem

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.omna.summa.R
import com.omna.summa.databinding.FragmentShoppingListBinding
import com.omna.summa.domain.model.ShoppingItem
import com.omna.summa.ui.components.showDatePicker
import com.omna.summa.ui.converters.formatCurrencyBR
import com.omna.summa.ui.converters.formatPlannedDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class ShoppingItemFragment : Fragment() {

    private val args: ShoppingItemFragmentArgs by navArgs()

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShoppingItemViewModel by viewModels()

    val units = listOf("un", "kg", "g", "L", "ml")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = args.listId

        lifecycleScope.launch {
            viewModel.getListById(id){ item ->
                binding.tvDate.setOnClickListener {
                    showDatePicker(it.context, initialDate = item.plannedAt ?: LocalDate.now()){ returnedDate ->
                        binding.tvDate.text = formatPlannedDate(returnedDate)
                        val updatedItem = item.copy(
                            plannedAt = returnedDate
                        )

                        viewModel.updateList(updatedItem)
                    }
                }

                if(item.name.isNotEmpty()){
                    binding.etPageName.setText(item.name)
                }else{
                    binding.etPageName.setHint(getString(R.string.lista_de_compras))
                }

                if(item.plannedAt != null){
                    binding.tvDate.text = formatPlannedDate(item.plannedAt!!)
                }else{
                    binding.tvDate.text = getString(R.string.adicione_uma_data)
                }
            }
        }

        binding.etPageName.setOnFocusChangeListener {_, hasFocus ->
            if(!hasFocus){
                if(binding.etPageName.text.isNullOrEmpty()){
                    binding.etPageName.error =
                        getString(R.string.o_nome_da_lista_nao_pode_ser_vazio)
                    return@setOnFocusChangeListener
                }

                viewModel.getListById(id) { item ->
                    val updatedItem = item.copy(name = binding.etPageName.text.toString().trim())
                    viewModel.updateList(updatedItem)
                }

            }
        }

        val menuUnitAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, units)

        val dropdownBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)

        val itemAdapter =
            ShoppingItemAdapter(mutableListOf(), menuUnitAdapter, dropdownBg, onAmountChanged = {
                val currentItems = viewModel.items.value
                updateTotal(currentItems)
            }, onItemChanged = { item ->
                viewModel.updateItem(item)
            })

        with(binding.rvItem) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }

        with(binding.slcUnit) {
            setAdapter(menuUnitAdapter)
            setOnClickListener {
                setDropDownBackgroundDrawable(dropdownBg)
                showDropDown()
            }
        }

        lifecycleScope.launch {
            viewModel.items.collect { items ->
                updateTotal(items)

                itemAdapter.updateItems(items)
            }
        }

        with(binding){
            etListName.requestFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etListName, InputMethodManager.SHOW_IMPLICIT)

            etListName.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    edtAmount.requestFocus()
                    true
                } else false
            }

            edtAmount.setOnFocusChangeListener{ _, hasFocus ->
                clQuantity.isSelected = hasFocus
            }

            edtAmount.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnAdd.performClick()
                    etListName.requestFocus()
                    true
                } else false
            }

            slcUnit.setOnFocusChangeListener{ _, hasFocus ->
                clQuantity.isSelected = hasFocus
            }

            slcUnit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnAdd.performClick()
                    etListName.requestFocus()
                    true
                } else false
            }

            btnAdd.setOnClickListener {
                val itemName = etListName.text.toString()
                val itemQuantity = edtAmount.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                val itemUnit = slcUnit.text.toString()

                if (itemName.isEmpty()) {
                    binding.etListName.error = getString(R.string.preencha_o_nome_do_item)
                    return@setOnClickListener
                }

                viewModel.insertItem(
                    ShoppingItem(
                        name = itemName,
                        quantity = itemQuantity,
                        unit = itemUnit,
                        unitPrice = 0L
                    )
                )

                etListName.setText("")
                edtAmount.setText("")
                slcUnit.setText(menuUnitAdapter.getItem(0), false)

                etListName.requestFocus()
            }

            ibBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition

                if (position == RecyclerView.NO_POSITION) return

                val removedItem = itemAdapter.getItemByPositon(position).copy()

                viewModel.deleteItem(removedItem)

                Snackbar.make(binding.root, getString(R.string.item_removido), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.desfazer)) {
                        viewModel.insertItem(removedItem)
                    }
                    .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash)
                val background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_swipe_delete)
                val itemView = viewHolder.itemView

                background?.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )

                background?.draw(c)

                deleteIcon?.let {
                    val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconLef = itemView.right - iconMargin - it.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + it.intrinsicHeight

                    it.setBounds(iconLef, iconTop, iconRight, iconBottom)
                    it.draw(c)
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTotal(items: List<ShoppingItem>) {
        val total = items.sumOf { it.totalPriceInCents() }

        binding.tvTotalGeral.text = formatCurrencyBR(total)
    }
}