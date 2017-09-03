package com.lawstub.funnyclub.animator_utlis;

import android.animation.TypeEvaluator;
import android.util.Log;

import com.lawstub.funnyclub.javabeans.Position;

/**
 * Created by 廖婵001 on 2017/9/1 0001.
 */

public class PositionEvaluator implements TypeEvaluator<Position> {

    private static final String TAG = "PositionEvaluator";
    @Override
    public Position evaluate(float fraction, Position startValue, Position endValue) {
        int x = startValue.getX() + (int)(fraction*(endValue.getX()-startValue.getX()));
        int y = startValue.getY() + (int)(fraction*(endValue.getY()-startValue.getY()));
        Position position = new Position(x,y);
        Log.d(TAG,"fraction=" +fraction);
        return position;
    }
}
