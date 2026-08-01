package com.pashu360.app.core.domain.model

import kotlinx.datetime.LocalDateTime

data class Farm(
    val id: String,
    val ownerName: String,
    val farmName: String,
    val village: String,
    val state: String,
    val expectedHerdSize: Int,
    val createdAt: LocalDateTime
)