package com.grunskis.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.grunskis.popularmovies.models.Movie;

import java.net.URL;

public class MoviePosterAdapter extends
        RecyclerView.Adapter<MoviePosterAdapter.MoviePosterAdapterViewHolder> {

    private Movie[] mMovies;

    private final MoviePosterAdapterOnClickHandler mClickHandler;

    public MoviePosterAdapter(MoviePosterAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public MoviePosterAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.poster, parent, false);
        return new MoviePosterAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviePosterAdapterViewHolder holder, int position) {
        URL posterURL = mMovies[position].getPosterUrl();
        Glide.with(holder.mImageView).load(posterURL).into(holder.mImageView);
        // TODO: 18.10.17 check if caching is enabled by default
    }

    @Override
    public int getItemCount() {
        if (mMovies != null) {
            return mMovies.length;
        } else {
            return 0;
        }
    }

    public void setMovies(Movie[] movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    public class MoviePosterAdapterViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final ImageView mImageView;

        public MoviePosterAdapterViewHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.iv_poster);

            mImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Movie movie = mMovies[getAdapterPosition()];
            mClickHandler.onClick(movie);
        }
    }

    public interface MoviePosterAdapterOnClickHandler {
        void onClick(Movie movie);
    }
}
