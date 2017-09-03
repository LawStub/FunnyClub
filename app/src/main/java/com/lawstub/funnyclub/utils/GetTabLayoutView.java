package com.lawstub.funnyclub.utils;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.view.View;

import com.lawstub.funnyclub.R;

import java.lang.reflect.Field;

/**
 * Created by 廖婵001 on 2017/9/3 0003.
 */

public class GetTabLayoutView {

    public static View getTabLayoutChildViewAtIndex(Activity activity,int index){
        View tabView = null;
        TabLayout.Tab tab = ((TabLayout)activity.findViewById(R.id.tab)).getTabAt(index);
        Field view = null;
        try{
            view = TabLayout.Tab.class.getDeclaredField("mView");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        view.setAccessible(true);
        try {
            tabView = (View)view.get(tab);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return tabView;
    }

}
