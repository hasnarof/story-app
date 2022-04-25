package com.hasnarof.storyapp.ui.auth.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hasnarof.storyapp.R
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.databinding.FragmentLoginBinding


private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "auth")

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthPreferences.getInstance(context?.dataStore as DataStore<Preferences>))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserver()
        setAction()
    }

    private fun setObserver() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding?.progressBar?.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAction() {
        binding?.buttonSignup?.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding?.buttonLogin?.setOnClickListener {
            val email = binding?.textInputEmail?.text.toString()
            val password = binding?.textInputPassword?.text.toString()

            when {
                email.isEmpty() -> {
                    binding?.textInputEmail?.error = getString(R.string.email_should_not_be_empty)
                }
                password.isEmpty() -> {
                    binding?.textInputPassword?.error = getString(R.string.password_should_not_be_empty)
                }
                else -> {
                    viewModel.login(email, password)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)?.supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}