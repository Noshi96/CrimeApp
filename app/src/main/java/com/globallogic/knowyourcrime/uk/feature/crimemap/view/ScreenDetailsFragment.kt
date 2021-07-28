package com.globallogic.knowyourcrime.uk.feature.crimemap.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.navArgs
import com.globallogic.knowyourcrime.R
import com.globallogic.knowyourcrime.databinding.FragmentScreenDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.roundToInt

class ScreenDetailsFragment : BottomSheetDialogFragment() {

    private val args by navArgs<ScreenDetailsFragmentArgs>()


    private lateinit var _binding: FragmentScreenDetailsBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScreenDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout> =
            BottomSheetBehavior.from(binding.bottomSheetDetails)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.crimeRow.textViewCategory.text = args.crimeDetails.category.replaceFirstChar {
            it.uppercase()
        }.replace('-', ' ')
        binding.crimeRow.textViewLocationType.text = args.crimeDetails.location_type
        binding.crimeRow.textViewMonth.text = args.crimeDetails.month
        val distanceText = "${args.crimeDetails.distanceFromGPS.roundToInt()} m"
        binding.crimeRow.textViewDistance.text = distanceText
        binding.textViewLatitude.text = args.crimeDetails.location.latitude
        binding.textViewLongitude.text = args.crimeDetails.location.longitude
        binding.textViewIdContent.text = args.crimeDetails.id.toString()
        //binding.textViewOutcomeStatusContent.text = args.crimeDetails.outcome_status.category
        binding.textViewWhereContent.text = args.crimeDetails.location.street.name
        binding.crimeRow.imageViewIconBottomSheetRow.setImageResource(returnIconIndexBasedOnCategoryName(args.crimeDetails.category))
    }

    private fun returnIconIndexBasedOnCategoryName(category: String): Int {
        var index = 0
        when(category){
            "anti-social-behaviour" -> index = R.drawable.shout2
            "bicycle-theft" -> index = R.drawable.bicycle
            "burglary" -> index = R.drawable.burglar2
            "criminal-damage-arson" -> index = R.drawable.fire
            "drugs" -> index = R.drawable.meds
            "other-theft" -> index = R.drawable.thief2
            "possession-of-weapons" -> index = R.drawable.gun
            "public-order" -> index = R.drawable.vandalism
            "robbery" -> index = R.drawable.robbery
            "shoplifting" -> index = R.drawable.shoppingcart
            "theft-from-the-person" -> index = R.drawable.onlinerobbery
            "vehicle-crime" -> index = R.drawable.car
            "violent-crime" -> index = R.drawable.violentcrime
            "other-crime" -> index = R.drawable.other
        }
        return index
    }
}