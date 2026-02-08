package com.provans.flowers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.provans.flowers.R
import com.provans.flowers.model.Review

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val authorView: TextView = itemView.findViewById(R.id.authorName)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val textView: TextView = itemView.findViewById(R.id.reviewText)
        private val dateView: TextView = itemView.findViewById(R.id.reviewDate)

        fun bind(review: Review) {
            authorView.text = review.authorName
            ratingBar.rating = review.rating
            textView.text = review.text
            dateView.text = review.date
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
}
