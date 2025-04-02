package com.example.danzygram.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.danzygram.R
import com.example.danzygram.data.Post
import com.example.danzygram.databinding.ItemProfilePostBinding

class ProfilePostAdapter(
    private val onPostClick: (Post) -> Unit
) : ListAdapter<Post, ProfilePostAdapter.ProfilePostViewHolder>(ProfilePostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
        val binding = ItemProfilePostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProfilePostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProfilePostViewHolder(
        private val binding: ItemProfilePostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPostClick(getItem(position))
                }
            }

            // Show overlay on long press
            binding.root.setOnLongClickListener {
                binding.root.findViewById<ViewGroup>(R.id.overlay).visibility = ViewGroup.VISIBLE
                true
            }
        }

        fun bind(post: Post) {
            binding.apply {
                // Load post image using data binding
                post.imageUrl.let { url ->
                    postImage.setImageResource(R.drawable.placeholder_image)
                    if (url.isNotEmpty()) {
                        binding.postImage.setImageURI(android.net.Uri.parse(url))
                    }
                }

                // Set counts
                likesCount.text = post.likes.toString()
                commentsCount.text = post.comments.toString()
            }
        }
    }

    private class ProfilePostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}