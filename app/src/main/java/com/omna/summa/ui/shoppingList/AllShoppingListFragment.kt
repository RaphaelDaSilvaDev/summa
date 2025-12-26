package com.omna.summa.ui.shoppingList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.omna.summa.R
import com.omna.summa.data.local.entity.ShoppingListEntity
import com.omna.summa.databinding.DialogAddListBinding
import com.omna.summa.databinding.FragmentAllShoppingListBinding
import com.omna.summa.domain.model.ShoppingList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
            val action = AllShoppingListFragmentDirections
                .actionAllShoppingListFragmentToShoppingListFragment(listId = list.id, listName = list.name)
            findNavController().navigate(action)
        })

        with(binding.rvItem){
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }

        lifecycleScope.launch {
            viewModel.lists.collect { lists ->
                itemAdapter.updateItems(lists)
            }
        }

        binding.btnAdd.setOnClickListener {
            showAddListDialog()
        }
    }

    private fun showAddListDialog(){
        val dialogBinding = DialogAddListBinding.inflate(layoutInflater)
        val customTitle = layoutInflater.inflate(R.layout.dialog_add_list_title, null)

        val dialog = MaterialAlertDialogBuilder(requireContext(),  R.style.ThemeOverlay_Summa_MaterialDialog)
            .setCustomTitle(customTitle)
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.criar), null)
            .setNegativeButton(getString(R.string.cancelar), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val listName = dialogBinding.etListName.text.toString().trim()

                if(listName.isEmpty()){
                    dialogBinding.etListName.error = getString(R.string.informe_o_nome_da_lista)
                    return@setOnClickListener
                }

                viewModel.addList(ShoppingList(name = listName)){ listId ->
                    viewModel.selectList(listId)
                    val action = AllShoppingListFragmentDirections
                        .actionAllShoppingListFragmentToShoppingListFragment(listId = listId, listName = listName)
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