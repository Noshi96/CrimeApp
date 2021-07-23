package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.globallogic.knowyourcrime.R
import com.globallogic.knowyourcrime.databinding.CrimeMapBinding
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.BottomSheetAdapter
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class CrimeMapFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: CrimeMapFragmentViewModel by inject()
    private lateinit var _binding: CrimeMapBinding
    private val binding get() = _binding

    private lateinit var googleMap: GoogleMap

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

/*        val appBarLayout = binding.appbar
        val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })
        params.behavior = behavior*/

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
                googleMap.clear()
            }

            crimes.forEach { crime ->
                val latLng =
                    LatLng(
                        crime.location.latitude.toDouble(),
                        crime.location.longitude.toDouble()
                    )
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.marker_image)
                )

                withContext(Dispatchers.Main) {
                    googleMap.addMarker(markerOptions)
                }
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

        val london = LatLng(51.52830802068529, -0.13734309192562905)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(london))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(london, 16.0f))
    }
}