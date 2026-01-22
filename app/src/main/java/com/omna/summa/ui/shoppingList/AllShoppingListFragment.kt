package com.omna.summa.ui.shoppingList

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.omna.summa.R
import com.omna.summa.data.remote.BillingEvent
import com.omna.summa.data.remote.BillingManager
import com.omna.summa.databinding.DialogAddListBinding
import com.omna.summa.databinding.FragmentAllShoppingListBinding
import com.omna.summa.domain.model.ShoppingList
import com.omna.summa.ui.MainViewModel
import com.omna.summa.ui.components.showDatePicker
import com.omna.summa.ui.converters.formatPlannedDate
import com.omna.summa.ui.pro.ProDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class AllShoppingListFragment : Fragment() {
    private var _binding: FragmentAllShoppingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AllShoppingListViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    lateinit var billingManager: BillingManager

    var proDialogShown = false

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

        billingManager = BillingManager(requireContext()) {event ->
            when(event){
                is BillingEvent.PurchaseConfirmed ->
                    mainViewModel.onPurchaseConfirmed(event.purchase)

                is BillingEvent.PurchaseError ->
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
        }

        billingManager.startConnection()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                mainViewModel.isPro.collect { isPro ->
                    if(!isPro && !proDialogShown){
                        showProDialog()
                        proDialogShown = true
                    }
                }
            }
        }

        val itemAdapter = AllShoppingListAdapter(onItemClick = { list ->
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
        }, onItemDeleted = { item ->
            viewModel.deleteItem(item)
        }, onDuplicateList = {item ->
            showAddListDialog(item)
        })

        with(binding.rvItem) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.lists.collect { lists ->
                    itemAdapter.submitList(lists)

                    if (lists.isEmpty()){
                        binding.tvEmptyRecyclerView.isVisible = true

                        val query = viewModel.searchQuery.value

                        binding.tvEmptyRecyclerView.text = if (query.isBlank()){
                            getString(R.string.lista_vazia_adicione_itens)
                        }else{
                            getString(R.string.n_o_encontramos_essa_lista)
                        }
                    }else{
                        binding.tvEmptyRecyclerView.isVisible = false
                    }
                }
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
            showAddListDialog(null)
        }
    }

    private fun showAddListDialog(list: ShoppingList?) {
        val dialogBinding = DialogAddListBinding.inflate(layoutInflater)
        val customTitle = layoutInflater.inflate(R.layout.dialog_add_list_title, null)
        var selectedDate: LocalDate? = null

        val dialog =
            MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Summa_MaterialDialog)
                .setCustomTitle(customTitle)
                .setView(dialogBinding.root)
                .setPositiveButton( if (list != null) getString(R.string.duplicar) else getString(R.string.criar), null)
                .setNegativeButton(getString(R.string.cancelar), null)
                .create()

        dialogBinding.etListName.setText(list?.name ?: "")

        dialogBinding.etDate.setText(if(list?.plannedAt != null) formatPlannedDate(list.plannedAt!!) else "")

        dialogBinding.etDate.setOnClickListener {
            showDatePicker(it.context, initialDate = list?.plannedAt ?: LocalDate.now()){ returnedDate ->
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

                if(list != null){
                    viewModel.addList(ShoppingList(name = listName, plannedAt = selectedDate)) { listId ->
                        viewModel.selectList(listId)

                        list.items.forEach { item ->
                            viewModel.insertItem(item.copy(id = 0, isDone = false), listId)
                        }

                        val action = AllShoppingListFragmentDirections
                            .actionAllShoppingListFragmentToShoppingListFragment(
                                listId = listId
                            )
                        findNavController().navigate(action)
                    }

                }else{
                    viewModel.addList(ShoppingList(name = listName, plannedAt = selectedDate)) { listId ->
                        viewModel.selectList(listId)
                        val action = AllShoppingListFragmentDirections
                            .actionAllShoppingListFragmentToShoppingListFragment(
                                listId = listId
                            )
                        findNavController().navigate(action)
                    }
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

    private fun showProDialog(){
        if (parentFragmentManager.findFragmentByTag("ProDialog") == null){
            ProDialog(
                onSubscribeClick = {
                    startProPurchase()
                }
            ).show(parentFragmentManager, "ProDialog")
        }
    }

    private fun startProPurchase(){
        billingManager.launchProSubscription(requireActivity())
    }
}