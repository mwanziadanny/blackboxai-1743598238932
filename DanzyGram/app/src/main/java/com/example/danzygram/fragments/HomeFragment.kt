package com.example.danzygram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.danzygram.R
import com.example.danzygram.adapters.PostAdapter
import com.example.danzygram.data.Post
import com.example.danzygram.databinding.FragmentHomeBinding
import com.example.danzygram.util.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        loadPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onLikeClick = { post -> handleLikeClick(post) },
            onCommentClick = { post -> handleCommentClick(post) },
            onShareClick = { post -> handleShareClick(post) },
            onSaveClick = { post -> handleSaveClick(post) },
            onProfileClick = { userId -> handleProfileClick(userId) }
        )
        binding.postsRecyclerView.adapter = postAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadPosts()
        }
    }

    private fun loadPosts() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val posts = FirebaseUtil.getPostsCollection()
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .toObjects(Post::class.java)

                if (posts.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)
                    postAdapter.submitList(posts)
                }
            } catch (e: Exception) {
                showError(e.localizedMessage ?: getString(R.string.error_loading_posts))
            } finally {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun handleLikeClick(post: Post) {
        lifecycleScope.launch {
            try {
                val currentUserId = FirebaseUtil.getCurrentUser()?.uid ?: return@launch
                val isLiked = post.likedBy.contains(currentUserId)
                
                val postRef = FirebaseUtil.getPostsCollection().document(post.postId)
                
                if (isLiked) {
                    // Unlike
                    postRef.update(
                        mapOf(
                            "likes" to (post.likes - 1),
                            "likedBy" to post.likedBy.filter { it != currentUserId }
                        )
                    ).await()
                } else {
                    // Like
                    postRef.update(
                        mapOf(
                            "likes" to (post.likes + 1),
                            "likedBy" to post.likedBy + currentUserId
                        )
                    ).await()
                }
                
                // Refresh posts to show updated like status
                loadPosts()
            } catch (e: Exception) {
                showError(getString(R.string.error_like_post))
            }
        }
    }

    private fun handleCommentClick(post: Post) {
        // Navigate to comments fragment
        // findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCommentsFragment(post.postId))
    }

    private fun handleShareClick(post: Post) {
        // Implement share functionality
    }

    private fun handleSaveClick(post: Post) {
        // Implement save/bookmark functionality
    }

    private fun handleProfileClick(userId: String) {
        // Navigate to profile fragment
        // findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment(userId))
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.postsRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}