package com.example.music.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.example.music.BuildConfig
import com.example.music.databinding.ActivityAboutBinding
import com.example.music.api.model.aboutus.Contributors
import com.example.music.network.utility.RequestNetwork
import com.example.music.network.utility.RequestNetworkController
import com.example.music.utils.customview.BottomSheetItemView

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    companion object {
        private const val TAG = "AboutActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.versionTxt.getTitleTextView().text = BuildConfig.VERSION_NAME

        binding.email.setOnClickListener {
            openUrl("mailto:harshsandeep23@gmail.com")
        }

        binding.sourceCode.setOnClickListener {
            openUrl("https://github.com/harshshah6/SaavnMp3-Android")
        }

        binding.discord.setOnClickListener {
            Toast.makeText(
                this@AboutActivity,
                "Oops, No Discord Server found.",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.instagram.setOnClickListener {
            openUrl("https://www.instagram.com/harsh_.s._shah/")
        }

        binding.telegram.setOnClickListener {
            openUrl("https://t.me/legendary_streamer_official")
        }

        binding.rate.setOnClickListener {
            openUrl("https://github.com/harshshah6/SaavnMp3-Android")
        }

        RequestNetwork(this).startRequestNetwork(
            RequestNetworkController.GET,
            "https://androsketchui.vercel.app/api/github/harshshah6/saavnmp3-android/contributors",
            "",
            object : RequestNetwork.RequestListener {
                override fun onResponse(
                    tag: String,
                    response: String,
                    responseHeaders: HashMap<String, Any>
                ) {
                    try {
                        val contributors = Gson().fromJson(response, Contributors::class.java)
                        Log.i(TAG, "contributors: $contributors")

                        // Corrección: Acceder a la propiedad 'contributors' (sin paréntesis, es una propiedad)
                        val contributorsList = contributors.contributors ?: emptyList()  // Manejo de null

                        for (contributor in contributorsList) {
                            val item = BottomSheetItemView(
                                this@AboutActivity,
                                contributor.login ?: "Unknown",  // Corrección: propiedad sin paréntesis, manejo de null
                                contributor.avatarUrl ?: "",    // Corrección: usar 'avatarUrl' (camelCase), sin paréntesis
                                ""
                            )
                            item.setOnClickListener {
                                contributor.htmlUrl?.let { openUrl(it) }  // Corrección: usar 'htmlUrl', verificación de null
                            }
                            binding.layoutContributors.addView(item)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing contributors: ", e)
                    }
                }

                override fun onErrorResponse(tag: String, message: String) {
                    Log.e(TAG, "Error fetching contributors: $message")
                }
            }
        )
    }

    private fun openUrl(url: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
        }
        startActivity(sendIntent)
    }
}
