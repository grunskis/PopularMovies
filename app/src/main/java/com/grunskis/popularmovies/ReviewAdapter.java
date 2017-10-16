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

    public ReviewAdapter(Review[] reviews) {
        mReviews = reviews;
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
        return mReviews.length;
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView mAuthor;
        public final TextView mContent;

        public ReviewAdapterViewHolder(View itemView) {
            super(itemView);

            // TODO: 18.10.17 use binding?
            mAuthor = (TextView) itemView.findViewById(R.id.tv_author);
            mContent = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }
}
