package com.example.lab08ejrc1task.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lab08ejrc1task.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Obtiene todas las tareas y las emite como un Flow.
    // Flow permite que la UI se actualice automáticamente cuando los datos cambian.
    @Query("SELECT * FROM tasks ORDER BY priority ASC, creationDate DESC")
    fun getAllTasks(): Flow<List<Task>>

    // Inserta una nueva tarea. Si ya existe, la reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    // Actualiza una tarea existente.
    @Update
    suspend fun updateTask(task: Task)

    // Elimina una tarea.
    @Delete
    suspend fun deleteTask(task: Task)

    // Elimina una tarea por su ID.
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
}
