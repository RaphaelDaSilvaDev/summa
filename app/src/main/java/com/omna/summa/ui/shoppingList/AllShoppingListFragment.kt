package com.omna.summa.ui.shoppingList

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.omna.summa.R
import com.omna.summa.databinding.DialogAddListBinding
import com.omna.summa.databinding.FragmentAllShoppingListBinding
import com.omna.summa.domain.model.ShoppingList
import com.omna.summa.ui.components.showDatePicker
import com.omna.summa.ui.converters.formatPlannedDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class AllShoppingListFragment : Fragment() {
    private var _binding: FragmentAllShoppingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AllShoppingListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemAdapter = AllShoppingListAdapter(mutableListOf(), onItemClick = { list ->
            viewModel.selectList(list.id)

            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()

            val action = AllShoppingListFragmentDirections
                .actionAllShoppingListFragmentToShoppingListFragment(
                    listId = list.id
                )
            findNavController().navigate(action)
        }, onItemChanged = { item ->
            viewModel.updateList(item)
        })

        with(binding.rvItem) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }

        lifecycleScope.launch {
            viewModel.lists.collect { lists ->
                itemAdapter.updateItems(lists)
            }
        }

        binding.edtSearch.setOnFocusChangeListener{ _, hasFocus ->
            binding.clSearch.isSelected = hasFocus
        }

        binding.edtSearch.addTextChangedListener { text ->
            viewModel.onSearchQueryChanged(text.toString())
        }

        binding.ibClearSearch.setOnClickListener {
            binding.edtSearch.setText("")
            viewModel.onSearchQueryChanged("")
        }

        binding.btnAdd.setOnClickListener {
            showAddListDialog()
        }

        val itemTouchHelp = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition

                if (position == RecyclerView.NO_POSITION) return

                val removedItem = itemAdapter.getItemByPosition(position).copy()

                viewModel.deleteItem(removedItem)

                Snackbar.make(
                    binding.root,
                    getString(R.string.lista_removida),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.desfazer)) {
                        viewModel.addList(removedItem) {}
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

        itemTouchHelp.attachToRecyclerView(binding.rvItem)
    }

    private fun showAddListDialog() {
        val dialogBinding = DialogAddListBinding.inflate(layoutInflater)
        val customTitle = layoutInflater.inflate(R.layout.dialog_add_list_title, null)
        var selectedDate: LocalDate? = null

        val dialog =
            MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Summa_MaterialDialog)
                .setCustomTitle(customTitle)
                .setView(dialogBinding.root)
                .setPositiveButton(getString(R.string.criar), null)
                .setNegativeButton(getString(R.string.cancelar), null)
                .create()

        dialogBinding.etDate.setOnClickListener {
            showDatePicker(it.context){ returnedDate ->
                dialogBinding.etDate.setText(formatPlannedDate(returnedDate))
                selectedDate = returnedDate
            }
        }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val listName = dialogBinding.etListName.text.toString().trim()

                if (listName.isEmpty()) {
                    dialogBinding.etListName.error = getString(R.string.informe_o_nome_da_lista)
                    return@setOnClickListener
                }

                viewModel.addList(ShoppingList(name = listName, plannedAt = selectedDate)) { listId ->
                    viewModel.selectList(listId)
                    val action = AllShoppingListFragmentDirections
                        .actionAllShoppingListFragmentToShoppingListFragment(
                            listId = listId
                        )
                    findNavController().navigate(action)
                }

                dialog.dismiss()
            }
        }

        dialogBinding.etListName.requestFocus()
        dialog.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        dialog.show()
    }
}