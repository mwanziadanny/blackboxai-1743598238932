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
import com.example.danzygram.data.User
import com.example.danzygram.databinding.FragmentEditProfileBinding
import com.example.danzygram.util.FirebaseUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.profileImage.setImageURI(uri)
                updateSaveButtonState()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupInputListeners()
        loadUserProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.saveButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun setupInputListeners() {
        binding.changePhotoButton.setOnClickListener {
            showImagePickerOptions()
        }

        binding.usernameEditText.addTextChangedListener {
            updateSaveButtonState()
        }

        binding.bioEditText.addTextChangedListener {
            updateSaveButtonState()
        }
    }

    private fun loadUserProfile() {
        val currentUserId = FirebaseUtil.getCurrentUser()?.uid ?: return
        
        showLoading(true)
        lifecycleScope.launch {
            try {
                currentUser = FirebaseUtil.getUsersCollection()
                    .document(currentUserId)
                    .get()
                    .await()
                    .toObject(User::class.java)

                currentUser?.let { user ->
                    binding.usernameEditText.setText(user.username)
                    binding.bioEditText.setText(user.bio)

                    // Load profile image
                    user.profileImageUrl.let { url ->
                        binding.profileImage.setImageResource(R.drawable.placeholder_profile)
                        if (url.isNotEmpty()) {
                            binding.profileImage.setImageURI(android.net.Uri.parse(url))
                        }
                    }
                }
            } catch (e: Exception) {
                showError(e.localizedMessage ?: getString(R.string.error_loading_profile))
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showImagePickerOptions() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.change_profile_picture)
            .setItems(
                arrayOf(
                    getString(R.string.take_photo),
                    getString(R.string.choose_from_gallery)
                )
            ) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        // TODO: Implement camera functionality
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        getContent.launch(intent)
    }

    private fun saveChanges() {
        val currentUserId = FirebaseUtil.getCurrentUser()?.uid ?: return
        val newUsername = binding.usernameEditText.text.toString().trim()
        val newBio = binding.bioEditText.text.toString().trim()

        if (newUsername.isEmpty()) {
            binding.usernameLayout.error = getString(R.string.error_username_required)
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                // Upload new profile picture if selected
                var profileImageUrl = currentUser?.profileImageUrl ?: ""
                selectedImageUri?.let { uri ->
                    val imageRef = FirebaseUtil.getStorage()
                        .child("profile_pictures")
                        .child(currentUserId)
                        .child("${UUID.randomUUID()}.jpg")

                    imageRef.putFile(uri).await()
                    profileImageUrl = imageRef.downloadUrl.await().toString()
                }

                // Update user document
                FirebaseUtil.getUsersCollection()
                    .document(currentUserId)
                    .update(
                        mapOf(
                            "username" to newUsername,
                            "bio" to newBio,
                            "profileImageUrl" to profileImageUrl
                        )
                    )
                    .await()

                showSnackbar(getString(R.string.profile_updated))
                findNavController().navigateUp()
            } catch (e: Exception) {
                showError(e.localizedMessage ?: getString(R.string.error_update_profile))
                showLoading(false)
            }
        }
    }

    private fun updateSaveButtonState() {
        val hasChanges = selectedImageUri != null ||
                binding.usernameEditText.text.toString() != currentUser?.username ||
                binding.bioEditText.text.toString() != currentUser?.bio

        binding.saveButton.isEnabled = hasChanges
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !show
        binding.usernameEditText.isEnabled = !show
        binding.bioEditText.isEnabled = !show
        binding.changePhotoButton.isEnabled = !show
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}