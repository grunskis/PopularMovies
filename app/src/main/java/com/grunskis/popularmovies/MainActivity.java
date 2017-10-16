package com.grunskis.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.grunskis.popularmovies.models.Movie;
import com.grunskis.popularmovies.utilities.NetworkUtils;
import com.grunskis.popularmovies.utilities.TMDBJsonUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends Activity implements
        MoviePosterAdapter.MoviePosterAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Movie[]> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SORT_ORDER_POPULAR = "popular";
    private static final String SORT_ORDER_TOP_RATED = "top_rated";
    private static final String SORT_ORDER_FAVORITES = "favorites";

    private static final String SHOW_LOADING = "show_loading";

    public static final int API_DATA_LOADER_ID = 1;

    private MoviePosterAdapter mMoviePosterAdapter;

    private View mErrorView;

    private ProgressBar mProgressBar;

    private String mSortOrder = SORT_ORDER_POPULAR;

    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.rv_posters);

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

        loadMoviePosters();

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(SHOW_LOADING, false)) {
                showLoadingSpinner();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_LOADING, isLoading);
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
        URL url = NetworkUtils.buildUrl(mSortOrder);

        Bundle urlBundle = new Bundle();
        urlBundle.putSerializable("url", url);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(API_DATA_LOADER_ID, urlBundle, this);
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

        int titleStringId = getResources().getIdentifier(mSortOrder, "string", getPackageName());
        this.setTitle(getString(titleStringId));

        return super.onOptionsItemSelected(item);
    }

    private void showErrorMessage() {
        mErrorView.setVisibility(View.VISIBLE);
    }

    private void hideErrorMessage() {
        mErrorView.setVisibility(View.INVISIBLE);
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

    @Override
    public Loader<Movie[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Movie[]>(this) {
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
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] movies) {
        hideLoadingSpinner();

        if (movies != null) {
            hideErrorMessage();
            mMoviePosterAdapter.setMovies(movies);
        } else {
            showErrorMessage();
            Log.e(TAG, "Failed to fetch movies");
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        Log.d(TAG, "onLoaderReset");
    }
}
