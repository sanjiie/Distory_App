package com.dicoding.picodiploma.loginwithanimation.story

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.view.detail.DetailStoryActivity

class StoryAdapter(private val stories: List<ListStoryItem?>) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        story?.let {
            holder.bind(it)
            holder.itemView.setOnClickListener { view ->
                val intent = Intent(view.context, DetailStoryActivity::class.java)
                intent.putExtra("EXTRA_STORY", it)
                view.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = stories.size

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)

        fun bind(story: ListStoryItem) {
            tvName.text = story.name
            tvDescription.text = story.description
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(ivPhoto)
        }
    }
}
