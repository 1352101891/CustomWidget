package com.meis.widget.refreshview.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.meis.widget.refreshview.entity.base.graph;

/**
 * Created by lvqiu on 2017/10/27.
 */

public class Circle extends graph {
    float radius = 100.0f;
    float x, y;
    float scale = 1.0f;
    float rotation = 0.0f;

    public Circle(int radius, float x, float y) {
        this.radius = radius;
        this.x = x;
        this.y = y;
        Init();
    }


    @Override
    public void Init() {

    }

    @Override
    public void Draw(Canvas canvas) {
        Paint mPaint = new Paint();

        canvas.save();
        mPaint.setColor(Color.BLUE);
        canvas.drawRect(new Rect(100, 100, 300, 300), mPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(30, 200, 200);
        mPaint.setColor(Color.RED);
        canvas.drawRect(new Rect(100, 100, 300, 300), mPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(30);
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(new Rect(100, 100, 300, 300), mPaint);
        canvas.drawRect(100, 100, 0, 0, mPaint);
        canvas.restore();
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

}
