package com.lawstub.funnyclub.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lawstub.funnyclub.R;

/**
 * 显示收藏提示框
 */

public class ShowStorePopupWindow {

    private static final String TAG = "ShowPopupWindow";

    private static PopupWindow popupWindow;

    private static View popupView;

    private static TextView textView;


    public static void showPopupWindow(Context context,View target,@Nullable String nofityContent){
        if(popupView == null){
            popupView = LayoutInflater.from(context).inflate(R.layout.popuwindow_below_tablayout,null);
            textView = (TextView) (popupView.findViewById(R.id.content));
        }
        if(nofityContent!=null){
            textView.setText(nofityContent);
        }
        if(popupWindow == null){
           popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, 80,false);
        }
        if(popupWindow.isShowing()){
            return;
        }
        popupWindow.update();
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(target);
    }

    public static void canclePopupWindow(){
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
    }


}
