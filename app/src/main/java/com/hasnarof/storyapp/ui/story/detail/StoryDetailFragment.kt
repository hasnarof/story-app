package com.hasnarof.storyapp.ui.story.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.hasnarof.storyapp.databinding.FragmentStoryDetailBinding
import com.hasnarof.storyapp.domain.model.Story

class StoryDetailFragment : Fragment() {

    private var _binding: FragmentStoryDetailBinding? = null
    private val binding get() = _binding

    companion object {
        const val EXTRA_STORY = "story"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoryDetailBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireActivity()).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val story = arguments?.getParcelable<Story>(EXTRA_STORY)

        binding?.imageStory?.transitionName = "image${story?.id}"
        binding?.tvUserName?.transitionName = "name${story?.id}"

        binding?.imageStory?.let { Glide.with(this).load(story?.photoUrl).into(it) }

        binding?.tvUserName?.text = story?.name
        binding?.tvDescription?.text = story?.description
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}