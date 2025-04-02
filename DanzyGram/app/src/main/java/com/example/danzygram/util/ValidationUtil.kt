package com.example.danzygram.util

import android.util.Patterns
import com.example.danzygram.util.Constants.MAX_CAPTION_LENGTH
import com.example.danzygram.util.Constants.MAX_COMMENT_LENGTH
import com.example.danzygram.util.Constants.MAX_PASSWORD_LENGTH
import com.example.danzygram.util.Constants.MAX_USERNAME_LENGTH
import com.example.danzygram.util.Constants.MIN_PASSWORD_LENGTH
import com.example.danzygram.util.Constants.MIN_USERNAME_LENGTH

object ValidationUtil {
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isEmpty() -> ValidationResult.Error("Email cannot be empty")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult.Error("Invalid email format")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Error("Password cannot be empty")
            password.length < MIN_PASSWORD_LENGTH -> 
                ValidationResult.Error("Password must be at least $MIN_PASSWORD_LENGTH characters")
            password.length > MAX_PASSWORD_LENGTH ->
                ValidationResult.Error("Password cannot exceed $MAX_PASSWORD_LENGTH characters")
            !password.any { it.isDigit() } ->
                ValidationResult.Error("Password must contain at least one number")
            !password.any { it.isLetter() } ->
                ValidationResult.Error("Password must contain at least one letter")
            else -> ValidationResult.Success
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isEmpty() -> ValidationResult.Error("Confirm password cannot be empty")
            password != confirmPassword -> ValidationResult.Error("Passwords do not match")
            else -> ValidationResult.Success
        }
    }

    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isEmpty() -> ValidationResult.Error("Username cannot be empty")
            username.length < MIN_USERNAME_LENGTH ->
                ValidationResult.Error("Username must be at least $MIN_USERNAME_LENGTH characters")
            username.length > MAX_USERNAME_LENGTH ->
                ValidationResult.Error("Username cannot exceed $MAX_USERNAME_LENGTH characters")
            !username.matches(Regex("^[a-zA-Z0-9._]+$")) ->
                ValidationResult.Error("Username can only contain letters, numbers, dots, and underscores")
            username.startsWith(".") || username.endsWith(".") ->
                ValidationResult.Error("Username cannot start or end with a dot")
            username.startsWith("_") || username.endsWith("_") ->
                ValidationResult.Error("Username cannot start or end with an underscore")
            username.contains("..") ->
                ValidationResult.Error("Username cannot contain consecutive dots")
            username.contains("__") ->
                ValidationResult.Error("Username cannot contain consecutive underscores")
            else -> ValidationResult.Success
        }
    }

    fun validateCaption(caption: String): ValidationResult {
        return when {
            caption.length > MAX_CAPTION_LENGTH ->
                ValidationResult.Error("Caption cannot exceed $MAX_CAPTION_LENGTH characters")
            else -> ValidationResult.Success
        }
    }

    fun validateComment(comment: String): ValidationResult {
        return when {
            comment.isEmpty() -> ValidationResult.Error("Comment cannot be empty")
            comment.length > MAX_COMMENT_LENGTH ->
                ValidationResult.Error("Comment cannot exceed $MAX_COMMENT_LENGTH characters")
            else -> ValidationResult.Success
        }
    }

    fun validateImageSize(size: Long, maxSize: Long): ValidationResult {
        return when {
            size <= 0 -> ValidationResult.Error("Invalid image size")
            size > maxSize -> ValidationResult.Error("Image size cannot exceed ${maxSize / (1024 * 1024)}MB")
            else -> ValidationResult.Success
        }
    }

    fun validateImageDimensions(width: Int, height: Int, maxDimension: Int): ValidationResult {
        return when {
            width <= 0 || height <= 0 -> ValidationResult.Error("Invalid image dimensions")
            width > maxDimension || height > maxDimension ->
                ValidationResult.Error("Image dimensions cannot exceed ${maxDimension}x${maxDimension}")
            else -> ValidationResult.Success
        }
    }

    fun validateImageFormat(mimeType: String): ValidationResult {
        return when {
            !mimeType.startsWith("image/") -> ValidationResult.Error("Invalid file type. Only images are allowed")
            !listOf("image/jpeg", "image/png", "image/jpg").contains(mimeType) ->
                ValidationResult.Error("Only JPEG and PNG images are allowed")
            else -> ValidationResult.Success
        }
    }

    fun validateBio(bio: String, maxLength: Int = 150): ValidationResult {
        return when {
            bio.length > maxLength ->
                ValidationResult.Error("Bio cannot exceed $maxLength characters")
            else -> ValidationResult.Success
        }
    }

    fun validateWebsite(website: String): ValidationResult {
        return when {
            website.isEmpty() -> ValidationResult.Success
            !Patterns.WEB_URL.matcher(website).matches() ->
                ValidationResult.Error("Invalid website URL")
            else -> ValidationResult.Success
        }
    }

    fun validatePhoneNumber(phoneNumber: String): ValidationResult {
        return when {
            phoneNumber.isEmpty() -> ValidationResult.Success
            !Patterns.PHONE.matcher(phoneNumber).matches() ->
                ValidationResult.Error("Invalid phone number")
            else -> ValidationResult.Success
        }
    }

    fun validateName(name: String, maxLength: Int = 50): ValidationResult {
        return when {
            name.isEmpty() -> ValidationResult.Error("Name cannot be empty")
            name.length > maxLength ->
                ValidationResult.Error("Name cannot exceed $maxLength characters")
            !name.matches(Regex("^[a-zA-Z\\s]+$")) ->
                ValidationResult.Error("Name can only contain letters and spaces")
            else -> ValidationResult.Success
        }
    }
}