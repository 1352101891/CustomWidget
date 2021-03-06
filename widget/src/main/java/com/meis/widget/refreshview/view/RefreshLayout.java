package com.meis.widget.refreshview.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.meis.widget.refreshview.entity.base.constans;
import com.meis.widget.refreshview.view.headView.GearView;

/**
 * Created by lvqiu on 2017/10/29.
 */

public class RefreshLayout extends LinearLayout implements NestedScrollingParent {
    NestedScrollingParentHelper mNestedScrollingParentHelper;
    View mTarget;
    GearView mGearView;
    RefreshListener refreshlistener;

    int ScreenWidth, ScreenHeight;
    Scroller mScroller;
    int HeadHeight;
    int minScrollDistance = 7;
    int maxScrollDistance = 20;
    boolean isRefreshing = false;


    public RefreshLayout(Context context) {
        super(context);
        Init();
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }


    public void Init() {
        mScroller = new Scroller(getContext());
        this.setOrientation(VERTICAL);

        ScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        ScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;

        mGearView = new GearView(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenHeight / 8);
        layoutParams.setMargins(0, -ScreenHeight / 8, 0, 0);

        this.addView(mGearView, layoutParams);


        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        this.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ensureTarget();
            }
        });
    }


    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.e("RefreshLayout", "onStartNestedScroll");
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && isTop();
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    /**
     * getScrollY()  ??????0 ????????????????????????????????? ?????????????????????  ?????????0 ?????????????????????????????????
     *
     * @param target
     * @param dx
     * @param dy       dy?????? 0 ???????????????????????? ?????????0 ?????????????????????????????????
     * @param consumed ??????????????????????????????
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        boolean hiddenHead;
        boolean showHead;
        int scrolled = getScrollY();
        boolean istop = isTop();
        hiddenHead = dy > 0 && scrolled < 0 && istop; //  -scrolled<=HeadHeight
        showHead = dy < 0 && scrolled <= 0 && istop; //-scrolled <HeadHeight

        if ((hiddenHead || showHead)) {
            int temp = getResolvedDy(dy);
            ActionGearview(temp);
            consumed[1] = dy;
        }
        Log.e("RefreshLayout", "onNestedPreScroll????????????istop:" + istop + ",????????????:" + (hiddenHead || showHead));
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.e("RefreshLayout", "onStopNestedScroll" + getScrollY());
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        finishGearview();
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {

    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.e("RefreshLayout", "onNestedPreFling");
        if (getScrollY() >= 0) return false;
        fling((int) velocityY);
        return true;
    }


    public boolean isTop() {
        RecyclerView recyclerView = (RecyclerView) mTarget;
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //????????????????????????????????????position
        int firstVisiablePosition = layoutManager.findFirstCompletelyVisibleItemPosition();

        //????????????????????????????????????
        int visibleItemCount = layoutManager.getChildCount();
        //RecyclerView???????????????
        int state = recyclerView.getScrollState();

        if (visibleItemCount > 0 && firstVisiablePosition == 0) {
            return true;
        } else {
            return false;
        }
    }


    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof RecyclerView) {
                    mTarget = child;
                }
            }
        }
        HeadHeight = mGearView.getMeasuredHeight();
    }


    public void ActionGearview(int distance) {
        Log.e("RefreshLayout", "ActionGearview,," + getScrollY());
        if (distance != 0) {
            if (distance < 0) {
                mGearView.pulldown(Math.abs(getScrollY()));
            } else {
                mGearView.pushup(Math.abs(getScrollY()));
            }
            scrollBy(0, distance);
        } else {

        }
    }


    public void finishGearview() {
        Log.e("RefreshLayout", "???????????????????????????" + (-getScrollY() >= HeadHeight));
        if (-getScrollY() >= HeadHeight) {
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY() - HeadHeight, constans.AnimateTime / 5);
            invalidate();
            mGearView.refreshing(constans.RotateTime);

            //????????????????????????????????????????????????
            if (refreshlistener != null)
                refreshlistener.Refresh();
        } else {
            mGearView.free(constans.AnimateTime);
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), constans.AnimateTime / 5);
            invalidate();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mGearView.idle();
                }
            }, constans.AnimateTime / 5);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param dy
     * @return
     */
    public int getResolvedDy(int dy) {
        int distance = 0;
        float f = 0;

        int abs_dy = Math.abs(dy);
        if (abs_dy < minScrollDistance) {
            distance = 0;  //?????????????????????????????????
        } else {
            if (-getScrollY() < HeadHeight * 2) {
                f = Math.abs(getScrollY() / (HeadHeight * 2.0000f));
                distance = (int) (abs_dy * Math.cos((Math.PI / 2) * f));
                if (distance < minScrollDistance) {
                    distance = Math.min(abs_dy, minScrollDistance);
                }
                if (distance > maxScrollDistance) {
                    distance = Math.min(abs_dy, maxScrollDistance);
                }
            } else {
                distance = minScrollDistance / 2;
            }
        }

        if (dy <= 0) {
            distance = -distance;
        } else {
            distance = dy;
        }
        return distance;
    }

    public void fling(int velocityY) {
        mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, 0);
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y > 0) {
            y = 0;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    public void EndFreshing(int delaytime) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mGearView.free(constans.AnimateTime);
                mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), constans.AnimateTime);
                invalidate();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mGearView.idle();
                    }
                }, constans.AnimateTime);
            }
        }, delaytime);
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setIsfreshing(boolean isfreshing) {
        this.isRefreshing = isfreshing;
        if (!isfreshing) {
            EndFreshing(0);
        } else {
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY() - HeadHeight, constans.AnimateTime / 5);
            invalidate();
            mGearView.refreshing(constans.RotateTime);
        }
    }

    public interface RefreshListener {
        public void Refresh();
    }

    public void setRefreshlistener(RefreshListener refreshlistener) {
        this.refreshlistener = refreshlistener;
    }
}
