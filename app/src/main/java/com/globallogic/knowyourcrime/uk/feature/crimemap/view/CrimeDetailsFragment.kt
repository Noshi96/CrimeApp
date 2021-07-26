package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.globallogic.knowyourcrime.R
import com.globallogic.knowyourcrime.databinding.DetailsFragmentBinding
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CrimesItemMarker
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import org.koin.android.ext.android.inject

class CrimeDetailsFragment : Fragment(), OnMapReadyCallback {

    private val args by navArgs<CrimeDetailsFragmentArgs>()

    private val viewModel: CrimeMapFragmentViewModel by inject()

    private lateinit var _binding: DetailsFragmentBinding
    private val binding get() = _binding

    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<CrimesItemMarker>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailsFragmentBinding.inflate(inflater, container, false)
        loadGoogleMaps()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.detailsSheet.detailsScreen.crimeRow.textViewCategory.text = args.crimesItem.category
        binding.detailsSheet.detailsScreen.crimeRow.textViewLocationType.text =
            args.crimesItem.location_type
        binding.detailsSheet.detailsScreen.crimeRow.textViewMonth.text = args.crimesItem.month

        binding.detailsSheet.detailsScreen.textViewLatitude.text =
            args.crimesItem.location.latitude

        binding.detailsSheet.detailsScreen.textViewLongitude.text =
            args.crimesItem.location.longitude

        binding.detailsSheet.detailsScreen.textViewIdContent.text = args.crimesItem.id.toString()
/*        binding.detailsSheet.detailsScreen.textViewOutcomeStatusContent.text =
            args.crimesItem.outcome_status.category*/
        binding.detailsSheet.detailsScreen.textViewWhereContent.text =
            args.crimesItem.location.street.name

    }

    private fun loadGoogleMaps() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.details_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        initClusterManager()
        addCrimeItemToCluster()
        setUpCamera(
            args.crimesItem.location.latitude.toDouble(),
            args.crimesItem.location.longitude.toDouble(),
            20.0f
        )
    }

    private fun initClusterManager() {
        clusterManager = ClusterManager(requireActivity(), googleMap)
        googleMap.setOnCameraIdleListener(clusterManager)

        clusterManager.renderer = CrimesItemMarkerRenderer(
            requireActivity(),
            googleMap,
            clusterManager
        )
    }

    private fun addCrimeItemToCluster() {
        val icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_image)
        clusterManager.clearItems()
        clusterManager.addItem(
            CrimesItemMarker(
                args.crimesItem.id,
                args.crimesItem.location.latitude.toDouble(),
                args.crimesItem.location.longitude.toDouble(),
                icon,
                args.crimesItem.category,
                args.crimesItem.location.street.name
            )
        )
        clusterManager.cluster()
    }

    private fun setUpCamera(latitude: Double, longitude: Double, zoom: Float) {
        val latLng = LatLng(latitude, longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }
}