package com.hasnarof.storyapp.ui.auth.register

import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hasnarof.storyapp.R
import com.hasnarof.storyapp.data.Resource
import com.hasnarof.storyapp.data.remote.response.RegisterResponse
import com.hasnarof.storyapp.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAction()
        setObserver()
    }

    private fun setAction() {
        binding?.buttonLogin?.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding?.buttonSignup?.setOnClickListener {
            val name = binding?.textInputName?.text.toString()
            val email = binding?.textInputEmail?.text.toString()
            val password = binding?.textInputPassword?.text.toString()

            when {
                name.isEmpty() -> {
                    binding?.textInputName?.error = getString(R.string.name_should_not_be_empty)
                }
                email.isEmpty() -> {
                    binding?.textInputEmail?.error = getString(R.string.email_should_not_be_empty)
                }
                password.isEmpty() -> {
                    binding?.textInputPassword?.error = getString(R.string.password_should_not_be_empty)
                }
                else -> {
                    viewModel.register(name, email, password)
                }
            }
        }
    }

    private fun setObserver() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding?.progressBar?.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.isSuccessRegister.observe(viewLifecycleOwner) {
            if (it) findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
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