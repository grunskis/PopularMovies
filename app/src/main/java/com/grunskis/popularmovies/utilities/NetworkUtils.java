package com.grunskis.popularmovies.utilities;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.grunskis.popularmovies.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    final private static String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie";

    final private static String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;

    final private static String PARAM_API_KEY = "api_key";

    public static URL buildUrl(String movieList) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL)
                .buildUpon()
                .appendPath(movieList)
                .appendQueryParameter(PARAM_API_KEY, TMDB_API_KEY)
                .build();

        Log.d(NetworkUtils.class.getName(), builtUri.toString());

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    @Nullable
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private static URL prepareMovieResourceUrl(int movieId, String resource) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL)
                .buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(resource)
                .appendQueryParameter(PARAM_API_KEY, TMDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL prepareTrailersUrl(int movieId) {
        return prepareMovieResourceUrl(movieId, "videos");
    }

    public static URL prepareReviewsUrl(int movieId) {
        return prepareMovieResourceUrl(movieId, "reviews");
    }
}
