package com.example.danzygram.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseBindingAdapter<T : Any, VB : ViewDataBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseBindingAdapter.BaseBindingViewHolder<VB>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<VB> {
        val binding = DataBindingUtil.inflate<VB>(
            LayoutInflater.from(parent.context),
            getLayoutId(viewType),
            parent,
            false
        )
        return BaseBindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder<VB>, position: Int) {
        val item = getItem(position)
        bind(holder.binding, item, position)
        holder.binding.executePendingBindings()
    }

    override fun onBindViewHolder(
        holder: BaseBindingViewHolder<VB>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = getItem(position)
            bindPayload(holder.binding, item, position, payloads)
            holder.binding.executePendingBindings()
        }
    }

    override fun onViewAttachedToWindow(holder: BaseBindingViewHolder<VB>) {
        super.onViewAttachedToWindow(holder)
        onViewHolderAttached(holder)
    }

    override fun onViewDetachedFromWindow(holder: BaseBindingViewHolder<VB>) {
        super.onViewDetachedFromWindow(holder)
        onViewHolderDetached(holder)
    }

    override fun onViewRecycled(holder: BaseBindingViewHolder<VB>) {
        super.onViewRecycled(holder)
        onViewHolderRecycled(holder)
    }

    abstract fun getLayoutId(viewType: Int): Int

    abstract fun bind(binding: VB, item: T, position: Int)

    open fun bindPayload(binding: VB, item: T, position: Int, payloads: MutableList<Any>) {
        // Override in child classes if needed
    }

    open fun onViewHolderAttached(holder: BaseBindingViewHolder<VB>) {
        // Override in child classes if needed
    }

    open fun onViewHolderDetached(holder: BaseBindingViewHolder<VB>) {
        // Override in child classes if needed
    }

    open fun onViewHolderRecycled(holder: BaseBindingViewHolder<VB>) {
        // Override in child classes if needed
    }

    class BaseBindingViewHolder<VB : ViewDataBinding>(
        val binding: VB
    ) : RecyclerView.ViewHolder(binding.root)

    fun updateItems(items: List<T>) {
        submitList(items.toMutableList())
    }

    fun addItem(item: T) {
        val currentList = currentList.toMutableList()
        currentList.add(item)
        submitList(currentList)
    }

    fun addItems(items: List<T>) {
        val currentList = currentList.toMutableList()
        currentList.addAll(items)
        submitList(currentList)
    }

    fun removeItem(item: T) {
        val currentList = currentList.toMutableList()
        currentList.remove(item)
        submitList(currentList)
    }

    fun removeItemAt(position: Int) {
        if (position in 0 until itemCount) {
            val currentList = currentList.toMutableList()
            currentList.removeAt(position)
            submitList(currentList)
        }
    }

    fun clearItems() {
        submitList(emptyList())
    }

    fun getItemAtPosition(position: Int): T? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    fun getCurrentItems(): List<T> {
        return currentList
    }

    fun isEmpty(): Boolean {
        return itemCount == 0
    }

    fun isNotEmpty(): Boolean {
        return itemCount > 0
    }

    fun getPosition(item: T): Int {
        return currentList.indexOf(item)
    }
}