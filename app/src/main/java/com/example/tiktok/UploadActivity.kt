package com.example.tiktok

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tiktok.databinding.ActivityUploadBinding
import com.example.tiktok.model.VideoModel
import com.example.tiktok.util.Uiutil
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage


class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var selectedVideoUri: Uri
    lateinit var videoLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)


        videoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data.also {
                        if (it != null) {
                            selectedVideoUri = it
                        }
                    }
                    showPostView()

                }
            }
        binding.uploadIcon.setOnClickListener {
            checkPermissionAndOpenVideoPicker()
        }
        binding.postButton.setOnClickListener {
            postVideo()
        }
    }

    private fun postVideo() {
        if (binding.caption.text.toString().isEmpty()) {
            binding.caption.error = "Write the caption"
            return
        } else {
            setInProgress(true)
            selectedVideoUri.apply {

                //store in firebase

                val videoRef = FirebaseStorage.getInstance()
                    .reference
                    .child("videos/*" + this.lastPathSegment)
                videoRef.putFile(this)
                    .addOnSuccessListener {
                        videoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            postToFirestore(downloadUrl.toString())
                            showThumbnail()
                        }
                    }
                //video model store in firebase store
            }
        }
    }

    private fun showThumbnail() {
        binding.postThumbnail.setImageURI(selectedVideoUri)
        return
    }


    private fun postToFirestore(url: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""
        val videoId = userId + "_" + Timestamp.now().toString()

        val videoModel = VideoModel(
            videoId,
            binding.caption.text.toString(),
            url,
            FirebaseAuth.getInstance().currentUser?.uid!!,
            Timestamp.now()
        )
        Firebase.firestore.collection("videos")
            .document(videoModel.videoId)
            .set(videoModel)
            .addOnSuccessListener {
                setInProgress(false)
                Uiutil.showToast(this, "video Uploaded")
                finish()
            }.addOnFailureListener {
                setInProgress(false)
                Uiutil.showToast(this, "Video notUploaded")
            }

    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.postButton.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.postButton.visibility = View.VISIBLE

        }
    }

    private fun showPostView() {
        binding.postView.visibility = View.VISIBLE
        binding.uploadView.visibility = View.GONE
    }

    private fun checkPermissionAndOpenVideoPicker() {
        var readExternalVideo: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readExternalVideo = android.Manifest.permission.READ_MEDIA_VIDEO
        } else {
            readExternalVideo = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(
                this,
                readExternalVideo
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            OpenVideoPicker()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(readExternalVideo), 100)
        }
    }

    private fun OpenVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        videoLauncher.launch(intent)
    }
}