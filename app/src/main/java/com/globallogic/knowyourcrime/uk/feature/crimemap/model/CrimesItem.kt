package com.globallogic.knowyourcrime.uk.feature.crimemap.model

data class CrimesItem(
    val category: String,
    val context: String,
    val id: Int,
    val location: Location,
    val location_subtype: String,
    val location_type: String,
    val month: String,
    val outcome_status: OutcomeStatus,
    val persistent_id: String
)