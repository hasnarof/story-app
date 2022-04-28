package com.hasnarof.storyapp.ui.map

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.hasnarof.storyapp.R
import com.hasnarof.storyapp.databinding.FragmentMapsBinding
import com.hasnarof.storyapp.domain.model.Story
import com.hasnarof.storyapp.ui.home.HomeViewModel
import com.hasnarof.storyapp.ui.home.HomeViewModelFactory


class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireActivity())
    }

    companion object {
        private const val TAG = "MapsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setObserver()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(), R.raw.map_style)
                )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

    }

    private fun setObserver() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding?.progressBar?.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.user.observe(viewLifecycleOwner) {
            viewModel.getStoriesWithLocation(it.token)
        }

        viewModel.stories.observe(viewLifecycleOwner) {
            setMapsLocation(it)
        }
    }

    private fun setMapsLocation(stories: List<Story>) {
        val bounds = LatLngBounds.Builder()

        if (stories.isEmpty()) {
            Toast.makeText(context, getString(R.string.no_location), Toast.LENGTH_SHORT).show()
        }

        stories.map {
            if(it.lat != null && it.lon != null) {
                val position = LatLng(it.lat, it.lon)
                mMap.addMarker(MarkerOptions().position(position).title(it.name))
                bounds.include(position)
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 64))
        }
    }

}