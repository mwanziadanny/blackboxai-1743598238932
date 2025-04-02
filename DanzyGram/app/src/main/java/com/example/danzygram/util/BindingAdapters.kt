package com.example.danzygram.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.load
import com.example.danzygram.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@BindingAdapter("isVisible")
fun View.setIsVisible(visible: Boolean) {
    isVisible = visible
}

@BindingAdapter("shimmerVisible")
fun ShimmerFrameLayout.setShimmerVisible(visible: Boolean) {
    if (visible) {
        startShimmer()
        isVisible = true
    } else {
        stopShimmer()
        isVisible = false
    }
}

@BindingAdapter("imageUrl")
fun ImageView.setImageUrl(url: String?) {
    if (!url.isNullOrEmpty()) {
        load(url) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.ic_error)
        }
    } else {
        setImageResource(R.drawable.placeholder_image)
    }
}

@BindingAdapter("profileImageUrl")
fun ImageView.setProfileImageUrl(url: String?) {
    if (!url.isNullOrEmpty()) {
        load(url) {
            crossfade(true)
            placeholder(R.drawable.placeholder_profile)
            error(R.drawable.placeholder_profile)
        }
    } else {
        setImageResource(R.drawable.placeholder_profile)
    }
}

@BindingAdapter("timestamp")
fun TextView.setTimestamp(timestamp: Date?) {
    text = timestamp?.let { formatTimestamp(it) } ?: ""
}

@BindingAdapter("likesCount")
fun TextView.setLikesCount(count: Int) {
    text = context.resources.getQuantityString(
        R.plurals.likes_count,
        count,
        count
    )
}

@BindingAdapter("commentsCount")
fun TextView.setCommentsCount(count: Int) {
    text = context.resources.getQuantityString(
        R.plurals.comments_count,
        count,
        count
    )
}

@BindingAdapter("followButtonState")
fun MaterialButton.setFollowButtonState(isFollowing: Boolean) {
    if (isFollowing) {
        setText(R.string.following_btn)
        setIconResource(R.drawable.ic_check)
    } else {
        setText(R.string.follow)
        icon = null
    }
}

@BindingAdapter("likeButtonState")
fun ImageView.setLikeButtonState(isLiked: Boolean) {
    setImageResource(
        if (isLiked) R.drawable.ic_like_filled
        else R.drawable.ic_like
    )
}

private fun formatTimestamp(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000}d"
        diff < 2592000_000 -> "${diff / 604800_000}w"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }
}

@BindingAdapter("errorView")
fun View.setErrorView(error: String?) {
    if (!error.isNullOrEmpty()) {
        isVisible = true
        if (this is TextView) {
            text = error
        }
    } else {
        isVisible = false
    }
}

@BindingAdapter("emptyView")
fun View.setEmptyView(isEmpty: Boolean) {
    isVisible = isEmpty
}

@BindingAdapter("loadingView")
fun View.setLoadingView(isLoading: Boolean) {
    isVisible = isLoading
    if (this is ShimmerFrameLayout) {
        if (isLoading) startShimmer() else stopShimmer()
    }
}

@BindingAdapter("contentView")
fun View.setContentView(isContent: Boolean) {
    isVisible = isContent
}
