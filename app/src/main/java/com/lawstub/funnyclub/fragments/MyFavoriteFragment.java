package com.lawstub.funnyclub.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lawstub.funnyclub.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFavoriteFragment extends Fragment {


    public MyFavoriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_favorite, container, false);
    }

}
