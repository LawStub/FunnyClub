package com.lawstub.funnyclub.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lawstub.funnyclub.fragments.FunnyPageFragment;
import com.lawstub.funnyclub.fragments.JokesFragment;
import com.lawstub.funnyclub.fragments.MyFavoriteFragment;
import com.lawstub.funnyclub.fragments.VideoFragment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by 廖婵001 on 2017/8/25 0025.
 */

public class MyViewPagerAdapter extends FragmentPagerAdapter {

    List<Fragment> fragments = new ArrayList<>();

    //tab标题
    String[] titles = {"搞笑图片","笑话","视频","我的收藏"};

    public MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
        Fragment fragment0 = new FunnyPageFragment();
        Fragment fragment1 = new JokesFragment();
        Fragment fragment2 = new VideoFragment();
        Fragment fragment3 = new MyFavoriteFragment();
        Observable.just(fragment0,fragment1,fragment2,fragment3)
                .subscribe(new Action1<Fragment>() {
                    @Override
                    public void call(Fragment fragment) {
                        fragments.add(fragment);
                    }
                });
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
