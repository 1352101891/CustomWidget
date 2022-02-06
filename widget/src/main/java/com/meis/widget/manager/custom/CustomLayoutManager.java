package com.meis.widget.manager.custom;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import java.util.List;

public class CustomLayoutManager extends RecyclerView.LayoutManager {
    private final static String TAG = "customLayoutManager";

    /**
     * 屏幕可见第一个view的position
     */
    private int mFirstVisiPos = -1;

    /**
     * 屏幕可见的最后一个view的position
     */
    private int mLastVisiPos = -1;

    private final OrientationHelper horizonOrientationHelper;

    /**
     * 是否自动选中
     */
    private boolean isAutoSelect = false;

    private View targetView = null;
    private final static int MAX_Z_ORDER = 10;
    private final static int MIN_Z_ORDER = 0;
    private final static float MAX_SCALE = 1.2f;
    private final static float MIN_SCALE = 1.0f;

    public void setAutoSelect(boolean autoSelect) {
        isAutoSelect = autoSelect;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    public CustomLayoutManager() {
        horizonOrientationHelper = OrientationHelper.createHorizontalHelper(this);
    }

    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() <= 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        int consume = fill(recycler,state,0);
        Log.d(TAG,"consume :" + consume);
    }

    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        return fillHorizontal(recycler,state,dx);
    }

    /**
     * @param recycler
     * @param state
     * @param dx <0列表向右滑动布局head，>0列表向左滑动布局tail
     * @return
     */
    private int fillHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        if (horizonOrientationHelper == null || state == null) {
            return 0;
        }

        int consumeX = 0;
        int layout_startX = 0;
        View firstVisibleView = null;
        if (mFirstVisiPos < 0) {
            layout_startX = getPaddingLeft();
            mFirstVisiPos = 0;
        } else {
            firstVisibleView = getChildAt(0);
            if (firstVisibleView != null && firstVisibleView.getLayoutParams() instanceof RecyclerView.LayoutParams) {
                mFirstVisiPos = ((RecyclerView.LayoutParams) firstVisibleView.getLayoutParams()).getViewAdapterPosition();
            }
            if (firstVisibleView != null) {
                layout_startX = horizonOrientationHelper.getDecoratedStart(firstVisibleView);
            }
        }

        layout_startX -= dx;

        //暂存视图，稍后添加
        detachAndScrapAttachedViews(recycler);

        int X_start = getPaddingLeft();
        int x_end = getWidth() - getPaddingLeft() - getPaddingRight();

        int tempStartX = layout_startX;
        int currentPos = mFirstVisiPos - 1;
        //向头部布局
        while (currentPos >= 0 && tempStartX > X_start) {

            View item = recycler.getViewForPosition(currentPos);
            updateChildView(item,false);
            addView(item,0);

            measureChildWithMargins(item, 0, 0);

            //宽度消耗
            consumeX = horizonOrientationHelper.getDecoratedMeasurement(item);

            int l, t, r, b;
            r = tempStartX;
            l = r - consumeX;
            t = getPaddingTop();
            //高度消耗
            b = t + horizonOrientationHelper.getDecoratedMeasurementInOther(item);
            // We calculate everything with View's bounding box (which includes decor and margins)
            // To calculate correct layout position, we subtract margins.
            layoutDecoratedWithMargins(item, l, t, r, b);

            tempStartX -= consumeX;
            currentPos--;
        }

        tempStartX = layout_startX;
        currentPos = mFirstVisiPos;
        //向底部布局
        while (currentPos < state.getItemCount() && tempStartX < x_end) {

            View item = recycler.getViewForPosition(currentPos);
            updateChildView(item,false);
            addView(item);

            measureChildWithMargins(item, 0, 0);

            //宽度消耗
            consumeX = horizonOrientationHelper.getDecoratedMeasurement(item);

            int l, t, r, b;
            l = tempStartX;
            t = getPaddingTop();
            r = l + consumeX;
            //高度消耗
            b = t + horizonOrientationHelper.getDecoratedMeasurementInOther(item);
            // We calculate everything with View's bounding box (which includes decor and margins)
            // To calculate correct layout position, we subtract margins.
            layoutDecoratedWithMargins(item, l, t, r, b);

            tempStartX += consumeX;
            currentPos++;
        }

        //回收头部不可见视图
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            int end_x = horizonOrientationHelper.getDecoratedEnd(view);
            if (end_x <= getPaddingLeft()) {
                removeAndRecycleView(view,recycler);
            } else if (view.getLayoutParams() instanceof RecyclerView.LayoutParams) {
                mFirstVisiPos = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
                break;
            }
        }

        //回收尾部不可见试图
        for (int i = getChildCount() - 1; i >= 0 ; i--) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            int start_x = horizonOrientationHelper.getDecoratedStart(view);
            if (start_x >= getWidth() - getPaddingRight()) {
                removeAndRecycleView(view,recycler);
            } else if (view.getLayoutParams() instanceof RecyclerView.LayoutParams) {
                mLastVisiPos = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
                break;
            }
        }

        //回收视图
        recycleChildren(recycler);
        return dx;
    }

    /**
     * 手指从右向左滑动，dx > 0; 手指从左向右滑动，dx < 0;
     * 位移0、没有子View 当然不移动
     * @param dx x轴偏移量
     * @return 最多可滑动的距离
     */
    public int canScrollDx(int dx,RecyclerView.State state) {
        if (dx == 0 || state == null) {
            return Integer.MIN_VALUE;
        }
        View firstChild = null,lastChild = null;
        int childCount = getChildCount();

        firstChild = getChildAt(0);
        if (firstChild != null && firstChild.getLayoutParams() instanceof RecyclerView.LayoutParams) {
            mFirstVisiPos = ((RecyclerView.LayoutParams) firstChild.getLayoutParams()).getViewAdapterPosition();
        }

        lastChild = getChildAt(childCount - 1);
        if (lastChild != null && lastChild.getLayoutParams() instanceof RecyclerView.LayoutParams) {
            mLastVisiPos = ((RecyclerView.LayoutParams) lastChild.getLayoutParams()).getViewAdapterPosition();
        }

        //列表向右滑动
        if (dx < 0) {
            if (mFirstVisiPos >= 0 && mFirstVisiPos <= state.getItemCount()) {
                if (mFirstVisiPos == 0) {
                    int leftEdge = horizonOrientationHelper.getDecoratedStart(firstChild);
                    if (leftEdge - dx >= getPaddingLeft()) {
                        return leftEdge - getPaddingLeft();
                    }
                }
                return dx;
            }
        }
        //列表向左滑动
        if (dx > 0) {
            if (mLastVisiPos >= 0 && mLastVisiPos <= state.getItemCount()) {
                if (mLastVisiPos == state.getItemCount() - 1) {
                    int rightEdge = horizonOrientationHelper.getDecoratedEnd(lastChild);
                    if (rightEdge - dx <= getWidth() - getPaddingRight()) {
                        return rightEdge - (getWidth() - getPaddingRight());
                    }
                }
                return dx;
            }
        }

        return dx;
    }

    public void updateChildView(View view,boolean selected) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (selected) {
                view.setZ(MAX_Z_ORDER);
                view.setScaleX(MAX_SCALE);
                view.setScaleY(MAX_SCALE);
            } else {
                view.setZ(MIN_Z_ORDER);
                view.setScaleX(MIN_SCALE);
                view.setScaleY(MIN_SCALE);
            }
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }


    @Override
    public boolean canScrollHorizontally() {
        return true;
    }


    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {

        dx = canScrollDx(dx,state);
        if (dx == Integer.MIN_VALUE) {
            return 0;
        }

        // 手指从右向左滑动，dx > 0; 手指从左向右滑动，dx < 0;
        // 位移0、没有子View 当然不移动
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }

        float realDx = dx * 1.0000f;
        if (Math.abs(realDx) < 0.00000001f) {
            return 0;
        }

        dx = fill(recycler, state, dx);

        return dx;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        //处于静止状态，根据配置是否选中
        if (RecyclerView.SCROLL_STATE_IDLE == state) {
            if (isAutoSelect) {
                float min = Integer.MAX_VALUE;
                float centerX = getWidth() / 2.00f;
                float childCenterX = 9;
                int scrollX = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    childCenterX = (view.getWidth() / 2.00f + view.getLeft());
                    float diff = Math.abs(centerX - childCenterX);
                    if (diff < min) {
                        min = diff;
                        targetView = view;
                        scrollX = (int) (childCenterX - centerX);
                    }
                    if (diff == 0) {
                        if (targetView != null) {
                            updateChildView(targetView, true);
                            targetView.invalidate();
                        }
                        return;
                    }
                }
                if (targetView != null && targetView.getParent() instanceof RecyclerView) {
                    ((RecyclerView) targetView.getParent()).smoothScrollBy(scrollX,0);
                }
            }
        }
    }

    /**
     * 回收需回收的item
     */
    private void recycleChildren(RecyclerView.Recycler recycler) {
        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (int i = 0; i < scrapList.size(); i++) {
            RecyclerView.ViewHolder holder = scrapList.get(i);
            removeAndRecycleView(holder.itemView, recycler);
        }
    }
}