package com.codinginflow.mvvmtodo.ui.addEditTask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_EDIT_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
        private val taskDao: TaskDao,
        @Assisted private val state:SavedStateHandle
) :ViewModel() {

    val task=state.get<Task>("task")

    var taskName=state.get<String>("taskName")  ?: task?.name ?: ""

    set(value)
    {
        field=value
        state.set("taskName",value)
    }
    var taskImportance=state.get<Boolean>("taskImportance")  ?: task?.important ?: false
    set(value)
    {
        field=value
        state.set("taskName",value)
    }
    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent =addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick()
    {
        if (taskName.isBlank()) {
            //invalid input message
            showInvalidInputMessage("name cannot be empty")
            return
        }
        if (task != null) {
            val updateTask =task.copy(name=taskName,important = taskImportance)
            updateTask(updateTask)
        }
        else{
            val newTask=Task(name = taskName,important = taskImportance)
            createNewTask(newTask)
        }
    }

    private fun showInvalidInputMessage(s: String) =viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInValidInputMessage(s))
    }

    private fun createNewTask(task:Task)=viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateTheResult(ADD_TASK_RESULT_OK))
        //navigate Back
    }
    private fun updateTask(updateTask:Task)= viewModelScope.launch {
        taskDao.update(updateTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateTheResult(ADD_EDIT_TASK_RESULT_OK))

        //navigate Back
    }

    sealed class AddEditTaskEvent{
        data class ShowInValidInputMessage(val msg:String):AddEditTaskEvent()
        data class NavigateTheResult(val result:Int):AddEditTaskEvent()
    }


}