package com.example.lab08ejrc1task.data.repository

import com.example.lab08ejrc1task.data.local.TaskDao
import com.example.lab08ejrc1task.data.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // El Flow de Room es la fuente de verdad para la UI.
    val allTasks = taskDao.getAllTasks()

    // Función para añadir una tarea.
    // Primero la guarda en Firestore, luego en la base de datos local.
    suspend fun insertTask(task: Task) {
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val taskWithUser = task.copy(userId = userId)
                // Guarda en Firestore
                firestore.collection("tasks").document(taskWithUser.id).set(taskWithUser).await()
                // Guarda en Room
                taskDao.insertTask(taskWithUser)
            }
        }
    }

    // Función para actualizar una tarea.
    suspend fun updateTask(task: Task) {
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Actualiza en Firestore
                firestore.collection("tasks").document(task.id).set(task).await()
                // Actualiza en Room
                taskDao.updateTask(task)
            }
        }
    }

    // Función para eliminar una tarea.
    suspend fun deleteTask(task: Task) {
        withContext(Dispatchers.IO) {
            // Elimina de Firestore
            firestore.collection("tasks").document(task.id).delete().await()
            // Elimina de Room
            taskDao.deleteTask(task)
        }
    }

    // Función para sincronizar datos desde Firestore a Room.
    // Útil cuando el usuario inicia sesión en un nuevo dispositivo.
    suspend fun syncTasks() {
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@withContext
            try {
                val snapshot = firestore.collection("tasks")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val tasksFromFirestore = snapshot.toObjects(Task::class.java)
                tasksFromFirestore.forEach { task ->
                    taskDao.insertTask(task)
                }
            } catch (e: Exception) {
                // Manejar error de sincronización (ej: sin conexión)
                e.printStackTrace()
            }
        }
    }
}
