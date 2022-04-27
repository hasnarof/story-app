package com.hasnarof.storyapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hasnarof.storyapp.databinding.ItemRowStoryBinding
import com.hasnarof.storyapp.domain.model.Story
import com.hasnarof.storyapp.helper.StoryDiffCallback
import com.hasnarof.storyapp.ui.home.HomeFragmentDirections

class StoriesAdapter: PagingDataAdapter<Story, StoriesAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    inner class StoryViewHolder(val binding: ItemRowStoryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Story) {
            Glide.with(itemView.context).load(data.photoUrl).into(binding.imageStory)
            binding.tvUserName.text = data.name

            binding.imageStory.transitionName = "image${data.id}"
            binding.tvUserName.transitionName = "name${data.id}"

            binding.cardView.setOnClickListener {
                val toStoryDetailFragment = HomeFragmentDirections.actionHomeFragmentToStoryDetailFragment(data)
                val extras = FragmentNavigatorExtras(
                    binding.imageStory to "image${data.id}",
                    binding.tvUserName to "name${data.id}"
                )
                it.findNavController().navigate(toStoryDetailFragment, extras)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesAdapter.StoryViewHolder {
        val binding = ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesAdapter.StoryViewHolder, position: Int) {
        val data = getItem(position)
        if(data != null) {
            holder.bind(data)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }


}