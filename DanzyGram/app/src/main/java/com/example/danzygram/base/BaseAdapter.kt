package com.example.danzygram.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : Any, VB : ViewDataBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseAdapter.BaseViewHolder<VB>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = DataBindingUtil.inflate<VB>(
            LayoutInflater.from(parent.context),
            getLayoutId(),
            parent,
            false
        )
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        val item = getItem(position)
        bind(holder.binding, item, position)
        holder.binding.executePendingBindings()
    }

    abstract fun getLayoutId(): Int
    abstract fun bind(binding: VB, item: T, position: Int)

    class BaseViewHolder<VB : ViewDataBinding>(
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

    fun updateItemAt(position: Int, item: T) {
        if (position in 0 until itemCount) {
            val currentList = currentList.toMutableList()
            currentList[position] = item
            submitList(currentList)
        }
    }

    fun insertItemAt(position: Int, item: T) {
        if (position in 0..itemCount) {
            val currentList = currentList.toMutableList()
            currentList.add(position, item)
            submitList(currentList)
        }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition in 0 until itemCount && toPosition in 0 until itemCount) {
            val currentList = currentList.toMutableList()
            val item = currentList.removeAt(fromPosition)
            currentList.add(toPosition, item)
            submitList(currentList)
        }
    }

    fun swapItems(position1: Int, position2: Int) {
        if (position1 in 0 until itemCount && position2 in 0 until itemCount) {
            val currentList = currentList.toMutableList()
            val item1 = currentList[position1]
            currentList[position1] = currentList[position2]
            currentList[position2] = item1
            submitList(currentList)
        }
    }

    fun replaceItems(items: List<T>) {
        submitList(items)
    }

    fun addItemsAt(position: Int, items: List<T>) {
        if (position in 0..itemCount) {
            val currentList = currentList.toMutableList()
            currentList.addAll(position, items)
            submitList(currentList)
        }
    }

    fun removeItems(items: List<T>) {
        val currentList = currentList.toMutableList()
        currentList.removeAll(items.toSet())
        submitList(currentList)
    }

    fun removeItemsAt(positions: List<Int>) {
        val currentList = currentList.toMutableList()
        positions.sortedDescending().forEach { position ->
            if (position in 0 until itemCount) {
                currentList.removeAt(position)
            }
        }
        submitList(currentList)
    }
}