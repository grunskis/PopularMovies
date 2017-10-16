package com.grunskis.popularmovies.utilities;

import android.net.Uri;

import com.grunskis.popularmovies.models.Movie;
import com.grunskis.popularmovies.models.Review;
import com.grunskis.popularmovies.models.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by martins on 26/05/17.
 */

public class TMDBJsonUtils {

    private static final String TMDB_BASE_IMAGE_URL = "https://image.tmdb.org/t/p";
    private static final String TMDB_THUMBNAIL_SIZE = "w600";

    public static Movie[] jsonToMovieArray(String json) {
        Movie[] movies = null;

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);

            JSONArray movieArray = jsonObject.getJSONArray("results");

            movies = new Movie[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movieJSON = movieArray.getJSONObject(i);

                int id = movieJSON.getInt("id");
                String title = movieJSON.getString("title");
                String releaseDate = movieJSON.getString("release_date");
                String posterPath = movieJSON.getString("poster_path");
                Double averageVote = movieJSON.getDouble("vote_average");
                String plot = movieJSON.getString("overview");

                URL trailersUrl = NetworkUtils.prepareTrailersUrl(id);
                URL reviewsUrl = NetworkUtils.prepareReviewsUrl(id);

                movies[i] = new Movie(id, title,
                        prepareRelaseDate(releaseDate),
                        preparePosterUrl(posterPath),
                        averageVote, plot,
                        prepareTrailers(trailersUrl),
                        prepareReviews(reviewsUrl));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movies;
    }

    private static Date prepareRelaseDate(String releaseDate) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(releaseDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private static URL preparePosterUrl(String posterPath) {
        Uri builtUri = Uri.parse(TMDB_BASE_IMAGE_URL)
                .buildUpon()
                .appendPath(TMDB_THUMBNAIL_SIZE)
                .appendPath(posterPath.substring(1))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    private static Trailer[] prepareTrailers(URL trailersUrl) {
        Trailer[] trailers = null;

        String json = null;
        try {
            json = NetworkUtils.getResponseFromHttpUrl(trailersUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);

            JSONArray resultsArray = jsonObject.getJSONArray("results");

            trailers = new Trailer[resultsArray.length()];
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject objectJSON = resultsArray.getJSONObject(i);

                String name = objectJSON.getString("name");
                String key = objectJSON.getString("key");

                trailers[i] = new Trailer(name, key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return trailers;
    }

    private static Review[] prepareReviews(URL reviewsUrl) {
        Review[] reviews = null;

        String json = null;
        try {
            json = NetworkUtils.getResponseFromHttpUrl(reviewsUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);

            JSONArray resultsArray = jsonObject.getJSONArray("results");

            reviews = new Review[resultsArray.length()];
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject objectJSON = resultsArray.getJSONObject(i);

                String author = objectJSON.getString("author");
                String content = objectJSON.getString("content");

                reviews[i] = new Review(author, content);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return reviews;
    }
}
