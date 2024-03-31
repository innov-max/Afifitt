package com.example.afifit.data

class BpmData(
    val bpm: Float = 0.0f,
    override val timestamp: Long = 0L,
    val avgBpm: Float = 0.0f

    ): TimestampedData