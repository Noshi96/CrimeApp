package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.globallogic.knowyourcrime.R
import com.globallogic.knowyourcrime.databinding.CrimeMapBinding
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import com.google.android.material.chip.Chip
import org.koin.android.ext.android.inject

class CrimeMapFragment : Fragment(R.layout.crime_map) {

    private val viewModel: CrimeMapFragmentViewModel by inject()
    private lateinit var _binding: CrimeMapBinding
    private val binding get() = _binding
    private lateinit var chipsList: MutableList<Chip>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.crimeCategories.observe(viewLifecycleOwner) {
            Log.d("CrimeMapFragment", it.toString())
        }

        viewModel.allCrimes.observe(viewLifecycleOwner) {
            Log.d("CrimeMapFragment", it.size.toString())
            Log.d("CrimeMapFragment", it.toString())
        }

        viewModel.loadCrimeCategories()
        viewModel.loadAllCrimes(52.629729, -1.131592)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CrimeMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        chipsList = mutableListOf()

        val categoryList =
            mutableListOf("Arson", "Theft", "Abuse", "Violent", "Crime", "6", "7", "8", "9")
        addChipsToViewAndLoadChipsList(categoryList, container)
        setChipsListenerAndUpdateViewModel(chipsList)

        return root
    }

    private fun addChipsToViewAndLoadChipsList(categoryList: List<String>, container: ViewGroup?) {
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
                viewModel.onSelectedChipChangesSendToViewModel(chip, binding.chipGroup.checkedChipIds, currentCheckedNames)
            }
        }
    }
}