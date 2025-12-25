package com.omna.summa.ui.shoppingList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.omna.summa.R
import com.omna.summa.databinding.FragmentAllShoppingListBinding
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
                .actionAllShoppingListFragmentToShoppingListFragment(listId = list.id)
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
            viewModel.addList{ listId ->
                viewModel.selectList(listId)
                val action = AllShoppingListFragmentDirections
                    .actionAllShoppingListFragmentToShoppingListFragment(listId = listId)
                findNavController().navigate(action)
            }
        }
    }
}