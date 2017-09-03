package com.lawstub.funnyclub.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.lawstub.funnyclub.R;
import com.lawstub.funnyclub.adapters.MyViewPagerAdapter;
import com.lawstub.funnyclub.views.MyViewPager;

import cn.bmob.v3.Bmob;

public class HomePageActivity extends AppCompatActivity {
    //声明控件
    TabLayout tabLayout;
    MyViewPager viewPager;
    Toolbar toolBar;
    AppBarLayout appBarLayout;

    MyViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initViews();
        initViewPager();
        setSupportActionBar(toolBar);
    }


    private void initViews() {
        tabLayout = (TabLayout)findViewById(R.id.tab);
        viewPager = (MyViewPager)findViewById(R.id.view_pager);
        toolBar = (Toolbar)findViewById(R.id.tool_bar);
        appBarLayout = (AppBarLayout)findViewById(R.id.appbar);
    }
    //配置viewpager
    private void initViewPager() {
        viewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

}
