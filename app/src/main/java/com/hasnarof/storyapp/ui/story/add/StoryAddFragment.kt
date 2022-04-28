package com.hasnarof.storyapp.ui.story.add

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var getFile: File? = null
    private var token: String = ""
    private var location: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoryAddBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setCameraPermission()
        getMyLastLocation()
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

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    location = loc
                } else {
                    Toast.makeText(context, "Location is not found. Try Again", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun setCameraPermission() {
        if (!checkPermission(Manifest.permission.CAMERA)) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.CAMERA] == true -> {
                println(getString(R.string.camera_permission_granted))
            }
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            else -> {
                Toast.makeText(
                    activity,
                    getString(R.string.couldnt_get_permission),
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        }
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
                Toast.makeText(requireActivity(), "Please insert description.", Toast.LENGTH_SHORT)
                    .show()
            }
            location == null -> {
                Toast.makeText(requireActivity(), "Location not found.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
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
                val lat = location?.latitude.toString().toRequestBody("text/plain".toMediaType())
                val lon = location?.longitude.toString().toRequestBody("text/plain".toMediaType())

                viewModel.uploadImage(token, imageMultipart, description, lat, lon)

            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}