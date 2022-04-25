package com.hasnarof.storyapp.ui.story.add

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hasnarof.storyapp.R
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.databinding.FragmentStoryAddBinding
import com.hasnarof.storyapp.helper.reduceFileImage
import com.hasnarof.storyapp.helper.rotateBitmap
import com.hasnarof.storyapp.helper.uriToFile
import com.hasnarof.storyapp.ui.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "auth")

class StoryAddFragment : Fragment() {

    private var _binding: FragmentStoryAddBinding? = null
    private val binding get() = _binding
    private val viewModel: StoryAddViewModel by viewModels {
        StoryAddViewModelFactory(AuthPreferences.getInstance(context?.dataStore as DataStore<Preferences>))
    }

    private var getFile: File? = null
    private var token: String = ""

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoryAddBinding.inflate(layoutInflater)

        setPermission()

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListenerFromCamera()
        setObserver()
        setAction()
    }

    private fun setObserver() {
        viewModel.getCurrentUser().observe(viewLifecycleOwner) {
            token = it.token
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding?.progressBar?.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.isSuccessUpload.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_storyAddFragment_to_homeFragment)
        }
    }

    private fun setAction() {
        binding?.buttonCamera?.setOnClickListener {
            startCamera()
        }

        binding?.buttonGallery?.setOnClickListener {
            startGallery()
        }

        binding?.buttonUpload?.setOnClickListener {
            uploadImage()
        }
    }

    private fun setPermission() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                activity as Activity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    activity,
                    "Couldn't get permission.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(activity as Context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        findNavController().navigate(R.id.action_storyAddFragment_to_cameraFragment)
    }

    private fun setListenerFromCamera() {
        setFragmentResultListener("take_photo") { key, bundle ->
            val myFile = bundle.getSerializable("picture") as File
            val isBackCamera = bundle.getBoolean("isBackCamera")

            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )

            getFile = myFile
            binding?.image?.setImageBitmap(result)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, requireActivity())
            getFile = myFile
            binding?.image?.setImageURI(selectedImg)
        }
    }

    private fun uploadImage() {
        val description = binding?.tieDescription?.text.toString()
        when {
            getFile == null -> {
                Toast.makeText(requireActivity(), "Please insert image.", Toast.LENGTH_SHORT).show()
            }
            description.isEmpty() -> {
                Toast.makeText(requireActivity(), "Please insert description.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val file = reduceFileImage(getFile as File)

                val description = description.toRequestBody("text/plain".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )

                viewModel.uploadImage(token, imageMultipart, description)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showSystemUI()
    }

}