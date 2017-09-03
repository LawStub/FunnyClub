package com.lawstub.funnyclub.views;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by 廖婵001 on 2017/8/26 0026.
 */

public class MyViewPager extends ViewPager implements NestedScrollingChild {
    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {

    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return true;
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return false;
    }

    @Override
    public void stopNestedScroll() {

    }

    @Override
    public boolean hasNestedScrollingParent() {
        return false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return false;
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return false;
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return false;
    }
}
