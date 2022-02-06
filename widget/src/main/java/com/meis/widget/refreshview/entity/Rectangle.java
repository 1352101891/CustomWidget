package com.meis.widget.refreshview.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.meis.widget.refreshview.entity.base.graph;

/**
 * Created by lvqiu on 2017/10/27.
 */

public class Rectangle extends graph {
    public int[][] position = new int[4][2];
    public int color;
    public int alpha = 255;


    @Override
    public void Init() {

    }

    @Override
    public void Draw(Canvas canvas) {
        drawgraph(canvas);
//        drawLine(canvas);
    }

    public void drawgraph(Canvas canvas) {
        Paint mField = new Paint();
        mField.setAntiAlias(false);
        Path mFieldPath = new Path();
        mFieldPath.moveTo(position[0][0], position[0][1]);
        mFieldPath.lineTo(position[1][0], position[1][1]);
        mFieldPath.lineTo(position[3][0], position[3][1]);
        mFieldPath.lineTo(position[2][0], position[2][1]);
        mFieldPath.close();
        mField.setColor(color);
        mField.setAlpha(alpha);
        canvas.drawPath(mFieldPath, mField);
    }

    public void drawLine(Canvas canvas) {
        Paint mField = new Paint();
        mField.setColor(Color.RED);
        canvas.drawLine(position[0][0], position[0][1], position[1][0], position[1][1], mField);
        canvas.drawLine(position[1][0], position[1][1], position[3][0], position[3][1], mField);
        canvas.drawLine(position[0][0], position[0][1], position[2][0], position[2][1], mField);
        canvas.drawLine(position[2][0], position[2][1], position[3][0], position[3][1], mField);
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
