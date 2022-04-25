package com.hasnarof.storyapp.ui.home

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnarof.storyapp.R
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.databinding.FragmentHomeBinding
import com.hasnarof.storyapp.ui.home.adapter.StoriesAdapter

private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "auth")

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(AuthPreferences.getInstance(context?.dataStore as DataStore<Preferences>))
    }

    private lateinit var storiesAdapter: StoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        setListAdapter()
        setObserver()
    }

    private fun setListAdapter() {
        storiesAdapter = StoriesAdapter()
        binding?.rvStories?.layoutManager = LinearLayoutManager(activity)
        binding?.rvStories?.adapter = storiesAdapter
    }

    private fun setObserver() {
        postponeEnterTransition()

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding?.progressBar?.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.getCurrentUser().observe(viewLifecycleOwner) {
            if(it.token.isNotEmpty()) {
                viewModel.getStories(it.token)
            }
        }

        viewModel.stories.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                binding?.tvNotFound?.visibility = View.GONE
                binding?.rvStories?.visibility = View.VISIBLE
                storiesAdapter.setStories(it)
            } else {
                binding?.rvStories?.visibility = View.GONE
                binding?.tvNotFound?.visibility = View.VISIBLE
            }

            (view?.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_logout -> {
                viewModel.logout()
                true
            }
            else -> {
                item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }



}