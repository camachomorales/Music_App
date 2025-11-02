package com.example.music.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.music.ApplicationClass
import com.example.music.databinding.ActivitySettingsBinding
import com.example.music.utils.SharedPreferenceManager
import com.example.music.utils.customview.MaterialCustomSwitch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsSharedPrefManager: SettingsSharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsSharedPrefManager = SettingsSharedPrefManager(this)
        SharedPreferenceManager.init(this)

        // Usamos objeto anónimo con el método correcto del listener
        binding.downloadOverCellular.setOnCheckChangeListener(object : MaterialCustomSwitch.OnCheckChangeListener {
            override fun onCheckChanged(isChecked: Boolean) {
                settingsSharedPrefManager.setDownloadOverCellular(isChecked)
            }
        })
        binding.highQualityTrack.setOnCheckChangeListener(object : MaterialCustomSwitch.OnCheckChangeListener {
            override fun onCheckChanged(isChecked: Boolean) {
                settingsSharedPrefManager.setHighQualityTrack(isChecked)
            }
        })
        binding.storeInCache.setOnCheckChangeListener(object : MaterialCustomSwitch.OnCheckChangeListener {
            override fun onCheckChanged(isChecked: Boolean) {
                settingsSharedPrefManager.setStoreInCache(isChecked)
            }
        })
        binding.explicit.setOnCheckChangeListener(object : MaterialCustomSwitch.OnCheckChangeListener {
            override fun onCheckChanged(isChecked: Boolean) {
                settingsSharedPrefManager.setExplicit(isChecked)
            }
        })

        binding.downloadOverCellular.setChecked(settingsSharedPrefManager.getDownloadOverCellular())
        binding.highQualityTrack.setChecked(settingsSharedPrefManager.getHighQualityTrack())
        binding.storeInCache.setChecked(settingsSharedPrefManager.getStoreInCache())
        binding.explicit.setChecked(settingsSharedPrefManager.getExplicit())

        binding.themeChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                com.example.music.R.id.dark -> "dark"
                com.example.music.R.id.light -> "light"
                else -> "system"
            }
            settingsSharedPrefManager.setTheme(theme)
            (application as ApplicationClass).updateTheme()  // CORREGIDO: Usa la instancia
        }


        binding.clearCache.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Cache")
                .setMessage("Are you sure you want to clear the cache?")
                .setPositiveButton("Yes") { _, _ ->
                    SharedPreferenceManager.clearCache()
                }
                .setNegativeButton("No", null)
                .show()
        }

        val currentTheme = settingsSharedPrefManager.getTheme()
        binding.themeChipGroup.check(
            when (currentTheme) {
                "dark" -> com.example.music.R.id.dark
                "light" -> com.example.music.R.id.light
                else -> com.example.music.R.id.system
            }
        )
    }

    fun backPress(view: View) {
        finish()
    }

    class SettingsSharedPrefManager(context: Context) {
        private val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        fun setDownloadOverCellular(value: Boolean) = sharedPreferences.edit().putBoolean("download_over_cellular", value).apply()
        fun getDownloadOverCellular(): Boolean = sharedPreferences.getBoolean("download_over_cellular", true)

        fun setHighQualityTrack(value: Boolean) = sharedPreferences.edit().putBoolean("high_quality_track", value).apply()
        fun getHighQualityTrack(): Boolean = sharedPreferences.getBoolean("high_quality_track", true)

        fun setStoreInCache(value: Boolean) = sharedPreferences.edit().putBoolean("store_in_cache", value).apply()
        fun getStoreInCache(): Boolean = sharedPreferences.getBoolean("store_in_cache", true)

        fun setExplicit(value: Boolean) = sharedPreferences.edit().putBoolean("explicit", value).apply()
        fun getExplicit(): Boolean = sharedPreferences.getBoolean("explicit", true)

        fun setTheme(theme: String) = sharedPreferences.edit().putString("theme", theme).apply()
        fun getTheme(): String = sharedPreferences.getString("theme", "system") ?: "system"
    }
}
