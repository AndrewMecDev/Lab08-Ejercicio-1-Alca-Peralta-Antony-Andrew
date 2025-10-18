package com.example.lab08ejrc1task.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lab08ejrc1task.data.local.TaskDatabase
import com.example.lab08ejrc1task.data.model.Task
import com.example.lab08ejrc1task.data.repository.TaskRepository
import com.example.lab08ejrc1task.util.NotificationWorker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val workManager = WorkManager.getInstance(application)

    val allTasks: StateFlow<List<Task>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)

        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch { repository.syncTasks() }
                }
            }
        } else {
            viewModelScope.launch { repository.syncTasks() }
        }

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insert(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        cancelNotification(task)
        repository.deleteTask(task)
    }

    // Lógica para programar una notificación
    fun scheduleNotification(context: Context, task: Task) {
        // Primero, cancelamos cualquier notificación antigua para esta tarea
        cancelNotification(task)

        val dueDate = task.dueDate ?: return
        val currentTime = System.currentTimeMillis()
        val delay = dueDate - currentTime

        if (delay > 0) {
            val data = Data.Builder()
                .putString(NotificationWorker.KEY_TASK_TITLE, "Recordatorio: ${task.title}")
                .putString(NotificationWorker.KEY_TASK_MESSAGE, task.description ?: "¡Es hora de completar tu tarea!")
                .build()

            val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(task.id) // Usamos el ID de la tarea como Tag
                .build()

            workManager.enqueue(notificationWork)
        }
    }

    // Lógica para cancelar una notificación
    private fun cancelNotification(task: Task) {
        workManager.cancelAllWorkByTag(task.id)
    }
}

