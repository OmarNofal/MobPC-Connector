package com.omar.pcconnector.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface WorkerDao {

    @Insert
    suspend fun insertWorker(worker: WorkerEntity)

    @Query("SELECT workerId FROM WorkerEntity")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM WorkerEntity WHERE workerId = :id")
    suspend fun deleteWork(id: String)

    @Query("SELECT * FROM WorkerEntity WHERE workerId = :id")
    suspend fun getById(id: String): WorkerEntity

    @Update
    suspend fun updateWorker(worker: WorkerEntity)

    @Query("SELECT * FROM WorkerEntity WHERE workerStatus IN ('RUNNING', 'STARTING')")
    fun getActiveWorkersFlow(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM WorkerEntity")
    fun getAllWorkersFlow(): Flow<List<WorkerEntity>>

//    @Query("SELECT * FROM WorkerEntity")
//    suspend fun getAllWorkers(): List<WorkerEntity>

    @Query("DELETE FROM WorkerEntity WHERE workerStatus NOT IN ('RUNNING', 'STARTING', 'ENQUEUED')")
    suspend fun deleteNonRunningWorkers()
}