package com.grunskis.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grunskis.popularmovies.models.Trailer;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private static final String TAG = TrailerAdapter.class.getSimpleName();

    private Trailer[] mTrailers;

    private final TrailerAdapterOnClickHandler mClickHandler;

    public TrailerAdapter(Trailer[] trailers, TrailerAdapterOnClickHandler clickHandler) {
        mTrailers = trailers;
        mClickHandler = clickHandler;
    }

    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapterViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called " + position);
        holder.mName.setText(mTrailers[position].getName());
    }

    @Override
    public int getItemCount() {
        if (mTrailers != null) {
            return mTrailers.length;
        } else {
            return 0;
        }
    }

    public interface TrailerAdapterOnClickHandler {
        void onClick(Trailer trailer);
    }

    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final TextView mName;

        public TrailerAdapterViewHolder(View itemView) {
            super(itemView);

            // TODO: 18.10.17 use binding?
            mName = (TextView) itemView.findViewById(R.id.tv_trailer);
            mName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Trailer trailer = mTrailers[getAdapterPosition()];
            mClickHandler.onClick(trailer);
        }
    }
}
