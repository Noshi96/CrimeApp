package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.globallogic.knowyourcrime.R
import com.globallogic.knowyourcrime.databinding.CrimeMapBinding
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.BottomSheetAdapter
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CrimesItemMarker
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import kotlin.math.abs

class CrimeMapFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: CrimeMapFragmentViewModel by inject()
    private lateinit var _binding: CrimeMapBinding
    private val binding get() = _binding

    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<CrimesItemMarker>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CrimeMapBinding.inflate(inflater, container, false)

        viewModel.allCrimes.observe(viewLifecycleOwner) {
            setMarkersForCrimes(it)
            setBottomListForCrimes(it)
        }

        viewModel.chipCategories.observe(viewLifecycleOwner) {
            setChipsForChipCategories(it, container)
        }

        loadViewModelData()
        loadGoogleMaps()

        return binding.root
    }

    private fun setBottomListForCrimes(crimes: Crimes) {
        binding.bottomSheet.recyclerViewBottomSheet.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = BottomSheetAdapter(crimes as ArrayList<CrimesItem>)
        }
    }

    private fun setMarkersForCrimes(crimes: Crimes) {
        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.Main) {
                clusterManager.clearItems()
            }
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_image)
            crimes.forEach { crime ->
                withContext(Dispatchers.Main) {
                    clusterManager.addItem(
                        CrimesItemMarker(
                            crime.id,
                            crime.location.latitude.toDouble(),
                            crime.location.longitude.toDouble(),
                            icon,
                            crime.category,
                            crime.location.street.name
                        )
                    )
                }
            }
            withContext(Dispatchers.Main) {
                clusterManager.cluster()
            }
        }
    }

    private fun setChipsForChipCategories(
        chipCategories: MutableList<String>,
        container: ViewGroup?
    ) {
        val chipsList = mutableListOf<Chip>()
        addChipsToViewAndLoadChipsList(chipCategories, container, chipsList)
        setChipsListenerAndUpdateViewModel(chipsList)
    }

    private fun addChipsToViewAndLoadChipsList(
        categoryList: List<String>,
        container: ViewGroup?,
        chipsList: MutableList<Chip>
    ) {
        categoryList.forEach { categoryName ->
            val chip = layoutInflater.inflate(R.layout.chip_item, container, false) as Chip
            chip.text = categoryName
            chip.chipIcon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_sports_baseball_24
            )
            chip.id = View.generateViewId()
            chipsList.add(chip)
            binding.chipGroup.addView(chip)
        }
    }

    private fun setChipsListenerAndUpdateViewModel(chipsList: MutableList<Chip>) {
        chipsList.forEach { chip ->
            chip.setOnClickListener {
                val currentCheckedNames = mutableListOf<String>()
                binding.chipGroup.checkedChipIds.forEach { chipId ->
                    currentCheckedNames.add(binding.chipGroup.findViewById<Chip>(chipId).text.toString())
                }
                viewModel.onSelectedChipChangesSendToViewModel(
                    chip,
                    binding.chipGroup.checkedChipIds,
                    currentCheckedNames
                )
            }
        }
    }

    private fun loadViewModelData() {
        viewModel.loadCrimeCategories()
        viewModel.loadChipCategories()
        viewModel.loadAllCrimes(51.52830802068529, -0.13734309192562905)
    }

    private fun loadGoogleMaps() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        initClusterManager()
        setUpCamera(51.52830802068529, -0.13734309192562905, 16.0f)
        loadCrimesToMapBasedOnCamera(0.006, 0.006, 0.01f, 12.5f)
    }

    private fun loadCrimesToMapBasedOnCamera(
        latOffset: Double,
        lngOffset: Double,
        zoomOffset: Float,
        maxLoadingZoom: Float
    ) {
        var oldPositionLat = .0
        var oldPositionLng = .0
        var oldZoom = .0f

        googleMap.setOnCameraIdleListener {
            val distanceLat = abs(oldPositionLat - googleMap.cameraPosition.target.latitude)
            val distanceLng = abs(oldPositionLng - googleMap.cameraPosition.target.longitude)
            val differenceZoom = abs(oldZoom - googleMap.cameraPosition.zoom)

            if (googleMap.cameraPosition.zoom > maxLoadingZoom &&
                (distanceLat > latOffset || distanceLng > lngOffset || differenceZoom > zoomOffset)
            ) {
                viewModel.loadAllCrimes(
                    googleMap.projection.visibleRegion.latLngBounds,
                    googleMap.cameraPosition.target.latitude,
                    googleMap.cameraPosition.target.longitude
                )

                oldPositionLat = googleMap.cameraPosition.target.latitude
                oldPositionLng = googleMap.cameraPosition.target.longitude
                oldZoom = googleMap.cameraPosition.zoom
            }
        }
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

    private fun setUpCamera(latitude: Double, longitude: Double, zoom: Float) {
        val latLng = LatLng(latitude, longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }
}