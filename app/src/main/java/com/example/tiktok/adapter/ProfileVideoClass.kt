package com.example.tiktok.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tiktok.SingleVideoActivity
import com.example.tiktok.databinding.ProfileItemLayoutBinding
import com.example.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ProfileVideoClass(options: FirestoreRecyclerOptions<VideoModel>) :
    FirestoreRecyclerAdapter<VideoModel, ProfileVideoClass.VideoViewHolder>(options) {

    inner class VideoViewHolder(val binding: ProfileItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(video: VideoModel) {
            Glide.with(binding.thumbnailImageView)
                .load(video.url)
                .into(binding.thumbnailImageView)
            binding.thumbnailImageView.setOnClickListener {
                val intent = Intent(binding.thumbnailImageView.context, SingleVideoActivity::class.java)
                intent.putExtra("videoId", video.videoId)
                binding.thumbnailImageView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
       val binding = ProfileItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
       holder.bind(model)
    }


}