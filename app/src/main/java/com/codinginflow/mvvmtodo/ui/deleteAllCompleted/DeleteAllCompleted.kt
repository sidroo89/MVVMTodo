package com.codinginflow.mvvmtodo.ui.deleteAllCompleted

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompleted:DialogFragment() {
private val viewModel: DeleteAllCompletedViewModel by viewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Do You really want to delete all completed messages")
            .setNegativeButton("cancel",null)
            .setPositiveButton("Yes"){_,_->
                //call delete operation
                viewModel.onConfirmClick()
            }
            .create()
}