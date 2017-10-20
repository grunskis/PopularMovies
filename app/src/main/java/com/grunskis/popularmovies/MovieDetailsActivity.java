package com.grunskis.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grunskis.popularmovies.data.MovieContract;
import com.grunskis.popularmovies.models.Movie;
import com.grunskis.popularmovies.models.Review;
import com.grunskis.popularmovies.models.Trailer;

import java.text.SimpleDateFormat;

public class MovieDetailsActivity extends Activity implements
        TrailerAdapter.TrailerAdapterOnClickHandler {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private Movie mMovie;

    private ImageView mPosterThumbnail;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private TextView mPlot;

    private boolean mIsFavorite;

    private RecyclerView mTrailerList;
    private TrailerAdapter mTrailerAdapter;

    private RecyclerView mReviewList;
    private ReviewAdapter mReviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mPosterThumbnail = (ImageView) findViewById(R.id.iv_poster_thumbnail);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        mVoteAverage = (TextView) findViewById(R.id.tv_vote_average);
        mPlot = (TextView) findViewById(R.id.tv_plot);

        mTrailerList = (RecyclerView) findViewById(R.id.rv_trailers);
        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(this);
        mTrailerList.setLayoutManager(trailerLayoutManager);
        mTrailerList.setHasFixedSize(true);

        mReviewList = (RecyclerView) findViewById(R.id.rv_reviews);
        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
        mReviewList.setLayoutManager(reviewLayoutManager);
        mReviewList.setHasFixedSize(true);

        Intent intent = getIntent();
        if (intent.hasExtra("movie")) {
            mMovie = intent.getParcelableExtra("movie");

            mIsFavorite = isFavorite(mMovie);

            this.setTitle(mMovie.getTitle());

            Glide.with(this).load(mMovie.getPosterUrl()).into(mPosterThumbnail);

            mTitle.setText(mMovie.getTitle());
            String releaseDate = new SimpleDateFormat("yyyy").format(mMovie.getReleaseDate());
            mReleaseDate.setText(releaseDate);
            Double rating = mMovie.getVoteAverage() * 10;
            mVoteAverage.setText(String.valueOf(rating.intValue()) + '%');
            mPlot.setText(mMovie.getPlot());

//            Trailer[] trailers = mMovie.getTrailers();
//            mTrailerAdapter = new TrailerAdapter(trailers, this);
//            mTrailerList.setAdapter(mTrailerAdapter);
//            // TODO: 18.10.17 show some text when there are no trailers available
//
//            Review[] reviews = mMovie.getReviews();
//            mReviewAdapter = new ReviewAdapter(reviews);
//            mReviewList.setAdapter(mReviewAdapter);
        }
    }

    @Override
    public void onClick(Trailer trailer) {
        Log.d(TAG, "Trailer clicked: " + trailer.getName());

        Intent intent = new Intent(Intent.ACTION_VIEW, trailer.getYoutubeUri());
        // TODO: 18.10.17 https://stackoverflow.com/a/12439378
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.favorite_yes_no) {
            if (!mIsFavorite) {
                addMovieToFavorites(mMovie);
            } else {
                removeMovieFromFavorites(mMovie);
            }
            // TODO: 19.10.17 update icon
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isFavorite(Movie movie) {
        String selection = "movie_id = ?";
        String selectionArgs[] = new String[]{String.valueOf(movie.getId())};
        Cursor cursor = getContentResolver().query(MovieContract.FavoriteMovieEntry.CONTENT_URI,
                null, selection, selectionArgs, null);
        return cursor != null && cursor.getCount() > 0;
    }

    private void addMovieToFavorites(Movie movie) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.FavoriteMovieEntry.COL_MOVIE_ID, movie.getId());
        contentValues.put(MovieContract.FavoriteMovieEntry.COL_TITLE, movie.getTitle());
        contentValues.put(MovieContract.FavoriteMovieEntry.COL_RELEASE_DATE,
                movie.getReleaseDate().getTime());
        contentValues.put(MovieContract.FavoriteMovieEntry.COL_POSTER_URL,
                movie.getPosterUrl().toString());
        contentValues.put(MovieContract.FavoriteMovieEntry.COL_VOTE_AVERAGE,
                movie.getVoteAverage());
        contentValues.put(MovieContract.FavoriteMovieEntry.COL_PLOT, movie.getPlot());
        getContentResolver().insert(MovieContract.FavoriteMovieEntry.CONTENT_URI, contentValues);
    }

    private void removeMovieFromFavorites(Movie movie) {
        Uri uri = MovieContract.FavoriteMovieEntry.CONTENT_URI
                        .buildUpon()
                        .appendPath(String.valueOf(movie.getId()))
                        .build();
        getContentResolver().delete(uri, null, null);
    }
}