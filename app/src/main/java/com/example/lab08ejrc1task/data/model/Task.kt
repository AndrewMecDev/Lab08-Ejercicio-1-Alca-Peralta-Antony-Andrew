package com.example.lab08ejrc1task.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// La anotación @Entity le dice a Room que esta clase representa una tabla en la base de datos.
@Entity(tableName = "tasks")
data class Task(
    // @PrimaryKey define la clave primaria de la tabla.
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // Genera un ID único para cada tarea.
    val title: String,
    val description: String?,
    val isCompleted: Boolean = false,
    val priority: Int = 2, // 1: Alta, 2: Media, 3: Baja
    val dueDate: Long? = null, // Fecha de vencimiento en milisegundos para notificaciones
    val creationDate: Long = System.currentTimeMillis(),
    var userId: String? = null // Para asociar la tarea a un usuario en Firestore
) {
    // Firestore requiere un constructor sin argumentos para poder deserializar los objetos.
    constructor() : this(
        id = UUID.randomUUID().toString(),
        title = "",
        description = null,
        isCompleted = false,
        priority = 2,
        dueDate = null,
        creationDate = System.currentTimeMillis(),
        userId = null
    )
}