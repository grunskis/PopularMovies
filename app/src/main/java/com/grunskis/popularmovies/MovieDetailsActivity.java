package com.grunskis.popularmovies;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.grunskis.popularmovies.data.MovieContract;
import com.grunskis.popularmovies.models.Movie;
import com.grunskis.popularmovies.models.Review;
import com.grunskis.popularmovies.models.Trailer;
import com.grunskis.popularmovies.utilities.NetworkUtils;
import com.grunskis.popularmovies.utilities.TMDBJsonUtils;

import java.net.URL;
import java.text.SimpleDateFormat;

public class MovieDetailsActivity extends AppCompatActivity implements
        TrailerAdapter.TrailerAdapterOnClickHandler {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private static final int TRAILER_LOADER_ID = 1;
    private static final int REVIEW_LOADER_ID = 2;

    private Movie mMovie;

    private ImageView mPosterThumbnail;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private TextView mPlot;

    private boolean mIsFavorite;
    private Menu mMenu;

    private RecyclerView mTrailerList;
    private TrailerAdapter mTrailerAdapter;
    private LoaderManager.LoaderCallbacks<Trailer[]> trailerLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Trailer[]>() {
                private Trailer[] mData;

                @Override
                public Loader<Trailer[]> onCreateLoader(int id, final Bundle args) {
                    return new AsyncTaskLoader<Trailer[]>(MovieDetailsActivity.this) {
                        @Override
                        protected void onStartLoading() {
                            super.onStartLoading();

                            if (mData != null) {
                                deliverResult(mData);
                                return;
                            }

                            if (takeContentChanged() || mData == null) {
                                forceLoad();
                            }
                        }

                        @Override
                        public Trailer[] loadInBackground() {
                            int movieId = args.getInt("movieId");
                            URL url = NetworkUtils.prepareTrailersUrl(movieId);
                            return TMDBJsonUtils.getTrailers(url);
                        }

                        @Override
                        protected void onReset() {
                            super.onReset();
                            mData = null;
                            cancelLoad();
                        }

                        @Override
                        public void deliverResult(Trailer[] data) {
                            mData = data;
                            super.deliverResult(data);
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<Trailer[]> loader, Trailer[] data) {
                    if (data != null) {
                        mTrailerAdapter.setTrailers(data);
                    } else {
                        Log.e(TAG, "Failed to fetch trailers");
                    }
                }

                @Override
                public void onLoaderReset(Loader<Trailer[]> loader) {
                }
            };

    private RecyclerView mReviewList;
    private ReviewAdapter mReviewAdapter;
    private LoaderManager.LoaderCallbacks<Review[]> reviewLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Review[]>() {
                private Review[] mData;

                @Override
                public Loader<Review[]> onCreateLoader(int id, final Bundle args) {
                    return new AsyncTaskLoader<Review[]>(MovieDetailsActivity.this) {
                        @Override
                        protected void onStartLoading() {
                            super.onStartLoading();

                            if (mData != null) {
                                deliverResult(mData);
                                return;
                            }

                            if (takeContentChanged() || mData == null) {
                                forceLoad();
                            }
                        }

                        @Override
                        public Review[] loadInBackground() {
                            int movieId = args.getInt("movieId");
                            URL url = NetworkUtils.prepareReviewsUrl(movieId);
                            return TMDBJsonUtils.getReviews(url);
                        }

                        @Override
                        protected void onReset() {
                            super.onReset();
                            mData = null;
                            cancelLoad();
                        }

                        @Override
                        public void deliverResult(Review[] data) {
                            mData = data;
                            super.deliverResult(data);
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<Review[]> loader, Review[] data) {
                    if (data != null) {
                        mReviewAdapter.setReviews(data);
                    } else {
                        Log.e(TAG, "Failed to fetch trailers");
                    }
                }

                @Override
                public void onLoaderReset(Loader<Review[]> loader) {
                }
            };


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
        mTrailerList.setNestedScrollingEnabled(false);

        mReviewList = (RecyclerView) findViewById(R.id.rv_reviews);
        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
        mReviewList.setLayoutManager(reviewLayoutManager);
        mReviewList.setHasFixedSize(true);
        mReviewList.setNestedScrollingEnabled(false);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

            // TODO: 18.10.17 show some text when there are no trailers available
            LoaderManager loaderManager = getLoaderManager();

            Bundle bundle = new Bundle();
            bundle.putInt("movieId", mMovie.getId());

            mTrailerAdapter = new TrailerAdapter(this);
            mTrailerList.setAdapter(mTrailerAdapter);
            loaderManager.restartLoader(TRAILER_LOADER_ID, bundle, trailerLoaderCallbacks);

            mReviewAdapter = new ReviewAdapter();
            mReviewList.setAdapter(mReviewAdapter);
            loaderManager.restartLoader(REVIEW_LOADER_ID, bundle, reviewLoaderCallbacks);
        }
    }

    @Override
    public void onClick(Trailer trailer) {
        Log.d(TAG, "Trailer clicked: " + trailer.getName());

        Intent intent = new Intent(Intent.ACTION_VIEW, trailer.getYoutubeUri());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);

        if (mIsFavorite) {
            menu.findItem(R.id.favorite_yes_no).setIcon(R.drawable.ic_star_black_24dp);
        }

        mMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite_yes_no:
                String toastMessage;
                if (!mIsFavorite) {
                    addMovieToFavorites(mMovie);
                    mMenu.findItem(R.id.favorite_yes_no).setIcon(R.drawable.ic_star_black_24dp);
                    toastMessage = mMovie.getTitle() + " added to favorites!";
                } else {
                    removeMovieFromFavorites(mMovie);
                    mMenu.findItem(R.id.favorite_yes_no).setIcon(R.drawable.ic_star_border_black_24dp);
                    toastMessage = mMovie.getTitle() + " removed from favorites!";
                }
                mIsFavorite = !mIsFavorite;
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
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