package com.hongmu.samplereceiver.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tb_log")
data class MyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: Int,
    val date: Long,
)
