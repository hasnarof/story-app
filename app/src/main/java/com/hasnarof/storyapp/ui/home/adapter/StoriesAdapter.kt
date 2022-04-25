package com.hasnarof.storyapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hasnarof.storyapp.databinding.ItemRowStoryBinding
import com.hasnarof.storyapp.domain.model.Story
import com.hasnarof.storyapp.helper.StoryDiffCallback
import com.hasnarof.storyapp.ui.home.HomeFragmentDirections

class StoriesAdapter: RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

    private val stories = ArrayList<Story>()

    fun setStories(stories: List<Story>) {
        val diffCallback = StoryDiffCallback(this.stories, stories)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.stories.clear()
        this.stories.addAll(stories)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesAdapter.StoryViewHolder {
        val binding = ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesAdapter.StoryViewHolder, position: Int) {
        val story = stories[position]

        Glide.with(holder.itemView.context).load(story.photoUrl).into(holder.binding.imageStory)
        holder.binding.tvUserName.text = story.name

        holder.binding.imageStory.transitionName = "image${stories[position].id}"
        holder.binding.tvUserName.transitionName = "name${stories[position].id}"

        holder.binding.cardView.setOnClickListener {
            val toStoryDetailFragment = HomeFragmentDirections.actionHomeFragmentToStoryDetailFragment(story)
            val extras = FragmentNavigatorExtras(
                holder.binding.imageStory to "image${stories[position].id}",
                holder.binding.tvUserName to "name${stories[position].id}"
            )
            it.findNavController().navigate(toStoryDetailFragment, extras)
        }
    }

    override fun getItemCount(): Int {
        return stories.size
    }


    inner class StoryViewHolder(val binding: ItemRowStoryBinding): RecyclerView.ViewHolder(binding.root)


}