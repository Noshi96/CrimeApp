package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.globallogic.knowyourcrime.R
import com.globallogic.knowyourcrime.databinding.CrimeMapBinding
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.*
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import com.google.android.material.chip.Chip
import org.koin.android.ext.android.inject

class CrimeMapFragment : Fragment(R.layout.crime_map) {

    private val viewModel: CrimeMapFragmentViewModel by inject()
    private lateinit var _binding: CrimeMapBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CrimeMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.allCrimes.observe(viewLifecycleOwner) {
            Log.d("CrimeMapFragment", it.size.toString())
            Log.d("CrimeMapFragment", it.toString())
        }

        viewModel.chipCategories.observe(viewLifecycleOwner) {
            val chipsList = mutableListOf<Chip>()
            addChipsToViewAndLoadChipsList(it, container, chipsList)
            setChipsListenerAndUpdateViewModel(chipsList)
        }

        viewModel.loadCrimeCategories()
        viewModel.loadChipCategories()
        viewModel.loadAllCrimes(52.629729, -1.131592)

        var crimes = mutableListOf<CrimesItem>()

        val crimesItem1 = CrimesItem(
            category = "Burglary",
            context = "2",
            id = 3,
            location = Location(
                latitude = "latitude",
                longitude = "longitude",
                street = Street(
                    id = 2,
                    name = "name"
                )
            ),
            location_subtype = "4",
            location_type = "Force",
            month = "2021/07",
            outcome_status = OutcomeStatus(
                category = "category",
                date = "date"
            ),
            persistent_id = "8"
        )

        crimes.add(crimesItem1)
        crimes.add(crimesItem1)
        crimes.add(crimesItem1)
        crimes.add(crimesItem1)
        crimes.add(crimesItem1)
        crimes.add(crimesItem1)
        crimes.add(crimesItem1)


        binding.bottomSheet.recyclerViewBottomSheet.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = BottomSheetAdapter(crimes as ArrayList<CrimesItem>)
        }

        return root
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
}