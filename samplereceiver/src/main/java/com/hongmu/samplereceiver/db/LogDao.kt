package com.hongmu.samplereceiver.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LogDao {
    @Insert
    fun insert(log: MyLog)

    @Query("SELECT * FROM tb_log")
    fun getAll(): LiveData<List<MyLog>>

    @Query("Delete FROM tb_log")
    fun deleteAll()
}