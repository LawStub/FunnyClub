package com.lawstub.funnyclub.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lawstub.funnyclub.R;
import com.lawstub.funnyclub.adapters.JokesRecyclerViewAdpter;
import com.lawstub.funnyclub.javabeans.Jokes;
import com.lawstub.funnyclub.utils.NetworkStateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SQLQueryListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class JokesFragment extends Fragment {

    private static final String TAG = "JokesFragment";

    private RecyclerView recyclerView;
    private TextView networkInfo;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipe;

    private boolean isNetworkAvailable;
    private boolean isFirstQueryFromBmob =true;

    private JokesRecyclerViewAdpter adapter;

    private List<Jokes> jokesList;

    private Context mContext;

    private static final String BMOB_KEY = "5ade83b03a1f7abdc8e9a63b5f773987";


    public JokesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_jokes, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.joke_recyclerview);
        networkInfo = (TextView)view.findViewById(R.id.joke_networkinfo);
        progressBar = (ProgressBar)view.findViewById(R.id.joke_progressbar);
        swipe = (SwipeRefreshLayout)view.findViewById(R.id.joke_swipe) ;
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryJokesFromBmob(true);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(manager);
        adapter = new JokesRecyclerViewAdpter(jokesList);
        recyclerView.setAdapter(adapter);
        if(isFirstQueryFromBmob && isNetworkAvailable){
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG,"queryFromBmob");
            queryJokesFromBmob(false);
        }else {
            networkInfo.setVisibility(View.VISIBLE);
        }
        Log.d(TAG,"onCreateView");
        return view;
    }

    private void queryJokesFromBmob(final boolean isToast) {
        BmobQuery<Jokes> bmobQuery = new BmobQuery<>();
        bmobQuery.doSQLQuery("select * from Jokes", new SQLQueryListener<Jokes>() {
            @Override
            public void done(BmobQueryResult<Jokes> bmobQueryResult, BmobException e) {
                if(bmobQueryResult!=null){
                    List<Jokes> result = bmobQueryResult.getResults();
                    if(isToast){
                        int count = jokesList.size() - result.size();
                        if(count>0){
                            Toast.makeText(mContext,"已更新"+count+"条内容",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(mContext,"没有更新",Toast.LENGTH_SHORT).show();
                        }
                    }
                    jokesList.clear();
                    jokesList.addAll(result);
                    adapter.notifyDataSetChanged();
                    isFirstQueryFromBmob = false;
                    progressBar.setVisibility(View.GONE);
                    if(swipe!=null && swipe.isRefreshing()){
                        swipe.setRefreshing(false);
                    }
                    Log.d(TAG,"onQueryDone result="+ result.toString());
                }else {
                    progressBar.setVisibility(View.GONE);
                    if(swipe!=null && swipe.isRefreshing()){
                        swipe.setRefreshing(false);
                    }
                    Toast.makeText(mContext,"网络出错",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this.getContext(),BMOB_KEY);
        jokesList = new ArrayList<>();
        isNetworkAvailable = NetworkStateUtil.netState(this.getContext());
        mContext = this.getContext();
        Log.d(TAG,"onCreate");
    }
}
