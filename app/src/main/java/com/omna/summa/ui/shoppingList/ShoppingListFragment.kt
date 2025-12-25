package com.omna.summa.ui.shoppingList

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.omna.summa.R
import com.omna.summa.databinding.FragmentShoppingListBinding
import com.omna.summa.domain.model.ShoppingItem

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

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

        val itemAdapter = ItemAdapter(mutableListOf(), menuUnitAdapter, dropdownBg) { items ->
            updateTotal(items)
        }

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

            itemAdapter.addItem(
                ShoppingItem(
                    name = itemName,
                    quantity = itemQuantity,
                    unit = itemUnit,
                    unitPrice = 0.0
                )
            )

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

                if(position == RecyclerView.NO_POSITION) return

                val removedItem = itemAdapter.removeItem(position)

                updateTotal(itemAdapter.getItems())

                Snackbar.make(binding.root, getString(R.string.item_removido), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.desfazer)) {
                        itemAdapter.restoreItem(removedItem, position)
                        updateTotal(itemAdapter.getItems())
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
                val background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_swipe_delete)
                val itemView = viewHolder.itemView

                background?.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

                background?.draw(c)

                deleteIcon?.let{
                    val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconLef = itemView.right - iconMargin - it.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + it.intrinsicHeight

                    it.setBounds(iconLef, iconTop, iconRight, iconBottom)
                    it.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
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