package com.lawstub.funnyclub.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lawstub.funnyclub.R;
import com.lawstub.funnyclub.javabeans.Jokes;

import java.util.List;

/**
 * Created by 廖婵001 on 2017/8/27 0027.
 */

public class JokesRecyclerViewAdpter extends RecyclerView.Adapter<JokesRecyclerViewAdpter.ViewHolder> {

    private static final String TAG = "JokesAdapter";

    private List<Jokes> mJokes;

    public JokesRecyclerViewAdpter(List<Jokes> jokes){
        mJokes = jokes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.jokes_page_view_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Jokes joke = mJokes.get(position);
        holder.content.setText(joke.getContent());
        holder.title.setText(joke.getTitle());
        Log.d(TAG,"onBindViewHolder position="+position);
    }

    @Override
    public int getItemCount() {
        return mJokes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private TextView content;
        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.joke_title);
            content = (TextView)itemView.findViewById(R.id.joke_content);
        }
    }
}
