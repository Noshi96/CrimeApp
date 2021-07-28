package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.globallogic.knowyourcrime.databinding.FragmentSettingsScreenBinding
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsScreenFragment : Fragment() {

    private val viewModel by sharedViewModel<CrimeMapFragmentViewModel>()
    private lateinit var _binding: FragmentSettingsScreenBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigateToMap()
        setCheckBoxAndEditText()
        validDateEditText()

        binding.htmlPart.text = viewModel.countCrimes()

    }

    private fun navigateToMap() {
        binding.buttonBack.setOnClickListener {
            viewModel.setDataFilteredBy(binding.editTextDateFrom.editText?.text.toString())
            viewModel.resetView = true

            val action =
                SettingsScreenFragmentDirections.actionSettingsScreenFragmentToCrimeMapFragment()
            findNavController().navigate(action)
        }
    }

    private fun setCheckBoxAndEditText() {
        if (binding.checkboxUpToDate.isChecked) {
            binding.editTextDateFrom.editText?.isEnabled = false
            binding.editTextDateFrom.editText?.setText("2021-05")
        } else {
            binding.editTextDateFrom.editText?.setText(viewModel.dateFilteredBy.value.toString())
            binding.editTextDateFrom.editText?.isEnabled = true
        }

        binding.checkboxUpToDate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.editTextDateFrom.editText?.isEnabled = false
                binding.editTextDateFrom.editText?.setText("2021-05")
            } else {
                binding.editTextDateFrom.editText?.isEnabled = true
            }
        }
    }

    private fun validDateEditText() {
        binding.editTextDateFrom.editText?.doAfterTextChanged {

            if (it?.length == 7 && (it.substring(4, 5) == "-" && (it.substring(0, 4)
                    .toInt() <= 2021) && (it.substring(0, 4)
                    .toInt() > 2015) && (it.substring(5, 7).toInt() < 13) && (it.substring(5, 7)
                    .toInt() > 0))
            ) {
                if (it.substring(0, 4).toInt() == 2021) {
                    binding.buttonBack.isEnabled = it.substring(5, 7).toInt() < 6
                } else {
                    binding.buttonBack.isEnabled = true
                }
            } else {
                binding.buttonBack.isEnabled = false
            }
        }
    }

}