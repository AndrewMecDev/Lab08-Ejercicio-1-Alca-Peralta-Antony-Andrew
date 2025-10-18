package com.example.lab08ejrc1task.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lab08ejrc1task.data.model.Task
import com.example.lab08ejrc1task.ui.viewmodel.TaskViewModel
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val taskToEdit by remember(taskId) {
        derivedStateOf { viewModel.allTasks.value.find { it.id == taskId } }
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(2) }
    var dueDate by remember { mutableStateOf<Long?>(null) }

    // Rellenar los campos si estamos editando una tarea existente.
    LaunchedEffect(taskToEdit) {
        taskToEdit?.let {
            title = it.title
            description = it.description ?: ""
            priority = it.priority
            dueDate = it.dueDate
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "Nueva Tarea" else "Editar Tarea") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón para eliminar la tarea si estamos editando
                    if (taskId != null && taskToEdit != null) {
                        IconButton(onClick = {
                            viewModel.delete(taskToEdit!!)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val task = taskToEdit?.copy(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate
                ) ?: Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate
                )

                if (taskToEdit == null) {
                    viewModel.insert(task)
                } else {
                    viewModel.update(task)
                }

                // Programar notificación
                viewModel.scheduleNotification(context, task)
                onNavigateBack()
            }) {
                Icon(Icons.Default.Done, contentDescription = "Guardar Tarea")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            PrioritySelector(selectedPriority = priority, onPrioritySelected = { priority = it })
            DueDatePicker(selectedDueDate = dueDate, onDueDateSelected = { dueDate = it })
        }
    }
}

@Composable
fun PrioritySelector(selectedPriority: Int, onPrioritySelected: (Int) -> Unit) {
    Column {
        Text("Prioridad", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            listOf(1 to "Alta", 2 to "Media", 3 to "Baja").forEach { (priority, label) ->
                RadioButton(
                    selected = selectedPriority == priority,
                    onClick = { onPrioritySelected(priority) }
                )
                Text(label, modifier = Modifier.padding(end = 16.dp))
            }
        }
    }
}

@Composable
fun DueDatePicker(selectedDueDate: Long?, onDueDateSelected: (Long?) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    if (selectedDueDate != null) {
        calendar.timeInMillis = selectedDueDate
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            // Abrir el TimePickerDialog después de seleccionar la fecha
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    onDueDateSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false // Usar formato de 12 o 24 horas según el sistema
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { datePickerDialog.show() }) {
            Text(if (selectedDueDate == null) "Añadir Recordatorio" else "Cambiar Recordatorio")
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (selectedDueDate != null) {
            Text(
                text = "Activo",
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { onDueDateSelected(null) }) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar recordatorio")
            }
        }
    }
}
