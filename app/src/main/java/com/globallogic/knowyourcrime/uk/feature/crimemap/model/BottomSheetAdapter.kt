package com.globallogic.knowyourcrime.uk.feature.crimemap.model

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.globallogic.knowyourcrime.databinding.BottomSheetRowBinding
import com.globallogic.knowyourcrime.uk.feature.crimemap.view.CrimeMapFragmentDirections
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng


class BottomSheetAdapter(
    private val dataSet: ArrayList<CrimesItem>,
    private val googleMap: GoogleMap
) :
    RecyclerView.Adapter<BottomSheetAdapter.ViewHolder>() {

    class ViewHolder(bottomSheetRowBinding: BottomSheetRowBinding) :
        RecyclerView.ViewHolder(bottomSheetRowBinding.root) {
        lateinit var crimesItem: CrimesItem
        val category: TextView
        val locationType: TextView
        val month: TextView
        lateinit var googleMap: GoogleMap

        init {
            category = bottomSheetRowBinding.textViewCategory
            locationType = bottomSheetRowBinding.textViewLocationType
            month = bottomSheetRowBinding.textViewMonth
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            BottomSheetRowBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.crimesItem = dataSet[position]
        viewHolder.category.text = dataSet[position].category.replaceFirstChar {
            it.uppercase()
        }.replace('-', ' ')
        viewHolder.locationType.text = dataSet[position].location_type
        viewHolder.month.text = dataSet[position].month
        viewHolder.googleMap = googleMap


        viewHolder.apply {
            itemView.setOnClickListener {
                val offsetLatSub = 0.00005443289f
                val offsetLongSub = 0.00000268221f
                val action =
                    CrimeMapFragmentDirections.actionCrimeMapFragmentToScreenDetailsFragment(
                        crimesItem
                    )
                val lng = dataSet[position].location.longitude.toDouble() - offsetLongSub
                val lat = dataSet[position].location.latitude.toDouble() - offsetLatSub

                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(lat, lng)).zoom(22.0f).build()

                googleMap.uiSettings.isScrollGesturesEnabled = false
                googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    object : CancelableCallback {
                        override fun onFinish() {
                            googleMap.uiSettings.isScrollGesturesEnabled = true
                            itemView.findNavController().navigate(action)
                        }

                        override fun onCancel() {
                            googleMap.uiSettings.setAllGesturesEnabled(true)
                        }
                    })
            }
        }
    }

    override fun getItemCount() = dataSet.size

}
