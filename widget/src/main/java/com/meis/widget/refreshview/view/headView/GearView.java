package com.meis.widget.refreshview.view.headView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.meis.widget.refreshview.entity.Gear;
import com.meis.widget.refreshview.entity.base.constans;
import com.meis.widget.refreshview.view.headInterface.GestureAction;

/**
 * Created by lvqiu on 2017/10/27.
 */

public class GearView extends View implements GestureAction {

    ValueAnimator valueAnimator;
    //    Circle mCircle=new Circle(100,100,100);
    Gear mGear;
    Gear mGear2;
    Gear mGear3;
    Gear CenterGear;

    volatile int degree = 0;
    volatile int alpha = 255;

    String status = constans.IDLE;


    public GearView(Context context) {
        super(context);
        Init();
    }

    public GearView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public GearView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }

    public void Init() {
        this.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mGear = new Gear(0, 0, 200);
                mGear2 = new Gear(270, 200, 100);
                mGear3 = new Gear(v.getMeasuredWidth() / 5 * 4, v.getMeasuredHeight() / 2, getMeasuredHeight() / 4 * 3);
                CenterGear = new Gear(v.getMeasuredWidth() / 2, v.getMeasuredHeight() / 2, getMeasuredHeight() / 3, 10);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureDimension(constans.DEFAULT_WIDTH, widthMeasureSpec);
        int height = measureDimension(constans.DEFAULT_HEIGHT, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }


    protected int measureDimension(int defaultValue, int measureSpec) {
        return measureHanlder(defaultValue, measureSpec);
    }


    private int measureHanlder(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("GearView", "onDraw透明度是：" + alpha);

        if (255 - alpha != 0) {  //优化处理
            CenterGear.setRotation(2 * degree);
            CenterGear.setAlpha(255 - alpha);
            CenterGear.setColor(Color.GREEN);
            CenterGear.Draw(canvas);
        }

        if (!status.equals(constans.REFRESHING) && alpha != 0) {  //优化处理
            mGear.setAlpha(alpha);
            mGear.setRotation(degree);
            mGear.setColor(Color.BLUE);
            mGear.Draw(canvas);

            mGear2.setAlpha(alpha);
            mGear2.setRotation(-degree * mGear.getPart() / mGear2.getPart());
            mGear2.setColor(Color.RED);
            mGear2.Draw(canvas);

            mGear3.setAlpha(alpha);
            mGear3.setRotation((int) (degree * mGear.getPart() * 1.00f / mGear3.getPart()));
            mGear3.setColor(Color.GRAY);
            mGear3.Draw(canvas);
        }

        if (status.equals(constans.REFRESHING) || status.equals(constans.FREE)) {
            invalidate();
        }

    }

    private void FreeAnimate(int time) {
        clearAnimation();
        valueAnimator = InitAnimation(time);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.e("GearView", "FreeAnimate");
                float value = (float) animation.getAnimatedValue();
                degree = (int) value;
                alpha = 255;

            }
        });
        valueAnimator.start();
    }

    private void refreshAnimation(int time) {
        clearAnimation();
        valueAnimator = InitAnimation(time);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.e("GearView", "refreshAnimation");
                float value = (float) animation.getAnimatedValue();
                degree = (int) value;
                alpha = 0;

            }
        });
        valueAnimator.start();
        invalidate();
    }

    public ValueAnimator InitAnimation(int time) {
        ValueAnimator value_Animator;
        value_Animator = ValueAnimator.ofFloat(0, 360f);
        value_Animator.setTarget(this);
        value_Animator.setDuration(time);
        value_Animator.setInterpolator(new LinearInterpolator());
        value_Animator.setRepeatMode(ValueAnimator.RESTART);
        value_Animator.setRepeatCount(ValueAnimator.INFINITE);
        return value_Animator;
    }

    public void clearAnimation() {
        if (valueAnimator != null)
            valueAnimator.end();
    }


    @Override
    public void pulldown(int position) {
        Log.e("GearView", "pulldown:");
        if (position < 0) {
            return;
        }
        degree = degree - 2;
        if (degree <= -360) {
            degree = 0;
        }
        alpha = getResolveValue(position);
        Log.e("GearView", "alpha:" + alpha);

        this.status = constans.PULLDOWN;
        invalidate();
    }

    @Override
    public void pushup(int position) {
        Log.e("GearView", "pushup:");
        if (position < 0) {
            return;
        }
        degree = degree + 2;
        if (degree >= 360) {
            degree = 0;
        }
        alpha = getResolveValue(position);
        this.status = constans.PUSHUP;

        invalidate();
    }

    @Override
    public void free(int duration) {
        Log.e("GearView", "free:");
        this.status = constans.FREE;
        FreeAnimate(constans.RotateTime / 2);
    }

    @Override
    public void refreshing(int duration) {
        Log.e("GearView", "refreshing:");
        this.status = constans.REFRESHING;
        refreshAnimation(constans.RotateTime / 2);
    }

    @Override
    public void backTo(int position, int destination) {
        this.status = constans.FREE;
    }

    @Override
    public void idle() {
        Log.e("GearView", "idle:");
        alpha = 255;
        degree = 0;
        this.status = constans.IDLE;
        clearAnimation();
    }


    public int getResolveValue(int position) {
        int alpha = 0;
        if (position < getMeasuredHeight()) {
            alpha = 255;
        } else if (position > 2 * getMeasuredHeight()) {
            alpha = 0;
        } else {
            alpha = (int) (255 * (1 - (position - getMeasuredHeight()) / (getMeasuredHeight() * 1.00f)));
        }
        if (alpha <= 0) {
            alpha = 0;
        }
        return alpha;
    }
}
