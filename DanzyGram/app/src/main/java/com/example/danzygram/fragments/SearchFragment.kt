package com.example.danzygram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.danzygram.R
import com.example.danzygram.adapters.UserAdapter
import com.example.danzygram.data.User
import com.example.danzygram.databinding.FragmentSearchBinding
import com.example.danzygram.util.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var userAdapter: UserAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchInput()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onUserClick = { user -> navigateToProfile(user) },
            onFollowClick = { user -> handleFollowClick(user) }
        )
        binding.usersRecyclerView.adapter = userAdapter
    }

    private fun setupSearchInput() {
        binding.searchEditText.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                // Add delay to avoid too many requests while typing
                delay(300)
                editable?.toString()?.let { query ->
                    if (query.isNotEmpty()) {
                        searchUsers(query)
                    } else {
                        userAdapter.submitList(emptyList())
                        showEmptyState(false)
                    }
                }
            }
        }
    }

    private suspend fun searchUsers(query: String) {
        try {
            showLoading(true)
            
            // Search users where username contains query (case-insensitive)
            val users = FirebaseUtil.getUsersCollection()
                .orderBy("username")
                .startAt(query.lowercase())
                .endAt(query.lowercase() + "\uf8ff")
                .limit(20)
                .get()
                .await()
                .toObjects(User::class.java)
                .filter { it.userId != FirebaseUtil.getCurrentUser()?.uid }

            if (users.isEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
                userAdapter.submitList(users)
            }
        } catch (e: Exception) {
            showError(e.localizedMessage ?: getString(R.string.error_search_users))
        } finally {
            showLoading(false)
        }
    }

    private fun handleFollowClick(user: User) {
        val currentUserId = FirebaseUtil.getCurrentUser()?.uid ?: return
        
        lifecycleScope.launch {
            try {
                val currentUserRef = FirebaseUtil.getUsersCollection().document(currentUserId)
                val targetUserRef = FirebaseUtil.getUsersCollection().document(user.userId)

                // Check if already following
                val currentUser = currentUserRef.get().await().toObject(User::class.java)
                val isFollowing = currentUser?.following?.contains(user.userId) == true

                if (isFollowing) {
                    // Unfollow
                    currentUserRef.update("following", currentUser.following - 1).await()
                    targetUserRef.update("followers", user.followers - 1).await()
                } else {
                    // Follow
                    currentUserRef.update("following", currentUser?.following?.plus(1) ?: 1).await()
                    targetUserRef.update("followers", user.followers + 1).await()
                }

                // Refresh search results
                binding.searchEditText.text?.toString()?.let { query ->
                    if (query.isNotEmpty()) {
                        searchUsers(query)
                    }
                }
            } catch (e: Exception) {
                showError(getString(R.string.error_follow_user))
            }
        }
    }

    private fun navigateToProfile(user: User) {
        // Navigate to profile fragment
        // findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToProfileFragment(user.userId))
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.usersRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}