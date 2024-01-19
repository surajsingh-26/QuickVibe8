package com.example.tiktok.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.ProfileActivity
import com.example.tiktok.R
import com.example.tiktok.databinding.VideoItemRowBinding
import com.example.tiktok.model.UserModel
import com.example.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class videoListAdapter(
    options: FirestoreRecyclerOptions<VideoModel>
) : FirestoreRecyclerAdapter<VideoModel,videoListAdapter.VideoViewHolder>(options) {

    inner class VideoViewHolder(private val binding: VideoItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindVideo(videoModel: VideoModel) {

            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get()
                .addOnSuccessListener {
                    val userModel=it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.userName.text = username

                        Glide.with(binding.profilePic).load(profilepic)
                            .circleCrop()
                            .apply(
                                RequestOptions().placeholder(R.drawable.icon_profile)
                            )
                            .into(binding.profilePic)

                        binding.userName.setOnClickListener {
                            val intent = Intent(binding.userName.context, ProfileActivity::class.java)
                            intent.putExtra("profile_user_id" , id)
                            binding.userName.context.startActivity(intent)
                        }
                    }
                }
            binding.captionInPost.text = videoModel.title
            binding.progressBar.visibility = View.VISIBLE


            binding.videoView.apply {
                setVideoPath(videoModel.url)
                setOnPreparedListener {
                    it.start()
                    it.isLooping = true
                }
                setOnClickListener {
                    if(isPlaying){
                        pause()
                        binding.playIcon.visibility = View.VISIBLE
                    }
                    else {
                        start()
                        binding.PauseIcon.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): videoListAdapter.VideoViewHolder {
       val binding = VideoItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: videoListAdapter.VideoViewHolder,
        position: Int,
        model: VideoModel
    ) {
        holder.bindVideo(model)
    }
}