package com.omna.summa.ui.shoppingItem

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.omna.summa.R
import com.omna.summa.databinding.FragmentShoppingListBinding
import com.omna.summa.domain.model.ShoppingItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShoppingItemFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShoppingItemViewModel by viewModels()

    val units = listOf("un", "kg", "L")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuUnitAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, units)

        val dropdownBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)

        val itemAdapter = ShoppingItemAdapter(mutableListOf(), menuUnitAdapter, dropdownBg, onAmountChanged = {
            val currentItems = viewModel.items.value
            updateTotal(currentItems)
        }, onItemChanged = {
                item ->
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

        binding.etItem.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_NEXT){
                binding.edtAmount.requestFocus()
                true
            }else false
        }

        binding.edtAmount.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                binding.btnAdd.performClick()
                binding.etItem.requestFocus()
                true
            }else false
        }

        binding.btnAdd.setOnClickListener {
            val itemName = binding.etItem.text.toString()
            val itemQuantity = binding.edtAmount.text.toString().toIntOrNull() ?: 0
            val itemUnit = binding.slcUnit.text.toString()

            if (itemName.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.preencha_o_nome_do_item),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.insertItem(ShoppingItem(
                name = itemName,
                quantity = itemQuantity,
                unit = itemUnit,
                unitPrice = 0.0
            ))

            binding.etItem.setText("")
            binding.edtAmount.setText("")
            binding.slcUnit.setText(menuUnitAdapter.getItem(0), false)
        }

        binding.ibBack.setOnClickListener {
            findNavController().popBackStack()
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
        val total = items.sumOf { it.totalPrice() }

        binding.tvTotalGeral.text = "R$ %.2f".format(total)
    }
}