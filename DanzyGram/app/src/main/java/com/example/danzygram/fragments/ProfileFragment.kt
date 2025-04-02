package com.example.danzygram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.danzygram.R
import com.example.danzygram.adapters.ProfilePostAdapter
import com.example.danzygram.data.Post
import com.example.danzygram.data.User
import com.example.danzygram.databinding.FragmentProfileBinding
import com.example.danzygram.util.FirebaseUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profilePostAdapter: ProfilePostAdapter
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadUserProfile()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        profilePostAdapter = ProfilePostAdapter { post ->
            // Handle post click - navigate to post detail
            // findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToPostDetailFragment(post.postId))
        }
        binding.postsGrid.adapter = profilePostAdapter
    }

    private fun setupClickListeners() {
        binding.editProfileButton.setOnClickListener {
            // Navigate to edit profile
            // findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment())
        }

        binding.profileImage.setOnClickListener {
            // Show profile picture options
            showProfilePictureOptions()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserProfile() {
        val currentUserId = FirebaseUtil.getCurrentUser()?.uid ?: return
        
        showLoading(true)
        lifecycleScope.launch {
            try {
                // Load user data
                currentUser = FirebaseUtil.getUsersCollection()
                    .document(currentUserId)
                    .get()
                    .await()
                    .toObject(User::class.java)

                currentUser?.let { user ->
                    updateUI(user)
                }

                // Load user posts
                val posts = FirebaseUtil.getPostsCollection()
                    .whereEqualTo("userId", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .toObjects(Post::class.java)

                profilePostAdapter.submitList(posts)
            } catch (e: Exception) {
                showError(e.localizedMessage ?: getString(R.string.error_loading_profile))
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateUI(user: User) {
        binding.apply {
            usernameTitle.text = user.username
            postsCount.text = user.posts.toString()
            followersCount.text = user.followers.toString()
            followingCount.text = user.following.toString()
            bio.text = user.bio

            // Load profile image
            user.profileImageUrl.let { url ->
                profileImage.setImageResource(R.drawable.placeholder_profile)
                if (url.isNotEmpty()) {
                    profileImage.setImageURI(android.net.Uri.parse(url))
                }
            }
        }
    }

    private fun showProfilePictureOptions() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.change_profile_picture)
            .setItems(
                arrayOf(
                    getString(R.string.take_photo),
                    getString(R.string.choose_from_gallery),
                    getString(R.string.remove_photo)
                )
            ) { _, which ->
                when (which) {
                    0 -> {} // Take photo
                    1 -> {} // Choose from gallery
                    2 -> removeProfilePicture()
                }
            }
            .show()
    }

    private fun removeProfilePicture() {
        val currentUserId = FirebaseUtil.getCurrentUser()?.uid ?: return
        
        lifecycleScope.launch {
            try {
                // Remove profile picture URL from user document
                FirebaseUtil.getUsersCollection()
                    .document(currentUserId)
                    .update("profileImageUrl", "")
                    .await()

                // Delete profile picture from storage
                currentUser?.profileImageUrl?.let { url ->
                    if (url.isNotEmpty()) {
                        FirebaseUtil.getStorage()
                            .child("profile_pictures")
                            .child(currentUserId)
                            .delete()
                            .await()
                    }
                }

                // Reload profile
                loadUserProfile()
            } catch (e: Exception) {
                showError(getString(R.string.error_remove_profile_picture))
            }
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout) { _, _ ->
                logout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun logout() {
        FirebaseUtil.getAuth().signOut()
        // Navigate to login activity
        activity?.let {
            startActivity(android.content.Intent(it, LoginActivity::class.java))
            it.finish()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}