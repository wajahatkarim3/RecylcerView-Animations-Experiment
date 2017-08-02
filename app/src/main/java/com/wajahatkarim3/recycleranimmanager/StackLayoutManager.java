package com.wajahatkarim3.recycleranimmanager;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by Wajahat on 8/2/2017.
 */

public class StackLayoutManager extends RecyclerView.LayoutManager {

    public final String TAG = StackLayoutManager.class.getSimpleName();
    private int stepSize = 100;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);

        //We have nothing to show for an empty data set but clear any existing views
        int itemCount = getItemCount();
        if (itemCount == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        int viewsCount = getChildCount();
        if(viewsCount > 0){
            return;
            // onLayout was called when we have views.
        }

        int viewTop;
        int viewLeft = 0, viewRight = 0;
        int lastVisibleItem = 0;
        viewTop = getPaddingTop();
        viewLeft = getPaddingLeft();

        boolean isLastLaidOutView;
        do {

            View view = recycler.getViewForPosition(lastVisibleItem);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            int measuredWidth = getDecoratedMeasuredWidth(view);
            int measuredHeight = getDecoratedMeasuredHeight(view);

            layoutDecorated(view, viewLeft, 0, viewLeft + measuredWidth, measuredHeight);

            // this assumes that all views are the same height
            viewLeft += measuredWidth;

            lastVisibleItem++;

        } while (lastVisibleItem < itemCount);

    }


    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {

        int childCount = getChildCount();
        if (childCount == 0) {
            // we cannot scroll when there is no views
            return 0;
        }

        //return super.scrollHorizontallyBy(dx, recycler, state);
        Log.e(TAG, "scrollHorizontallyBy: " + dx );
        offsetChildrenHorizontal(-dx);
        return dx;
    }

    @Override
    public void layoutDecorated(View child, int left, int top, int right, int bottom) {
        super.layoutDecorated(child, left, top, right, bottom);
        Log.w(TAG, "layoutDecorated: " + left + ", " + top + ", " + right + ", " + bottom );
    }
}
