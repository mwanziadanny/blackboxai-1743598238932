package com.example.danzygram.util

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

object ResourceUtil {
    fun getString(context: Context, @StringRes resId: Int): String {
        return context.getString(resId)
    }

    fun getString(context: Context, @StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    fun getStringArray(context: Context, @ArrayRes resId: Int): Array<String> {
        return context.resources.getStringArray(resId)
    }

    fun getQuantityString(
        context: Context,
        @PluralsRes resId: Int,
        quantity: Int,
        vararg formatArgs: Any
    ): String {
        return context.resources.getQuantityString(resId, quantity, *formatArgs)
    }

    @ColorInt
    fun getColor(context: Context, @ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    fun getColorStateList(context: Context, @ColorRes resId: Int): ColorStateList? {
        return ContextCompat.getColorStateList(context, resId)
    }

    fun getDrawable(context: Context, @DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }

    fun getDimension(context: Context, @DimenRes resId: Int): Float {
        return context.resources.getDimension(resId)
    }

    fun getDimensionPixelSize(context: Context, @DimenRes resId: Int): Int {
        return context.resources.getDimensionPixelSize(resId)
    }

    fun getDimensionPixelOffset(context: Context, @DimenRes resId: Int): Int {
        return context.resources.getDimensionPixelOffset(resId)
    }

    fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    fun pxToDp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }

    fun spToPx(sp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    fun pxToSp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.scaledDensity
    }

    fun isNightMode(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun getScreenDensity(): Float {
        return Resources.getSystem().displayMetrics.density
    }

    fun getScreenDensityDpi(): Int {
        return Resources.getSystem().displayMetrics.densityDpi
    }

    fun getResourceName(context: Context, resId: Int): String {
        return context.resources.getResourceName(resId)
    }

    fun getResourceTypeName(context: Context, resId: Int): String {
        return context.resources.getResourceTypeName(resId)
    }

    fun getResourceEntryName(context: Context, resId: Int): String {
        return context.resources.getResourceEntryName(resId)
    }

    fun getResourcePackageName(context: Context, resId: Int): String {
        return context.resources.getResourcePackageName(resId)
    }

    fun getThemeAttributeValue(context: Context, attrId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }

    fun getFont(context: Context, fontId: Int): android.graphics.Typeface? {
        return ResourcesCompat.getFont(context, fontId)
    }

    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun getNavigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun getActionBarHeight(context: Context): Int {
        val tv = TypedValue()
        return if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        } else {
            0
        }
    }

    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.screenLayout and 
            Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}