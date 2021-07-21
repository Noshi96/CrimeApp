package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.globallogic.knowyourcrime.R
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import org.koin.android.ext.android.inject

class CrimeMapFragment : Fragment(R.layout.crime_map) {

    private val viewModel: CrimeMapFragmentViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadCrimeCategories()
        viewModel.loadAllCrimes(52.629729, -1.131592)

        viewModel.crimeCategories.observe(viewLifecycleOwner) {
            Log.d("CrimeMapFragment", it.toString())
        }

        viewModel.allCrimes.observe(viewLifecycleOwner) {
            Log.d("CrimeMapFragment", it.size.toString())
            Log.d("CrimeMapFragment", it.toString())
        }
    }
}