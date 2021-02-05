package com.codinginflow.mvvmtodo.ui.deleteAllCompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao : TaskDao,
    @AppModule.ApplicationScope private val applicationScope:CoroutineScope
): ViewModel() {

    fun onConfirmClick()=applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}