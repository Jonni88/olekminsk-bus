package com.provans.flowers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.provans.flowers.adapter.ReviewAdapter
import com.provans.flowers.data.FlowerData
import com.provans.flowers.databinding.FragmentReviewsBinding

class ReviewsFragment : Fragment() {

    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!
    private var flowerId: Int = 0
    private lateinit var reviewAdapter: ReviewAdapter

    companion object {
        private const val ARG_FLOWER_ID = "flower_id"
        
        fun newInstance(flowerId: Int): ReviewsFragment {
            return ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FLOWER_ID, flowerId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flowerId = it.getInt(ARG_FLOWER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadReviews()
        
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.addReviewButton.setOnClickListener {
            // TODO: Добавить отзыв
        }
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewAdapter
        }
    }

    private fun loadReviews() {
        val reviews = FlowerData.reviews.filter { it.flowerId == flowerId }
        reviewAdapter.submitList(reviews)
        
        if (reviews.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.reviewCount.text = "${reviews.size} отзывов"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
