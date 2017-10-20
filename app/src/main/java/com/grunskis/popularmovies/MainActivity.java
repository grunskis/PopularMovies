package com.grunskis.popularmovies;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.grunskis.popularmovies.data.MovieContract;
import com.grunskis.popularmovies.models.Movie;
import com.grunskis.popularmovies.utilities.NetworkUtils;
import com.grunskis.popularmovies.utilities.TMDBJsonUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        MoviePosterAdapter.MoviePosterAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName() + "!!!";

    private static final String SORT_ORDER_POPULAR = "popular";
    private static final String SORT_ORDER_TOP_RATED = "top_rated";
    private static final String SORT_ORDER_FAVORITES = "favorites";

    private static final String SHOW_LOADING = "show_loading";
    private static final String CURRENT_SORT_ORDER = "current_sort_order";

    // TODO: 20.10.17 add projection
    public static final int INDEX_MOVIE_ID = 1;
    public static final int INDEX_MOVIE_TITLE = 2;
    public static final int INDEX_MOVIE_RELEASE_DATE = 3;
    public static final int INDEX_MOVIE_POSTER_URL = 4;
    public static final int INDEX_MOVIE_VOTE_AVERAGE = 5;
    public static final int INDEX_MOVIE_PLOT = 6;

    private static final int API_DATA_LOADER_ID = 1;
    private static final int DB_DATA_LOADER_ID = 2;

    private LoaderManager.LoaderCallbacks<Movie[]> apiLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Movie[]>() {

        @Override
        public Loader<Movie[]> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Movie[]>(MainActivity.this) {
                private Movie[] mData;

                @Override
                protected void onReset() {
                    super.onReset();
                    mData = null;
                    cancelLoad();
                }

                @Override
                public void deliverResult(Movie[] data) {
                    mData = data;
                    super.deliverResult(data);
                }

                @Override
                protected void onStartLoading() {

                    Log.d(TAG, "onStartLoading " + mData + " args: " + args);
                    super.onStartLoading();

                    if (mData != null) {
                        deliverResult(mData);
                        return;
                    }

                    showLoadingSpinner();
                    hideErrorMessage();

                    if (takeContentChanged() || mData == null) {
                        forceLoad();
                    }
                }

                @Override
                public Movie[] loadInBackground() {
                    Log.d(TAG, "loadInBackground");

                    URL url = (URL) args.getSerializable("url");
                    if (url == null) {
                        return null;
                    }

                    String responseJSON = null;
                    try {
                        responseJSON = NetworkUtils.getResponseFromHttpUrl(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (responseJSON != null) {
                        return TMDBJsonUtils.jsonToMovieArray(responseJSON);
                    } else {
                        return null;
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
            hideLoadingSpinner();

            if (data != null) {
                hideErrorMessage();
                mMoviePosterAdapter.setMovies(data);
            } else {
                showErrorMessage();
                Log.e(TAG, "Failed to fetch movies");
            }
        }

        @Override
        public void onLoaderReset(Loader<Movie[]> loader) {
            Log.d(TAG, "onLoaderReset");
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> dbLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    showLoadingSpinner();
                    hideErrorMessage();

                    return new CursorLoader(MainActivity.this,
                            MovieContract.FavoriteMovieEntry.CONTENT_URI,
                            null, null, null, null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    Log.d(TAG, "loaded "+data.getCount());

                    hideLoadingSpinner();

                    if (data != null) {
                        hideErrorMessage();
                        Movie[] movies = new Movie[data.getCount()];

                        int i = 0;

                        while (data.moveToNext()) {
                            int id = data.getInt(INDEX_MOVIE_ID);
                            String title = data.getString(INDEX_MOVIE_TITLE);
                            Date releaseDate = new Date(data.getInt(INDEX_MOVIE_RELEASE_DATE));

                            URL posterUrl = null;
                            try {
                                posterUrl = new URL(data.getString(INDEX_MOVIE_POSTER_URL));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }

                            double voteAverage = data.getDouble(INDEX_MOVIE_VOTE_AVERAGE);
                            String plot = data.getString(INDEX_MOVIE_PLOT);

                            Movie movie = new Movie(id, title, releaseDate, posterUrl,
                                    voteAverage, plot);

                            movies[i] = movie;

                            i++;
                        }

                        mMoviePosterAdapter.setMovies(movies);
                    } else {
                        showErrorMessage();
                        Log.e(TAG, "Failed to fetch movies from DB");
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private MoviePosterAdapter mMoviePosterAdapter;
    private RecyclerView mRecyclerView;

    private View mErrorView;

    private ProgressBar mProgressBar;

    private String mSortOrder = SORT_ORDER_POPULAR;

    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_posters);
        mErrorView = findViewById(R.id.ll_error);

        Button mReload = (Button) findViewById(R.id.b_reload);
        mReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoviePosters();
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMoviePosterAdapter = new MoviePosterAdapter(this);
        mRecyclerView.setAdapter(mMoviePosterAdapter);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(SHOW_LOADING, false)) {
                showLoadingSpinner();
            }
            String sortOrder = savedInstanceState.getString(CURRENT_SORT_ORDER);
            if (sortOrder != null) {
                mSortOrder = sortOrder;
            }
        }

        loadMoviePosters();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_LOADING, isLoading);
        outState.putString(CURRENT_SORT_ORDER, mSortOrder);
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    private void loadMoviePosters() {
        mMoviePosterAdapter.setMovies(null);

        int titleStringId = getResources().getIdentifier(mSortOrder, "string", getPackageName());
        this.setTitle(getString(titleStringId));

        LoaderManager loaderManager = getLoaderManager();

        if (SORT_ORDER_FAVORITES.equals(mSortOrder)) {
            loaderManager.restartLoader(DB_DATA_LOADER_ID, null, dbLoaderCallbacks);
        } else {
            URL url = NetworkUtils.buildUrl(mSortOrder);

            Bundle urlBundle = new Bundle();
            urlBundle.putSerializable("url", url);

            loaderManager.restartLoader(API_DATA_LOADER_ID, urlBundle, apiLoaderCallbacks);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_popular:
                mSortOrder = SORT_ORDER_POPULAR;
                break;

            case R.id.show_top_rated:
                mSortOrder = SORT_ORDER_TOP_RATED;
                break;

            case R.id.show_favorites:
                mSortOrder = SORT_ORDER_FAVORITES;
                break;
        }

        item.setChecked(true);

        loadMoviePosters();

        return super.onOptionsItemSelected(item);
    }

    private void showErrorMessage() {
        mErrorView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideErrorMessage() {
        mErrorView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoadingSpinner() {
        mProgressBar.setVisibility(View.VISIBLE);
        isLoading = true;
    }

    private void hideLoadingSpinner() {
        mProgressBar.setVisibility(View.INVISIBLE);
        isLoading = false;
    }

    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }
}
