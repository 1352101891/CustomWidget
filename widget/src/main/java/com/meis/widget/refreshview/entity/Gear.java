package com.meis.widget.refreshview.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.meis.widget.refreshview.entity.base.graph;

/**
 * Created by lvqiu on 2017/10/27.
 */

public class Gear extends graph {
    int x, y;
    int radius;
    int part;
    double perAngle;
    Rectangle[] rectangles;
    int height = 30;
    Paint mPaint;
    int mcolor = Color.WHITE;
    int rotation;
    float scale = 1f;
    int alpha = 255;

    public Gear(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        part = radius / 10;
        Init();
    }

    public Gear(int x, int y, int radius, int part) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.part = part;
        height = 20;
        Init();
    }

    public void Init() {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        perAngle = Math.PI / 180 * (360.0f / part);
        rectangles = new Rectangle[part / 2 + 1];
        for (int i = 0; i <= part; i = i + 2) {
            Rectangle re = new Rectangle();

            re.position[0][0] = (int) (radius * (float) Math.cos(perAngle * i)) + x;
            re.position[0][1] = (int) -(radius * (float) Math.sin(perAngle * i)) + y;
            re.position[1][0] = (int) (radius * (float) Math.cos(perAngle * (i + 1))) + x;
            re.position[1][1] = (int) -(radius * (float) Math.sin(perAngle * (i + 1))) + y;


            int a = re.position[0][0];
            int b = re.position[0][1];
            int a1 = re.position[1][0];
            int b1 = re.position[1][1];

            int dy = (b1 - b);
            int dx = a1 - a;

            float k = -1 * dx * 1.00f / dy;


            double angle = Math.atan(k);
            int dex = (int) (height * (float) Math.abs(Math.cos(angle)));
            int dey = (int) (height * (float) Math.abs(Math.sin(angle)));

            int c = ((i) * (360 / part)) % 360 / 90;
            switch (c) {
                case 0:
                    dey = -dey;
                    break;
                case 1:
                    dex = -dex;
                    dey = -dey;
                    break;
                case 2:
                    dex = -dex;
                    break;
                default:
                    break;
            }

            re.position[2][0] = a + dex;
            re.position[2][1] = b + dey;
            re.position[3][0] = a1 + dex;
            re.position[3][1] = b1 + dey;

            rectangles[i / 2] = re;
        }
    }

    public void setColor(int color) {
        mcolor = color;
        mPaint.setColor(color);
    }

    @Override
    public void Draw(Canvas canvas) {
        canvas.save();
        canvas.rotate(rotation, x, y);
        canvas.scale(scale, scale);
        mPaint.setAlpha(alpha);
        canvas.drawCircle(x, y, radius, mPaint);
        for (int i = 0; i < rectangles.length; i++) {
            rectangles[i].setColor(mcolor);
            rectangles[i].Draw(canvas);
            rectangles[i].setAlpha(alpha);
        }
        canvas.restore();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(alpha);
        canvas.drawCircle(x, y, radius / 3, paint);
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getPart() {
        return part;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getHeight() {
        return height;
    }

}
