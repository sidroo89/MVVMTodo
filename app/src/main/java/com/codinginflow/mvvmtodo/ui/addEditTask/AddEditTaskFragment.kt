package com.codinginflow.mvvmtodo.ui.addEditTask

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.FragmentAddEditTaskBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class AddEditTaskFragment :Fragment(R.layout.fragment_add_edit_task) {

   private val viewModel:AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding=FragmentAddEditTaskBinding.bind(view)
        binding.apply {
            editTextTaskName.setText(viewModel.taskName)
            importantCheckBox.isChecked=viewModel.taskImportance
            importantCheckBox.jumpDrawablesToCurrentState()
            textViewDateCreated.isVisible = viewModel.task !=null

            textViewDateCreated.text="created : ${viewModel.task?.createdDateFormatted}"

          editTextTaskName.addTextChangedListener {
              viewModel.taskName=it.toString()
          }
            importantCheckBox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance=isChecked
            }
            fabSaveTask.setOnClickListener{
                viewModel.onSaveClick()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event->
                when(event)
                {
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInValidInputMessage-> {

                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateTheResult->{
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_result",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()

                    }
                }
            }.exhaustive
        }
    }


}