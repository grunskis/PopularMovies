package com.grunskis.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grunskis.popularmovies.models.Review;
import com.grunskis.popularmovies.models.Trailer;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private static final String TAG = ReviewAdapter.class.getSimpleName();

    private Review[] mReviews;

    public void setReviews(Review[] reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.review_list_item, parent, false);
        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {
        Review review = mReviews[position];
        holder.mAuthor.setText(review.getAuthor());
        holder.mContent.setText(review.getContent());
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) {
            return 0;
        } else {
            return mReviews.length;
        }
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView mAuthor;
        public final TextView mContent;

        public ReviewAdapterViewHolder(View itemView) {
            super(itemView);

            mAuthor = (TextView) itemView.findViewById(R.id.tv_author);
            mContent = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }
}
