package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentTaskBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.codinginflow.mvvmtodo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_task.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment :Fragment(R.layout.fragment_task),TaskAdapter.OnItemClickListener {


    private val viewModel:TasksViewModel by viewModels()
    private lateinit var searchView:SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding=FragmentTaskBinding.bind(view)
        val taskAdapter=TaskAdapter(this)

        binding.apply {
            recyclerViewTask.apply {
                adapter=taskAdapter
                layoutManager=LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                 val task =taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwipe(task)
                }
            }).attachToRecyclerView(recyclerViewTask)
        }

        fab_add_task.setOnClickListener{
            viewModel.onAddTaskClick()
        }
       setFragmentResultListener("add_edit_result") {_,bundle->
           val result=bundle.getInt("add_edit_result")
           viewModel.onAddedResult(result)
       }
   viewModel.tasks.observe(viewLifecycleOwner, Observer { tasks->
           taskAdapter.submitList(tasks)
   })

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect { event->
                when(event)
                {
                    is TasksViewModel.TaskEvents.ShowUnDoDeleteMessage->{
                        Snackbar.make(requireView(),"Task Deleted",Snackbar.LENGTH_LONG)
                                .setAction("undo"){
                                    viewModel.onUndoDeleteClick(event.task)
                                }.show()
                    }
                   is TasksViewModel.TaskEvents.NavigateToTaskScreen -> {

                     val action =TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(null,"New Task")
                       findNavController().navigate(action)
                   }
                    is TasksViewModel.TaskEvents.NavigateTaskScreen -> {

                        val action =TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(event.task,"Edit Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvents.ShowTaskSaveConfirmationMessage->{
                        Snackbar.make(requireView(),event.msgString,Snackbar.LENGTH_SHORT).show()
                    }
                    TasksViewModel.TaskEvents.NavigateToDeletedAllCompletedTaskScreen -> {

                        val action=TaskFragmentDirections.actionGlobalDeleteAllCompleted()
                        findNavController().navigate(action)
                    }
                }.exhaustive

            }
        }
        setHasOptionsMenu(true)
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task,menu)

        val searchItem=menu.findItem(R.id.action_search)
          searchView=searchItem.actionView as SearchView
        val pendingQuery=viewModel.searchQuery.value
        if (pendingQuery!=null && pendingQuery.isNotEmpty())
        {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }
        searchView.onQueryTextChanged {
           viewModel.searchQuery.value=it
//            Log.d("test", "onCreateOptionsMenu: "+it)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_task).isChecked=viewModel.preferenceFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
      return  when(item.itemId)
        {
            R.id.action_sort_by_name->{
               viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
          R.id.action_sort_by_date_created->{
              viewModel.onSortOrderSelected(SortOrder.BY_DATE)
              true
          }
          R.id.action_hide_completed_task->{
              item.isChecked = !item.isChecked
             viewModel.onHideCompletedOnClick(item.isChecked)
              true
          }
          R.id.action_delete_all_completed_tasks->{
           viewModel.onDeleteAllCompletedTask()
              true
          }
          else->{
              super.onOptionsItemSelected(item)
          }
        }
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckClick(task: Task, isChecked: Boolean) {
      viewModel.onTaskChecked(task,isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}




