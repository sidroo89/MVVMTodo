 package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_EDIT_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

 class TasksViewModel @ViewModelInject constructor(
        private val taskDao:TaskDao,
        private  val preferencesManager: PreferencesManager,
        @Assisted private val state :SavedStateHandle

) :ViewModel() {

    val searchQuery = state.getLiveData("searchQuery","")

    val preferenceFlow=preferencesManager.preferencesFlow

     private val taskEventChannel= Channel<TaskEvents>()
     val taskEvent=taskEventChannel.receiveAsFlow()
    private val taskFlow= combine(
            searchQuery.asFlow(),
            preferenceFlow
    ){
        query,filterPreference->
        Pair(query,filterPreference)
    }
            .flatMapLatest {(query,filterPreference)->

        taskDao.getTasks(query,filterPreference.sortOrder,filterPreference.hideCompleted)

    }
     val tasks=taskFlow.asLiveData()
    fun onSortOrderSelected(sortOrder:SortOrder)=viewModelScope.launch {
        preferencesManager.updateSortOrder((sortOrder))
    }
     fun onHideCompletedOnClick(hideCompleted:Boolean)=viewModelScope.launch {
         preferencesManager.updateHideCompleted(hideCompleted)
     }

     fun onTaskSelected(task:Task)=viewModelScope.launch {
         taskEventChannel.send(TaskEvents.NavigateTaskScreen(task))
     }
     fun onTaskChecked(task: Task,isChecked:Boolean) = viewModelScope.launch {
         taskDao.update(task.copy(completed = isChecked))
     }
     fun onTaskSwipe(task: Task) = viewModelScope.launch {
         taskDao.delete(task)
         taskEventChannel.send(TaskEvents.ShowUnDoDeleteMessage(task))
     }
     fun onUndoDeleteClick(task: Task) =viewModelScope.launch {
         taskDao.insert(task)
     }

     sealed class TaskEvents{
         object NavigateToTaskScreen:TaskEvents()
         data class NavigateTaskScreen(val task:Task):TaskEvents()
         data class ShowUnDoDeleteMessage(val task: Task) : TaskEvents()
         data class ShowTaskSaveConfirmationMessage(val msgString: String):TaskEvents()
         object NavigateToDeletedAllCompletedTaskScreen:TaskEvents()
     }

     fun onAddTaskClick()  =viewModelScope.launch {
         taskEventChannel.send(TaskEvents.NavigateToTaskScreen)
     }
     fun onTaskClicked(task: Task) =  viewModelScope.launch {
         taskEventChannel.send(TaskEvents.NavigateTaskScreen(task))
     }

     fun onAddedResult(result:Int){
         when(result)
         {
             ADD_TASK_RESULT_OK->showResultTask("Task Added")

             ADD_EDIT_TASK_RESULT_OK->showResultTask("Task Updated")
         }
     }

     private fun showResultTask(s: String) =viewModelScope.launch {
         taskEventChannel.send(TaskEvents.ShowTaskSaveConfirmationMessage(s))
     }
     fun onDeleteAllCompletedTask()=viewModelScope.launch {
         taskEventChannel.send(TaskEvents.NavigateToDeletedAllCompletedTaskScreen)
     }

 }

