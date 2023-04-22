package com.prisyazhnuy.radioplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : Any, VH : BaseViewHolder<T>>(
        context: Context,
        data: List<T> = listOf()
) :
        RecyclerView.Adapter<VH>() {

    protected val context: Context = context.applicationContext
    protected val inflater: LayoutInflater = LayoutInflater.from(context)
    protected val data: MutableList<T> = data.toMutableList()

    /**
     * @return the list.
     */
    val all: MutableList<T>
        get() = data

    /**
     * @return a copy of the list.
     */
    val snapshot: List<T>
        get() = data.toMutableList()

    /**
     * @return item count of this list.
     */
    override fun getItemCount() = data.size

    /**
     * @return an element from the specified [position] the list.
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    fun getItem(position: Int): T = data[position]

    /**
     * @return true if list is empty.
     */
    fun isEmpty() = data.isEmpty()

    /**
     * @return true if list is not empty.
     */
    fun isNotEmpty() = data.isNotEmpty()

    /**
     * Inserts an element into the list.
     */
    fun add(item: T) = data.add(item)

    /**
     * Replace an element position in this list.
     */
    fun replace(oldPosition: Int, newPosition: Int) = data.add(newPosition, remove(oldPosition))

    /**
     * Replaces the element at the specified [position] in this list with the specified [item].
     *
     * @return the element previously at the specified position.
     */
    operator fun set(position: Int, item: T): T = data.set(position, item)

    /**
     * Removes an element at the specified [item] from the list.
     */
    fun remove(item: T) = data.remove(item)

    /**
     * Removes an element at the specified [position] from the list.
     *
     * @return the element that has been removed.
     */
    fun remove(position: Int): T = data.removeAt(position)

    /**
     * Update the items in the list using the [newItems] list and [DiffUtil.Callback].
     */
    fun updateListItems(newItems: List<T>, callback: DiffUtil.Callback) {
        DiffUtil.calculateDiff(callback).dispatchUpdatesTo(this)
        data.clear()
        data.addAll(newItems)
    }

    /**
     * Update the items in the list using the [newItems] list.
     */
    fun updateAllNotify(newObjects: List<T>) {
        clear()
        addAll(newObjects)
        notifyDataSetChanged()
    }

    /**
     * Removes all elements from this list.
     */
    fun clear() {
        data.clear()
    }

    /**
     * Inserts all of the elements in the specified [collection] into this list.
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    fun addAll(collection: Collection<T>) = data.addAll(collection)

    /**
     * Returns the index of the first occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list.
     */
    fun getItemPosition(item: T) = data.indexOf(item)

    /**
     * Inserts an element into the list at the specified [position].
     */
    fun insert(item: T, position: Int) {
        data.add(position, item)
    }

    /**
     * Inserts all of the elements in the specified collection [items] into this list at the specified [position].
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    fun insertAll(items: Collection<T>, position: Int) {
        data.addAll(position, items)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        position.takeUnless { it == RecyclerView.NO_POSITION }?.let { holder.bind(getItem(it)) }
    }
}