package com.example.music.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.adapters.SavedLibrariesAdapter
import com.example.music.databinding.ActivitySavedLibrariesBinding
import com.example.music.databinding.AddNewLibraryBottomSheetBinding
import com.example.music.records.sharedpref.SavedLibraries
import com.example.music.utils.SharedPreferenceManager  // Import correcto
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import java.text.SimpleDateFormat
import java.util.Date

class SavedLibrariesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedLibrariesBinding
    private var savedLibraries: SavedLibraries? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedLibrariesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa SharedPreferenceManager si no lo has hecho en Application
        SharedPreferenceManager.init(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        OverScrollDecoratorHelper.setUpOverScroll(
            binding.recyclerView,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )

        binding.addNewLibrary.setOnClickListener {
            val addNewLibraryBottomSheetBinding = AddNewLibraryBottomSheetBinding.inflate(layoutInflater)
            val bottomSheetDialog = BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme)
            bottomSheetDialog.setContentView(addNewLibraryBottomSheetBinding.root)

            addNewLibraryBottomSheetBinding.cancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            addNewLibraryBottomSheetBinding.create.setOnClickListener {
                val name = addNewLibraryBottomSheetBinding.edittext.text.toString()
                if (name.isEmpty()) {
                    addNewLibraryBottomSheetBinding.edittext.error = "Name cannot be empty"
                    return@setOnClickListener
                }
                addNewLibraryBottomSheetBinding.edittext.error = null
                Log.i("SavedLibrariesActivity", "BottomSheetDialog_create: $name")

                val currentTime = System.currentTimeMillis().toString()

                val library = SavedLibraries.Library(
                    id = "#$currentTime",
                    name = name,
                    image = "",
                    description = "Created on :- ${formatMillis(currentTime.toLong())}",
                    songs = ArrayList(),
                    isAlbum = false,
                    isCreatedByUser = true
                )

                // Usa SharedPreferenceManager directamente
                SharedPreferenceManager.addLibraryToSavedLibraries(library)
                Snackbar.make(binding.root, "Library added successfully", Snackbar.LENGTH_SHORT).show()

                bottomSheetDialog.dismiss()
                showData()
            }

            bottomSheetDialog.show()
        }

        showData()
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatMillis(millis: Long): String {
        val date = Date(millis)
        val simpleDateFormat = SimpleDateFormat("MM-dd-yyyy HH:mm a")
        return simpleDateFormat.format(date)
    }

    private fun showData() {
        savedLibraries = SharedPreferenceManager.getSavedLibrariesData()  // Directo
        binding.emptyListTv.visibility = if (savedLibraries == null) View.VISIBLE else View.GONE
        savedLibraries?.let {
            binding.recyclerView.adapter = SavedLibrariesAdapter(it.lists)
        }
    }

    override fun onResume() {
        super.onResume()
        showData()
    }

    @Suppress("UNUSED_PARAMETER")
    fun backPress(view: View) {
        finish()
    }
}
