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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
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

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isLocationFinePermissionGranted = false
    private var isLocationCoarsePermissionGranted = false
    private var isCameraPermissionGranted = false

    private val MY_PERMISSIONS_REQUEST_CODE = 123

    companion object {
        private const val TAG = "StoryAddFragment"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

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

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->

            isLocationFinePermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationFinePermissionGranted
            isLocationCoarsePermissionGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: isLocationCoarsePermissionGranted
            isCameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: isCameraPermissionGranted

            if(isLocationFinePermissionGranted || isLocationCoarsePermissionGranted) getMyLastLocation()

        }

        requestPermission()
//        setPermissionCamera()
//        getMyLastLocation()
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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        this.location = location
                    } else {
                        Toast.makeText(context, "Location is not found. Try Again", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().navigateUp()
                    }
                }
        } else {
            requestPermission()
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
//                Log.d(TAG, "${isLocationFinePermissionGranted.toString()} ${isLocationCoarsePermissionGranted.toString()} ${isCameraPermissionGranted.toString()}")
//                requestPermission()
                getMyLastLocation()
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

    private fun requestPermission(){

        isLocationFinePermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isLocationCoarsePermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

//        Log.d(TAG, "${isLocationFinePermissionGranted.toString()} ${isLocationCoarsePermissionGranted.toString()} ${isCameraPermissionGranted.toString()}")

        val permissionRequests : MutableList<String> = ArrayList()

        if (!isLocationFinePermissionGranted){
            permissionRequests.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!isLocationCoarsePermissionGranted){
            permissionRequests.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (!isCameraPermissionGranted){
            permissionRequests.add(Manifest.permission.CAMERA)
        }

        if (permissionRequests.isNotEmpty()){
            permissionLauncher.launch(permissionRequests.toTypedArray())
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showSystemUI()
        requestPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}