package com.lawstub.funnyclub.javabeans;

/**
 * 控制属性动画的 x y 坐标数值
 */

public class Position {
    private int x;
    private int y;

    public Position(int x,int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
}
