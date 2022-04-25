package com.hasnarof.storyapp.helper

import androidx.recyclerview.widget.DiffUtil
import com.hasnarof.storyapp.domain.model.Story

class StoryDiffCallback(private val mOldStories: List<Story>, private val mNewStories: List<Story>): DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return mOldStories.size
    }

    override fun getNewListSize(): Int {
        return mNewStories.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldStories[oldItemPosition].id == mNewStories[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = mOldStories[oldItemPosition]
        val newItem = mNewStories[newItemPosition]
        return oldItem.name == newItem.name && oldItem.description == newItem.description && oldItem.photoUrl == newItem.photoUrl
    }
}