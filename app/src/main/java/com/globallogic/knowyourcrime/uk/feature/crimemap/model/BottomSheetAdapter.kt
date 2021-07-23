package com.globallogic.knowyourcrime.uk.feature.crimemap.model

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.globallogic.knowyourcrime.databinding.BottomSheetBinding
import com.globallogic.knowyourcrime.databinding.BottomSheetRowBinding

class BottomSheetAdapter(private val dataSet: ArrayList<CrimesItem>) :
    RecyclerView.Adapter<BottomSheetAdapter.ViewHolder>() {

    @SuppressLint("CutPasteId")
    class ViewHolder(bottomSheetRowBinding: BottomSheetRowBinding) : RecyclerView.ViewHolder(bottomSheetRowBinding.root) {
        val category: TextView
        val locationType: TextView
        val month: TextView
        init {
            // Define click listener for the ViewHolder's View.
            category = bottomSheetRowBinding.textViewCategory
            locationType = bottomSheetRowBinding.textViewLocationType
            month = bottomSheetRowBinding.textViewMonth
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = BottomSheetRowBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.category.text = dataSet[position].category
        viewHolder.locationType.text = dataSet[position].location_type
        viewHolder.month.text = dataSet[position].month
    }

    override fun getItemCount() = dataSet.size

}
