package com.example.danzygram.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T : Any, VB : ViewDataBinding>(
    protected val binding: VB
) : RecyclerView.ViewHolder(binding.root) {

    private var item: T? = null

    open fun bind(item: T) {
        this.item = item
        binding.executePendingBindings()
    }

    open fun bind(item: T, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            bind(item)
        }
    }

    protected fun getItem(): T? = item

    open fun onViewAttachedToWindow() {
        // Override in child classes if needed
    }

    open fun onViewDetachedFromWindow() {
        // Override in child classes if needed
    }

    open fun onViewRecycled() {
        // Override in child classes if needed
    }

    protected fun getString(resId: Int): String {
        return binding.root.context.getString(resId)
    }

    protected fun getString(resId: Int, vararg formatArgs: Any): String {
        return binding.root.context.getString(resId, *formatArgs)
    }

    protected fun getColor(resId: Int): Int {
        return binding.root.context.getColor(resId)
    }

    protected fun getDimension(resId: Int): Float {
        return binding.root.context.resources.getDimension(resId)
    }

    protected fun getDimensionPixelSize(resId: Int): Int {
        return binding.root.context.resources.getDimensionPixelSize(resId)
    }

    protected fun getDrawable(resId: Int) = binding.root.context.getDrawable(resId)

    protected fun dpToPx(dp: Float): Int {
        val scale = binding.root.context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    protected fun pxToDp(px: Float): Float {
        val scale = binding.root.context.resources.displayMetrics.density
        return px / scale
    }

    protected fun getAdapterPosition(): Int {
        return if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
            bindingAdapterPosition
        } else {
            RecyclerView.NO_POSITION
        }
    }

    protected fun isFirstItem(): Boolean {
        return getAdapterPosition() == 0
    }

    protected fun isLastItem(): Boolean {
        val adapter = bindingAdapter
        return adapter != null && getAdapterPosition() == adapter.itemCount - 1
    }

    protected fun getItemCount(): Int {
        return bindingAdapter?.itemCount ?: 0
    }

    protected fun notifyItemChanged() {
        bindingAdapter?.notifyItemChanged(getAdapterPosition())
    }

    protected fun notifyItemChanged(payload: Any) {
        bindingAdapter?.notifyItemChanged(getAdapterPosition(), payload)
    }

    protected fun notifyItemRemoved() {
        bindingAdapter?.notifyItemRemoved(getAdapterPosition())
    }

    protected fun notifyItemMoved(toPosition: Int) {
        bindingAdapter?.notifyItemMoved(getAdapterPosition(), toPosition)
    }

    protected fun notifyDataSetChanged() {
        bindingAdapter?.notifyDataSetChanged()
    }
}