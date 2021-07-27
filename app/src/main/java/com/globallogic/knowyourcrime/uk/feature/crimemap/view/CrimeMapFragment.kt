package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.globallogic.knowyourcrime.R
import com.globallogic.knowyourcrime.databinding.CrimeMapBinding
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.BottomSheetAdapter
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CrimesItemMarker
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import kotlin.math.abs

private const val OFFLINE_GPS_LATITUDE = 52.21434496480181
private const val OFFLINE_GPS_LONGITUDE = 0.12568139995511415

class CrimeMapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val viewModel by sharedViewModel<CrimeMapFragmentViewModel>()
    private lateinit var _binding: CrimeMapBinding
    private val binding get() = _binding

    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<CrimesItemMarker>

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var bottomSheetAdapter: BottomSheetAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var gpsMarker: Marker? = null

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.all {
                it.value == true
            }
        }

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

        viewModel.currentGPSPosition.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                withTimeout(1000L) {
                    withContext(Dispatchers.Main) {
                        setGPSMarker(it)
                    }
                }
            }
        }

        loadViewModelData()
        loadGoogleMaps()
        loadBottomSheet()
        loadGPS()
        loadSettings()

        return binding.root
    }

    private fun loadSettings() {
        binding.fabSettings.setOnClickListener {
            val action =
                CrimeMapFragmentDirections.actionCrimeMapFragmentToSettingsScreenFragment()
            viewModel.clearCheckedChipsNamesList()
            findNavController().navigate(action)
        }
    }

    private fun loadGPS() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
        locationCallback = object : LocationCallback() {
            var firstCallback = true
            override fun onLocationResult(locationResult: LocationResult) {
                if (firstCallback) {
                    val location = locationResult.lastLocation
                    setUpCamera(location.latitude, location.longitude, 16.0f)
                    firstCallback = false
                }

                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    viewModel.updateCurrentGPSPosition(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun setGPSMarker(latLng: LatLng) {
        if (gpsMarker != null) {
            gpsMarker?.remove()
        }
        gpsMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("you are here")
        )
    }

    private fun setBottomListForCrimes(crimes: Crimes) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.bottomSheet.recyclerViewBottomSheet.apply {
                    layoutManager = LinearLayoutManager(activity)
                    bottomSheetAdapter =
                        BottomSheetAdapter(crimes as ArrayList<CrimesItem>, googleMap)
                    adapter = bottomSheetAdapter
                }
            }
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
        var chipId = 0

        if (viewModel.resetView) {
            binding.chipGroup.removeAllViews()
        }

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
            chipId++
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
                    binding.chipGroup.checkedChipIds,
                    currentCheckedNames
                )
                viewModel.loadAllCrimes()
            }
        }
    }

    private fun loadViewModelData() {
        viewModel.loadCrimeCategories()
        viewModel.loadChipCategories()
    }

    private fun loadGoogleMaps() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun loadBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheet)

        binding.fab.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            else
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        }

        binding.fabCurrentPosition.setOnClickListener {
            viewModel.currentGPSPosition.value?.let {
                setUpCamera(it.latitude, it.longitude, 22.0f)
            }
        }

        binding.bottomSheet.sortByDistance.setOnClickListener {
            viewModel.sortListByDistance(binding.bottomSheet.sortByDistance.isChecked)

            bottomSheetAdapter.notifyDataSetChanged()
            binding.bottomSheet.recyclerViewBottomSheet.smoothScrollToPosition(0)
        }

        binding.bottomSheet.sortAlphabetically.setOnClickListener {
            viewModel.sortListAlphabetically(binding.bottomSheet.sortAlphabetically.isChecked)

            bottomSheetAdapter.notifyDataSetChanged()
            binding.bottomSheet.recyclerViewBottomSheet.smoothScrollToPosition(0)
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        initClusterManager()
        loadCrimesToMapBasedOnCamera(0.006, 0.006, 0.01f)
        initFusedLocationClient()
    }

    private fun loadCrimesToMapBasedOnCamera(
        latOffset: Double,
        lngOffset: Double,
        zoomOffset: Float
    ) {
        var oldPositionLat = .0
        var oldPositionLng = .0
        var oldZoom = .0f

        googleMap.setOnCameraIdleListener {
            viewModel.updateCurrentCameraPosition(
                googleMap.projection.visibleRegion.latLngBounds,
                googleMap.cameraPosition.target.latitude,
                googleMap.cameraPosition.target.longitude
            )

            val distanceLat = abs(oldPositionLat - googleMap.cameraPosition.target.latitude)
            val distanceLng = abs(oldPositionLng - googleMap.cameraPosition.target.longitude)
            val differenceZoom = abs(oldZoom - googleMap.cameraPosition.zoom)

            if ((distanceLat > latOffset || distanceLng > lngOffset || differenceZoom > zoomOffset)) {
                viewModel.loadAllCrimes()
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

        clusterManager.setOnClusterItemClickListener {

            val crimesItem: CrimesItem = viewModel.getCrimesItemById(it.crimeId) ?: CrimesItem()
            val action =
                CrimeMapFragmentDirections.actionCrimeMapFragmentToScreenDetailsFragment(
                    crimesItem
                )
            val offsetLatSub = 0.00005443289f
            val offsetLongSub = 0.00000268221f
            val latLng = LatLng(
                crimesItem.location.latitude.toDouble() - offsetLatSub,
                crimesItem.location.longitude.toDouble() - offsetLongSub
            )

            val cameraPosition = CameraPosition.Builder()
                .target(latLng).zoom(22.0f).build()

            googleMap.uiSettings.isScrollGesturesEnabled = false
            googleMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        googleMap.uiSettings.isScrollGesturesEnabled = true
                        view?.findNavController()?.navigate(action)
                    }

                    override fun onCancel() {
                        googleMap.uiSettings.setAllGesturesEnabled(true)
                    }
                })

            true
        }
    }

    private fun setUpCamera(latitude: Double, longitude: Double, zoom: Float) {
        val latLng = LatLng(latitude, longitude)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun initFusedLocationClient() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            viewModel.updateCurrentGPSPosition(it.latitude, it.longitude)
        }
        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (!isLocationEnabled() || !checkLocationPermission()) {
                permissionRequestLauncher.launch(PERMISSIONS)
            }
        }
        fusedLocationClient.lastLocation.addOnFailureListener {
            viewModel.updateCurrentGPSPosition(OFFLINE_GPS_LATITUDE, OFFLINE_GPS_LONGITUDE)
            setUpCamera(OFFLINE_GPS_LATITUDE, OFFLINE_GPS_LONGITUDE, 16.0f)
        }
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            ||
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun moveToCurrentPosition() {
        googleMap.setOnCameraIdleListener {
            viewModel.currentGPSPosition.value?.let {
                viewModel.updateCurrentCameraPosition(
                    googleMap.projection.visibleRegion.latLngBounds,
                    it.latitude,
                    it.longitude
                )
            }
        }
    }

}