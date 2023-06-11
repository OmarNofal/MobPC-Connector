package com.omar.pcconnector.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface WorkerDao {

    @Insert
    fun insertWorker(worker: WorkerEntity)

    @Query("SELECT * FROM WorkerEntity WHERE workerId = :id")
    fun getById(id: String): WorkerEntity

    @Update
    fun updateWorker(worker: WorkerEntity)

    @Query("SELECT * FROM WorkerEntity WHERE workerStatus IN ('RUNNING', 'STARTING')")
    fun getActiveWorkersFlow(): Flow<List<WorkerEntity>>

    @Query("DELETE FROM WorkerEntity WHERE workerStatus NOT IN ('RUNNING', 'STARTING')")
    fun deleteNonRunningWorkers()
}