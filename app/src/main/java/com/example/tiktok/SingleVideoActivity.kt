package com.example.tiktok

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tiktok.adapter.videoListAdapter
import com.example.tiktok.databinding.ActivitySingleVideoBinding
import com.example.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import io.reactivex.rxjava3.core.Single

class SingleVideoActivity : AppCompatActivity() {

    lateinit var binding : ActivitySingleVideoBinding
    lateinit var videoId : String
    lateinit var adapter : videoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoId = intent.getStringExtra("videoId")!!
        setupViewpager()
    }

    private fun setupViewpager() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereEqualTo("videoId",videoId),
                VideoModel::class.java
            ).build()
        adapter = videoListAdapter(options)
        binding.videoPager.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }
}