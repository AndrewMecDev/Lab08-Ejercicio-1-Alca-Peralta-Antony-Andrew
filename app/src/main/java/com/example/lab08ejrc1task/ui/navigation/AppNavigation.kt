package com.example.lab08ejrc1task.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab08ejrc1task.ui.screens.AddEditTaskScreen
import com.example.lab08ejrc1task.ui.screens.TaskListScreen
import com.example.lab08ejrc1task.ui.viewmodel.TaskViewModel

// Definimos las rutas de nuestra aplicación para evitar errores de tipeo.
object Routes {
    const val TASK_LIST = "taskList"
    const val ADD_EDIT_TASK = "addEditTask"
}

@Composable
fun AppNavigation(taskViewModel: TaskViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.TASK_LIST) {
        // Ruta para la pantalla principal de la lista de tareas
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                viewModel = taskViewModel,
                onAddTask = {
                    // Navega a la pantalla de edición sin un ID, indicando una nueva tarea.
                    navController.navigate("${Routes.ADD_EDIT_TASK}/null")
                },
                onEditTask = { taskId ->
                    // Navega a la pantalla de edición con el ID de la tarea a editar.
                    navController.navigate("${Routes.ADD_EDIT_TASK}/$taskId")
                }
            )
        }

        // Ruta para la pantalla de añadir/editar.
        // Recibe un 'taskId' opcional como argumento en la URL.
        composable(
            route = "${Routes.ADD_EDIT_TASK}/{taskId}",
            arguments = listOf(navArgument("taskId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            AddEditTaskScreen(
                viewModel = taskViewModel,
                taskId = if (taskId == "null") null else taskId,
                onNavigateBack = {
                    // Regresa a la pantalla anterior.
                    navController.popBackStack()
                }
            )
        }
    }
}
