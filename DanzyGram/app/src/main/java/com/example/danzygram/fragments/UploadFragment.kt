package com.example.danzygram.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.danzygram.R
import com.example.danzygram.data.Post
import com.example.danzygram.data.User
import com.example.danzygram.databinding.FragmentUploadBinding
import com.example.danzygram.util.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UploadFragment : Fragment() {
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.postImage.setImageURI(uri)
                updatePostButtonState()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        // Select image button
        binding.selectImageButton.setOnClickListener {
            openImagePicker()
        }

        // Caption input
        binding.captionEditText.addTextChangedListener {
            updatePostButtonState()
        }

        // Post button
        binding.postButton.setOnClickListener {
            uploadPost()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        getContent.launch(intent)
    }

    private fun updatePostButtonState() {
        binding.postButton.isEnabled = selectedImageUri != null
    }

    private fun uploadPost() {
        val imageUri = selectedImageUri ?: return
        val caption = binding.captionEditText.text.toString()
        val currentUserId = FirebaseUtil.getCurrentUser()?.uid ?: return

        showLoading(true)
        lifecycleScope.launch {
            try {
                // Get current user data
                val currentUser = FirebaseUtil.getUsersCollection()
                    .document(currentUserId)
                    .get()
                    .await()
                    .toObject(User::class.java) ?: throw Exception("User not found")

                // Upload image to Firebase Storage
                val imageRef = FirebaseUtil.getStorage()
                    .child("posts")
                    .child(currentUserId)
                    .child("${UUID.randomUUID()}.jpg")

                imageRef.putFile(imageUri).await()
                val imageUrl = imageRef.downloadUrl.await().toString()

                // Create post document
                val postId = UUID.randomUUID().toString()
                val post = Post(
                    postId = postId,
                    userId = currentUserId,
                    imageUrl = imageUrl,
                    caption = caption,
                    userProfileImage = currentUser.profileImageUrl,
                    username = currentUser.username
                )

                // Save post to Firestore
                FirebaseUtil.getPostsCollection()
                    .document(postId)
                    .set(post)
                    .await()

                // Update user's post count
                FirebaseUtil.getUsersCollection()
                    .document(currentUserId)
                    .update("posts", currentUser.posts + 1)
                    .await()

                // Show success and navigate back
                showSnackbar(getString(R.string.success_post))
                findNavController().navigateUp()
            } catch (e: Exception) {
                showSnackbar(e.localizedMessage ?: getString(R.string.error_upload))
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.postButton.isEnabled = !show
        binding.selectImageButton.isEnabled = !show
        binding.captionEditText.isEnabled = !show
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}