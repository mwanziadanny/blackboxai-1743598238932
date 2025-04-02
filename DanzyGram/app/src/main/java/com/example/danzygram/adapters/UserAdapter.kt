package com.example.danzygram.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.danzygram.R
import com.example.danzygram.data.User
import com.example.danzygram.databinding.ItemUserBinding
import com.example.danzygram.util.FirebaseUtil

class UserAdapter(
    private val onUserClick: (User) -> Unit,
    private val onFollowClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUserClick(getItem(position))
                }
            }

            binding.followButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFollowClick(getItem(position))
                }
            }
        }

        fun bind(user: User) {
            binding.apply {
                username.text = user.username
                bio.text = user.bio

                // Load profile image using data binding
                user.profileImageUrl.let { url ->
                    profileImage.setImageResource(R.drawable.placeholder_profile)
                    if (url.isNotEmpty()) {
                        binding.profileImage.setImageURI(android.net.Uri.parse(url))
                    }
                }

                // Check if current user is following this user
                val currentUserId = FirebaseUtil.getCurrentUser()?.uid
                if (currentUserId != null && currentUserId != user.userId) {
                    followButton.visibility = android.view.View.VISIBLE
                    // TODO: Check if following and update button state
                    // followButton.text = if (isFollowing) context.getString(R.string.following) else context.getString(R.string.follow)
                } else {
                    followButton.visibility = android.view.View.GONE
                }
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}