package com.applligent.formdatarequest.model

data class ApiResponse(
    val message: String,
    val statusCode: Int,
    val success: Boolean,
    val type: Int
)