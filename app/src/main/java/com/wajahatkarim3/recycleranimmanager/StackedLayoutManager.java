package com.wajahatkarim3.recycleranimmanager;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * Created by Wajahat on 8/3/2017.
 */

public class StackedLayoutManager extends RecyclerView.LayoutManager {

    public final String TAG = StackedLayoutManager.class.getSimpleName();

    /**
     * Maximum views visible on whole screen or RecyclerView space
     */
    int maxViewToVisible = 3;

    /**
     * The index of first visible item
     */
    int firstVisibleItem = 0;

    /**
     * The index of first partial item
     */
    int firstPartialItem = -1;

    /**
     * The item view's width
     */
    int itemViewWidth = 0;

    /**
     * The number of views to scale behind the first item
     */
    int numViewsToScale = 2;

    /**
     * Enable / disable logs
     */
    boolean logsEnabled = true;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
    {
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

        // Calculate the maximum views to be visible - This calculates max width as well
        calculateMaxViewsToVisible(recycler);

        // Fill the view
        fillTheView(recycler);
    }

    public void fillTheView(RecyclerView.Recycler recycler)
    {
        // Obtain the first visible item position
        findFirstVisiblePosition(recycler);

        //before we layout child views, we first scrap all current attached views
        detachAndScrapAttachedViews(recycler);

        // Layout scaled views here
        int scaledViewsCount = drawScaledViews(recycler);

        // Layout visible views here
        int decorViewsCount = drawDecorViews(recycler);

        if (logsEnabled)
            Log.w(TAG, "fillTheView: Scaled: " + scaledViewsCount + " --- Decor: " + decorViewsCount  );

        // Remove anything that is left behind
        final List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (int i=0; i<scrapList.size(); i++)
        {
            final View viewToRemove = scrapList.get(i).itemView;
            recycler.recycleView(viewToRemove);
        }
    }

    public int drawScaledViews(RecyclerView.Recycler recycler)
    {
        int scaledViewsCount = 0;
        int i = firstVisibleItem;
        int left = getPaddingLeft();
        for (i = firstVisibleItem-1; i>=0 && i>=firstVisibleItem-numViewsToScale; i--)
        {
            if (i< 0 || i >= getItemCount())
                break;

            View view = recycler.getViewForPosition(i);
            addView(view);

            measureChildWithMargins(view, 0, 0);

            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, 0, left+width, height);

            scaledViewsCount++;
        }
        return scaledViewsCount;
    }

    public int drawDecorViews(RecyclerView.Recycler recycler)
    {
        int decorViewsCount = 0;
        int left = getPaddingLeft();
        int i = firstVisibleItem;
        while (i >= firstVisibleItem && i < (firstVisibleItem + maxViewToVisible) )
        {
            if (i< 0 || i >= getItemCount())
                break;

            View view = recycler.getViewForPosition(i);
            addView(view);

            measureChildWithMargins(view, 0, 0);

            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, 0, left+width, height);
            left += width;

            i++;

            decorViewsCount++;
        }
        return decorViewsCount;
    }

    public void calculateMaxViewsToVisible(RecyclerView.Recycler recycler)
    {
        View view = recycler.getViewForPosition(0);
        addView(view);
        measureChildWithMargins(view, 0, 0);
        int itemWidth = getDecoratedMeasuredWidth(view);
        removeView(view);
        int width = getWidth();

        itemViewWidth = itemWidth;

        //if (logsEnabled)
        //     Log.w(TAG, "calculateMaxViewsToVisible: Max Width: " + width + " -- Item Width: " + itemWidth);

        maxViewToVisible = (width / itemWidth) + 1;         // Adding 1 extras - 1 for partial (if any) and other for extra view

        //if (logsEnabled)
        //    Log.w(TAG, "calculateMaxViewsToVisible: Max Views: " + maxViewToVisible );
    }

    private void findFirstVisiblePosition(RecyclerView.Recycler recycler) {

        for (int i=0; i<getItemCount(); i++)
        {
            View view = recycler.getViewForPosition(i);
            if (view.getLeft() < itemViewWidth)
            {
                firstVisibleItem = i;
                break;
            }
        }

        if (logsEnabled)
            Log.d(TAG, "findFirstVisiblePosition: " + firstVisibleItem);
    }

}
