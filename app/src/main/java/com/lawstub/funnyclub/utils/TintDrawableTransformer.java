package com.lawstub.funnyclub.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.lawstub.funnyclub.R;

import java.lang.reflect.Field;

/**
 * tint效果转换器
 */

public class TintDrawableTransformer {
    public static Drawable getLikedDrawble(Context context, int color,int picture, @Nullable PorterDuff.Mode tintMode){
        Drawable drawable = ContextCompat.getDrawable(context, picture);
        Drawable.ConstantState state = drawable.getConstantState();
        Drawable drawable1 = DrawableCompat.wrap(state == null ? drawable : state.newDrawable()).mutate();
        if(tintMode != null){
            DrawableCompat.setTintMode(drawable1,tintMode);
        }
        DrawableCompat.setTint(drawable1,ContextCompat.getColor(context,color));
        return drawable1;
    }
}
