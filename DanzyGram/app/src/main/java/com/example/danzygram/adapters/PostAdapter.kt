package com.example.danzygram.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.danzygram.R
import com.example.danzygram.data.Post
import com.example.danzygram.databinding.ItemPostBinding
import com.example.danzygram.util.FirebaseUtil

class PostAdapter(
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onShareClick: (Post) -> Unit,
    private val onSaveClick: (Post) -> Unit,
    private val onProfileClick: (String) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                // Set user info
                username.text = post.username
                profileImage.setOnClickListener { onProfileClick(post.userId) }

                // Load images using data binding
                post.userProfileImage.let { url ->
                    profileImage.setImageResource(R.drawable.placeholder_profile)
                    if (url.isNotEmpty()) {
                        binding.profileImage.setImageURI(android.net.Uri.parse(url))
                    }
                }

                post.imageUrl.let { url ->
                    postImage.setImageResource(R.drawable.placeholder_image)
                    if (url.isNotEmpty()) {
                        binding.postImage.setImageURI(android.net.Uri.parse(url))
                    }
                }

                // Set post details
                caption.text = post.caption
                likesCount.text = itemView.context.getString(
                    R.string.likes,
                    post.likes
                )
                commentsCount.text = itemView.context.getString(
                    R.string.comments,
                    post.comments
                )

                // Check if current user has liked the post
                val currentUserId = FirebaseUtil.getCurrentUser()?.uid
                val isLiked = currentUserId?.let { post.likedBy.contains(it) } ?: false
                likeButton.setImageResource(
                    if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like
                )

                // Set click listeners
                likeButton.setOnClickListener { onLikeClick(post) }
                commentButton.setOnClickListener { onCommentClick(post) }
                shareButton.setOnClickListener { onShareClick(post) }
                saveButton.setOnClickListener { onSaveClick(post) }

                // Set more options menu
                moreOptions.setOnClickListener {
                    // Show options menu for post owner
                    if (post.userId == currentUserId) {
                        // Show delete/edit options
                    }
                }
            }
        }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}