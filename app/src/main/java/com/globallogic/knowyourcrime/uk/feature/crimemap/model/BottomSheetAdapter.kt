package com.globallogic.knowyourcrime.uk.feature.crimemap.model

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.globallogic.knowyourcrime.R

class BottomSheetAdapter (private val dataSet: ArrayList<CrimesItem>) :
    RecyclerView.Adapter<BottomSheetAdapter.ViewHolder>() {

@SuppressLint("CutPasteId")
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val category: TextView
    val locationType: TextView
    val month: TextView

    init {
        // Define click listener for the ViewHolder's View.
        category = view.findViewById(R.id.text_view_category)
        locationType = view.findViewById(R.id.text_view_location_type)
        month = view.findViewById(R.id.text_view_month)
    }
}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.bottom_sheet_row, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.category.text = dataSet[position].category
        viewHolder.locationType.text = dataSet[position].location_type
        viewHolder.month.text = dataSet[position].month
    }

    override fun getItemCount() = dataSet.size

}