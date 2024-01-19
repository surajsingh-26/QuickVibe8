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
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.adapter.ProfileVideoClass
import com.example.tiktok.databinding.ActivityProfileBinding
import com.example.tiktok.databinding.VideoItemRowBinding
import com.example.tiktok.model.UserModel
import com.example.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    lateinit var profileUserId: String
    lateinit var currentUserId: String
    lateinit var profileUserModel: UserModel
    lateinit var photoLauncher: ActivityResultLauncher<Intent>

    lateinit var adapter : ProfileVideoClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileUserId = intent.getStringExtra("profile_user_id")!!
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!


        photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
//upload Photo
                    uploadToFirestore(result.data?.data!!)
                }
            }


        if (profileUserId == currentUserId) {
            //current user Profile
            binding.profileBtn.text = "Logout"
            binding.profileBtn.setOnClickListener {
                logout()
            }
            binding.profileIcon.setOnClickListener {
                checkPermissionAndPickPhoto()
            }
        } else {//Other user profile
            binding.profileBtn.text = "Follow"
            binding.profileBtn.setOnClickListener {
                followUnfollowUser()
            }

        }
        getProfileDataFromFirebase()
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereEqualTo("uploaderId", profileUserId)
                    .orderBy("createdTime", Query.Direction.DESCENDING),
                VideoModel::class.java
            ).build()
        adapter= ProfileVideoClass(options)
        binding.recyclerView.layoutManager = GridLayoutManager(this,3)
        binding.recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

    private fun followUnfollowUser() {
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!
                if (profileUserModel.followerList.contains(currentUserId)) {
                    //Unfollow User
                    profileUserModel.followerList.remove(currentUserId)
                    currentUserModel.followingList.remove(profileUserId)
                    binding.profileBtn.text = "Follow"
                } else {
                    //follow User
                    profileUserModel.followerList.add(currentUserId)
                    currentUserModel.followingList.add(profileUserId)
                    binding.profileBtn.text = "Unfollow"
                }
                updateUserData(profileUserModel)
                updateUserData(currentUserModel)

            }


    }

    fun updateUserData(model: UserModel) {
        Firebase.firestore.collection("users")
            .document(model.id)
            .set(model)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    private fun uploadToFirestore(photoUri: Uri) {
        binding.accountProgressBar.visibility = View.VISIBLE
        val photoRef = FirebaseStorage.getInstance()
            .reference
            .child("profilePic" + currentUserId)
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    postToFirestore(downloadUrl.toString())
                }
            }
    }

    private fun postToFirestore(url: String) {
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .update("profilePic", url)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }

    }

    fun checkPermissionAndPickPhoto() {
        var readExternalVideo: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readExternalVideo = android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            readExternalVideo = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(
                this,
                readExternalVideo
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            OpenPhotoPicker()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(readExternalVideo), 100)
        }
    }

    private fun OpenPhotoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }


    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getProfileDataFromFirebase() {
        Firebase.firestore.collection("users")
            .document(profileUserId)
            .get()
            .addOnSuccessListener {
                profileUserModel = it.toObject(UserModel::class.java)!!
                setUi()
            }
    }

    private fun setUi() {

        profileUserModel.apply {
            Glide.with(binding.profileIcon).load(profilepic)
                .apply(RequestOptions().placeholder(R.drawable.icon_account_circlr))
                .circleCrop()
                .into(binding.profileIcon)
            binding.accountUsername.text = "@" + username
            if (profileUserModel.followerList.contains(currentUserId)) {
                binding.profileBtn.text = "Unfollow"
            }
            binding.accountProgressBar.visibility = View.INVISIBLE
            binding.followingCount.text = followingList.size.toString()
            binding.followerCount.text = followerList.size.toString()
            Firebase.firestore.collection("videos")
                .whereEqualTo("uploaderId", profileUserId)
                .get().addOnSuccessListener {
                    binding.postsCount.text = it.size().toString()
                }
        }

    }
}