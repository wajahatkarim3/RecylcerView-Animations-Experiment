package com.wajahatkarim3.recycleranimmanager;

import android.support.v4.app.Fragment;
import android.support.v4.util.DebugUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by Wajahat on 8/2/2017.
 */

public class StackLayoutManager extends RecyclerView.LayoutManager {

    public final String TAG = StackLayoutManager.class.getSimpleName();
    int firstElement = 5;
    int minDistanceToElement = 0;
    int scrollDistance = 0;

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

        positionViewsHorizontallyFirst(recycler, 0);
    }

    private void positionViewsHorizontallyFirst(RecyclerView.Recycler recycler, int offset) {

        int itemCount = getItemCount();

        int viewTop;
        int viewLeft = 0, viewRight = 0;
        int lastVisibleItem = 0;
        viewTop = getPaddingTop();
        viewLeft = getPaddingLeft();

        // Show all elements at first before firstElement
        for (int i=0; i<=firstElement; i++)
        {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            int measuredWidth = getDecoratedMeasuredWidth(view);
            int measuredHeight = getDecoratedMeasuredHeight(view);

            minDistanceToElement = viewLeft + measuredWidth;
            Log.e(TAG, "Min Distance: " + minDistanceToElement );

            layoutDecorated(view, viewLeft, 0, viewLeft + measuredWidth, measuredHeight);

            //viewLeft += measuredWidth;
        }


        for (int i=firstElement+1; i<getItemCount(); i++)
        {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            int measuredWidth = getDecoratedMeasuredWidth(view);
            int measuredHeight = getDecoratedMeasuredHeight(view);

            viewLeft += measuredWidth;

            layoutDecorated(view, viewLeft + offset, 0, viewLeft + measuredWidth + offset, measuredHeight);
            //view.offsetLeftAndRight(offset);

            // this assumes that all views are the same height
        }
    }


    private void alignViewsHorizontally(RecyclerView.Recycler recycler, int offset) {

        if (offset > 0)
        {
            int itemCount = getItemCount();

            int viewTop;
            int viewLeft = 0, viewRight = 0;
            int lastVisibleItem = 0;
            viewTop = getPaddingTop();
            viewLeft = getPaddingLeft();

            // Show all elements at first before firstElement
            for (int i=0; i<firstElement; i++)
            {
                //View view = recycler.getViewForPosition(i);
                //addView(view);

                //measureChildWithMargins(view, 0, 0);

                //int measuredWidth = getDecoratedMeasuredWidth(view);
                //int measuredHeight = getDecoratedMeasuredHeight(view);

                //minDistanceToElement = viewLeft + measuredWidth;
                //Log.e(TAG, "Min Distance: " + minDistanceToElement );

                //layoutDecorated(view, viewLeft, 0, viewLeft + measuredWidth, measuredHeight);

                //viewLeft += measuredWidth;

                // SKIP DO NOTHING
            }


            for (int i=firstElement+1; i<getItemCount(); i++)
            {
            /*
            View view = recycler.getViewForPosition(i);
            //addView(view);
            measureChildWithMargins(view, 0, 0);

            int measuredWidth = getDecoratedMeasuredWidth(view);
            int measuredHeight = getDecoratedMeasuredHeight(view);

            viewLeft += measuredWidth;

            //layoutDecorated(view, viewLeft + offset, 0, viewLeft + measuredWidth + offset, measuredHeight);
            //view.offsetLeftAndRight(offset);
            view.offsetLeftAndRight(offset);
            // this assumes that all views are the same height
            */


                View view = getChildAt(i);
                //Log.w(TAG, "Left Before: " + view.getLeft());
                view.offsetLeftAndRight(-offset);
                //Log.w(TAG, "Left After: " + view.getLeft());

                if (getChildAt(i).getLeft() <= getChildAt(0).getLeft())
                {
                    getChildAt(i).setLeft(getChildAt(0).getLeft());
                    firstElement++;
                }
            }
        }
        else {

            // Expanding Views

            /*
            Algorithm:

            Step 1: Check for the outer bound.
            Step 2: Expand partial views first
            Step 3: Expand top element view
            Step 4: Update top element view

            */

            // Step 1: Check for the outer bound.
            if (firstElement == 0)
            {
                Log.w(TAG, "alignViewsHorizontally: OOPS! Can't expand more" );
                return;
            }

            // Step 2: Expand partial views first
            if (isPartialView() == true)
            {
                int diff = getChildAt(firstElement).getRight() - getChildAt(firstElement+1).getLeft();
                if (diff <= 0)
                {
                    firstElement--;
                }
                else {
                    Log.w(TAG, "alignViewsHorizontally: Diff: " + diff );

                    // Move partial view and all onward views
                    for (int i=firstElement+1; i<getItemCount(); i++)
                    {
                        View view = getChildAt(i);
                        view.offsetLeftAndRight(-offset);
                    }
                }
            }
            // Step 3: Expand top element view
            else {

                Log.d(TAG, "alignViewsHorizontally: " + firstElement);

                // Move view and all onward views
                for (int i=firstElement; i<getItemCount(); i++)
                {
                    View view = getChildAt(i);
                    view.offsetLeftAndRight(-offset);

                    View first = getChildAt(firstElement);
                    View olderFirst = getChildAt(firstElement-1);
                    if (first == null || olderFirst == null)
                        continue;

                    if (getChildAt(firstElement).getRight() > getChildAt(firstElement-1).getRight())
                    {
                        getChildAt(firstElement).setRight(getChildAt(firstElement-1).getRight());
                        firstElement--;
                    }
                }

            }




            /*
            //firstElement--;
            int itemCount = getItemCount();

            int viewTop;
            int viewLeft = 0, viewRight = 0;
            int lastVisibleItem = 0;
            viewTop = getPaddingTop();
            viewLeft = getPaddingLeft();

            for (int i=firstElement; i<getItemCount(); i++)
            {
                View view = getChildAt(i);
                view.offsetLeftAndRight(-offset);
            }
            */

        }
    }

    private boolean isPartialView() {

        if (firstElement > getItemCount()-2 )
            return false;

        View firstView = getChildAt(firstElement);
        View partialView = getChildAt(firstElement + 1);

        if (partialView.getLeft() > firstView.getLeft())
            return true;

        return false;
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

        /*
        final View topView = getChildAt(0);
        final View bottomView = getChildAt(childCount - 1);


        if (dx > 0)
        {
            int recyclerViewRightEdge = getWidth() - getPaddingRight();
            if (bottomView.getRight() <= recyclerViewRightEdge)
                return 0;
        }
        else {
            int recyclerViewRightEdge = getWidth() - getPaddingRight();
            if (topView.getLeft() >= 0 && topView.getRight() <= recyclerViewRightEdge)
                return 0;
        }
        */

        //return super.scrollHorizontallyBy(dx, recycler, state);
        //Log.e(TAG, "scrollHorizontallyBy: " + dx );
        Log.e(TAG, "scrollHorizontallyBy: " + dx + " -- first: " + firstElement );

        // Left Bound
        //if (dx < 0 && firstElement == 0) return 0;
        // Right Bound
        //else if (dx > 0 && firstElement == getItemCount()-1) return 0;

        // Scroll
        alignViewsHorizontally(recycler, dx);
        return dx;
    }

    @Override
    public void layoutDecorated(View child, int left, int top, int right, int bottom) {
        super.layoutDecorated(child, left, top, right, bottom);
        //Log.w(TAG, "layoutDecorated: " + left + ", " + top + ", " + right + ", " + bottom );
    }
}
