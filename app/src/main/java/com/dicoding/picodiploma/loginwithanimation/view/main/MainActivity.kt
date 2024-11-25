package com.dicoding.picodiploma.loginwithanimation.view.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.story.StoryPagingAdapter
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.addstory.TambahStoryActivity
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.maps.MapsActivity
import com.dicoding.picodiploma.loginwithanimation.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var pagingAdapter: StoryPagingAdapter
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupRecyclerView()
        setupAction()

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, TambahStoryActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_STORY)
        }

        binding.btnMaps.setOnClickListener {
            viewModel.getSession().observe(this) { user ->
                val intent = Intent(this, MapsActivity::class.java).apply {
                    putExtra("TOKEN", user.token)
                }
                startActivity(intent)
            }
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                loadStoriesPaging(user.token)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_STORY && resultCode == Activity.RESULT_OK) {
            viewModel.getSession().value?.token?.let { loadStoriesPaging(it) }
        }
    }

    private fun setupRecyclerView() {
        pagingAdapter = StoryPagingAdapter()
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pagingAdapter
        }

        // Observing load state directly from PagingDataAdapter
        pagingAdapter.addLoadStateListener { loadState ->
            // Show progress bar during initial load or refresh
            binding.progressBar.visibility =
                if (loadState.refresh is LoadState.Loading) View.VISIBLE else View.GONE

            // Handle errors during data load
            if (loadState.refresh is LoadState.Error) {
                val error = (loadState.refresh as LoadState.Error).error
                Log.e("MainActivity", "Error loading stories: ${error.localizedMessage}")
                showErrorMessage(error.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    private fun showErrorMessage(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun loadStoriesPaging(token: String) {
        lifecycleScope.launch {
            viewModel.getStoriesPaging(token).collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun setupAction() {
        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnLanguageSettings.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun logout() {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.logout()
        binding.progressBar.visibility = View.GONE
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            Log.d("Photo Picker", "Selected media URI: $uri")

            val intent = Intent(this, TambahStoryActivity::class.java).apply {
                putExtra("IMAGE_URI", uri.toString())
            }
            startActivity(intent)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Bahasa Indonesia")
        val languageCodes = arrayOf("en", "in")

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language_settings))
            .setItems(languages) { _, which ->
                setLocale(languageCodes[which])
            }
            .show()
    }

    private fun setLocale(languageCode: String) {
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("My_Lang", "en")

        if (currentLang != languageCode) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources = resources
            val config = resources.configuration
            config.setLocale(locale)

            resources.updateConfiguration(config, resources.displayMetrics)

            val editor = prefs.edit()
            editor.putString("My_Lang", languageCode)
            editor.apply()

            recreate()
        }
    }

    private fun loadLocale() {
        val prefs: SharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "en")
        val locale = Locale(language!!)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)
    }

    companion object {
        private const val REQUEST_ADD_STORY = 1
    }
}
