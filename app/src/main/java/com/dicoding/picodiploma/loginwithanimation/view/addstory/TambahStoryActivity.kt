package com.dicoding.picodiploma.loginwithanimation.view.addstory

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.api.reduceFileImage
import com.dicoding.picodiploma.loginwithanimation.api.uriToFile
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityTambahStoryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class TambahStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahStoryBinding
    private lateinit var userPreference: UserPreference
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var photoFile: File? = null
    private var selectedImageUri: Uri? = null
    private var currentLocation: Location? = null
    private var includeLocation: Boolean = false

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val REQUEST_LOCATION_PERMISSION = 101
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoFile?.let {
                    val photoUri = Uri.fromFile(it)
                    binding.ivPreview.setImageURI(photoUri)
                    selectedImageUri = photoUri
                }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                binding.ivPreview.setImageURI(selectedImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(dataStore)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.switchUseLocation.setOnCheckedChangeListener { _, isChecked ->
            includeLocation = isChecked
            if (isChecked) getCurrentLocation()
        }

        binding.btnTakePhoto.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            val description = binding.etDescription.text.toString()
            if (description.isNotEmpty() && selectedImageUri != null) {
                lifecycleScope.launch {
                    val userModel = userPreference.getSession().first()
                    val token = "Bearer ${userModel.token}"
                    uploadImage(description, token, currentLocation)
                }
            } else {
                Toast.makeText(this, "Fill the description and select an image!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            try {
                photoFile = createImageFile()
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.dicoding.picodiploma.loginwithanimation.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePhotoLauncher.launch(takePictureIntent)
                }
            } catch (ex: IOException) {
                Toast.makeText(this, "Failed to create image file.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Camera is not available.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = externalCacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply { photoFile = this }
    }

    private fun uploadImage(description: String, token: String, location: Location?) {
        selectedImageUri?.let { uri ->
            binding.progressBar.visibility = View.VISIBLE
            var imageFile = uriToFile(uri, this)
            imageFile = imageFile.reduceFileImage()

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)

            val lat = location?.latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lon = location?.longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            lifecycleScope.launch {
                try {
                    val apiService = ApiConfig.getApiService()
                    val response = apiService.uploadImage(token, multipartBody, requestBody, lat, lon)
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@TambahStoryActivity, response.message, Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@TambahStoryActivity, "Failed to upload story.", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        val locationTask: Task<Location> = fusedLocationProviderClient.lastLocation
        locationTask.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                Toast.makeText(this, "Location obtained", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
