package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val totalProfit: Double,
    val amountTendered: Double = 0.0,
    val changeAmount: Double = 0.0,
    val paymentMethod: String = "Cash"
)

