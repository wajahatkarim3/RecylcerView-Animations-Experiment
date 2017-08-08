package com.wajahatkarim3.recycleranimmanager;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.regex.Matcher;

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

    int leftBorder = 0;

    /**
     * Enable / disable logs
     */
    boolean logsEnabled = true;
    private int SCALE_MARGIN = 50;
    private float SCALE_FACTOR = 0.8f;

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

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {

        if (getChildCount() == 0) {
            return 0;
        }

        if (dx > 0)                 // Contents are scrolling left
        {
            // Don't exceed more than last item. Stop scrolling as last item comes in screen
            if (!isLastItemVisible())
            {
                View lastView = getChildAt(getChildCount()-1);
                int diff = lastView.getRight() - getWidth();
                dx = Math.min(diff, dx);
                scrollToLeft(recycler, dx);
            }
        }
        else if (dx < 0)            // Contents are scrolling right
        {
            scrollToRight(recycler, dx);
        }

        return dx;
    }

    public void fillTheView(RecyclerView.Recycler recycler)
    {
        // Obtain the first visible item position
        findFirstVisiblePosition(recycler);

        //before we layout child views, we first scrap all current attached views
        detachAndScrapAttachedViews(recycler);

        // Layout scaled views here
        //int scaledViewsCount = drawScaledViews(recycler);

        // Layout visible views here
        int decorViewsCount = drawDecorViews(recycler);

        //if (logsEnabled)
            //Log.w(TAG, "fillTheView: Scaled: " + scaledViewsCount + " --- Decor: " + decorViewsCount  );

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
        //for (i = firstVisibleItem-1; i>=0 && i>=firstVisibleItem-numViewsToScale; i--)
        for (i = firstVisibleItem-1; i>=0; i--)
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
        int left = getPaddingLeft() + SCALE_MARGIN;
        leftBorder = getPaddingLeft() + SCALE_MARGIN;
        int i = firstVisibleItem;
        //while (i >= firstVisibleItem && i < (firstVisibleItem + maxViewToVisible) )
        while (i >= firstVisibleItem && i < getItemCount() )
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

        for (int i=0; i<getChildCount(); i++)
        {
            View view = getChildAt(i);
            if (view.getRight() >= getPaddingLeft() && view.getRight() <= itemViewWidth + SCALE_MARGIN && i > firstVisibleItem)
            {
                firstVisibleItem = i;
                break;
            }
        }

        //if (logsEnabled)
        //    Log.d(TAG, "findFirstVisiblePosition: " + firstVisibleItem);
    }

    private boolean isLastItemVisible()
    {
        if (getChildCount() < 0)
            return false;

        View view = getChildAt(getChildCount()-1);
        if (view.getRight() < getWidth())
            return true;

        return false;
    }

    private boolean isFirstItemVisible()
    {
        if (getChildCount() < 0)
            return false;

        View view = getChildAt(0);
        if (view.getLeft() >= getPaddingLeft())
            return true;

        return false;
    }

    public void scrollToLeft(RecyclerView.Recycler recycler, int dx)
    {
        // Find first visible item
        findFirstVisiblePosition(recycler);

        View firstView = getChildAt(0);

        // Not any partial view, move next element and onwards to left
        if (isAnyPartialView(recycler) == false)
        {
            for (int i=firstVisibleItem+1; i<getChildCount(); i++)
            {
                View view = getChildAt(i);
                //int diff = view.getLeft() - firstView.getLeft();
                int diff = view.getLeft() - leftBorder;
                //if (logsEnabled)
                //    Log.e(TAG, "scrollToLeft: " + "min: " + minLeft + "  ---  dx: " + dx + "  -- diff: " + diff );
                view.offsetLeftAndRight(-Math.min(diff, dx));
            }

            scaleDownFirstView(recycler, firstVisibleItem);
        }
        else {
            for (int i=firstVisibleItem; i<getChildCount(); i++)
            {
                View view = getChildAt(i);
                //int diff = view.getLeft() - firstView.getLeft();
                int diff = view.getLeft() - leftBorder;

                //if (logsEnabled)
                //    Log.e(TAG, "scrollToLeft: " + "min: " + minLeft + "  ---  dx: " + dx + "  -- diff: " + diff );
                view.offsetLeftAndRight(-Math.min(diff, dx));
            }

            scaleDownFirstView(recycler, firstVisibleItem);
        }
    }

    public void scrollToRight(RecyclerView.Recycler recycler, int dx)
    {
        // Find first visible item
        findFirstVisiblePosition(recycler);

        // If we have reached on exceed left, don't scroll anymore
        if (firstVisibleItem==0 && !isAnyPartialView(recycler))
            return;

        int startItem = isAnyPartialView(recycler)?firstVisibleItem+1:firstVisibleItem;

        View topView = getChildAt(startItem-1);
        View startView = getChildAt(startItem);

        int diff = topView.getRight() - startView.getLeft();

        //if (logsEnabled)
        //    Log.e(TAG, "scrollToRight: Diff: " + diff );

        if (diff <= 0)
        {
            firstVisibleItem--;

            if (firstVisibleItem==0)
                return;

            startItem = isAnyPartialView(recycler)?firstVisibleItem+1:firstVisibleItem;
            topView = getChildAt(startItem-1);
            startView = getChildAt(startItem);

            diff = topView.getRight() - startView.getLeft();

            boolean isPartial = true;
            for (int i=startItem; i<getChildCount(); i++)
            {

                View view = getChildAt(i);
                //View prevView = getChildAt(i-1);

                //if (prevView != null && isPartial)
                {
                    //if (logsEnabled)
                    //    Log.e(TAG, "scrollToRight: " + i + " - diff: " + diff + " -- dx: " + dx );

                    int delta = Math.min(dx, diff);

                    //int dis = view.getLeft() - prevView.getRight();
                    //delta = Math.min(dis, delta);
                    //if (logsEnabled)
                    //    Log.w(TAG, "scrollToRight: " + i + " - delta: " + -delta );


                    view.offsetLeftAndRight(-delta);



                    //isPartial = false;
                }

            }
        }
        else if (diff > 0) {

            for (int i=startItem; i<getChildCount(); i++)
            {
                View view = getChildAt(i);
                View prevView = getChildAt(i-1);

                if (prevView != null)
                {

                    //if (logsEnabled)
                    //    Log.w(TAG, "scrollToRight: " + i + " - diff: " + diff + " -- dx: " + dx );

                    int delta = Math.max(dx, -diff);

                    int dis = view.getLeft() - prevView.getLeft();
                    delta = Math.min(dis, delta);
                    //if (logsEnabled)
                    //    Log.e(TAG, "scrollToRight: " + i + " - delta: " + delta );

                    view.offsetLeftAndRight(-delta);
                }
            }
        }

        scaleUpFirstView(recycler, startItem-1);
    }

    public boolean isAnyPartialView(RecyclerView.Recycler recycler)
    {
        findFirstVisiblePosition(recycler);

        if (firstVisibleItem >= getChildCount()-1)
            return false;

        View topView = getChildAt(firstVisibleItem);
        View partialView = getChildAt(firstVisibleItem+1);

        if (partialView.getLeft() > topView.getLeft() && topView.getRight() > partialView.getLeft())
        //if (topView.getLeft() == partialView.getLeft() && topView.getRight() == partialView.getRight())
        {
            //if (logsEnabled)
            //    Log.w(TAG, "isAnyPartialView: " + " true");
            return true;
        }

        return false;
    }

    private void scaleDownFirstView(RecyclerView.Recycler recycler, int firstVisibleItem)
    {
        View toScaleView = getChildAt(firstVisibleItem);
        View partialView = getChildAt(firstVisibleItem+1);

        int maxDistance = itemViewWidth;
        int distance = partialView.getLeft() - leftBorder;

        float percent = distance * 100 / maxDistance;

        if (toScaleView.getScaleX() > SCALE_FACTOR)
        {
            // Scale down here
            float scaling = SCALE_FACTOR - (SCALE_FACTOR * percent / 100);
            toScaleView.setScaleX( (toScaleView.getScaleX() - scaling)<SCALE_FACTOR?SCALE_FACTOR:(toScaleView.getScaleX() - scaling) );
            toScaleView.setScaleY( (toScaleView.getScaleY() - scaling)<SCALE_FACTOR?SCALE_FACTOR:(toScaleView.getScaleY() - scaling) );

            if (toScaleView.getScaleX() > 1f)
                toScaleView.setScaleX(1f);

            if (toScaleView.getScaleY() > 1f)
                toScaleView.setScaleY(1f);
        }
        else {
            toScaleView.setScaleX(SCALE_FACTOR);
            toScaleView.setScaleY(SCALE_FACTOR);
        }

        int translationDistance = (int)(SCALE_MARGIN*(100-percent)/100);
        toScaleView.offsetLeftAndRight(-translationDistance);

        if (toScaleView.getLeft() < (leftBorder-SCALE_MARGIN) )
        {
            toScaleView.setLeft((leftBorder-SCALE_MARGIN));
            toScaleView.setRight(leftBorder + (int)(itemViewWidth*SCALE_FACTOR));
        }
    }

    private void scaleUpFirstView(RecyclerView.Recycler recycler, int firstVisibleItem)
    {
        View toScaleView = getChildAt(firstVisibleItem);
        View partialView = getChildAt(firstVisibleItem+1);

        int maxDistance = itemViewWidth;
        int distance = leftBorder + itemViewWidth - partialView.getLeft();

        float percent = Math.abs(distance * 100 / (itemViewWidth + leftBorder));
        float factor = percent/100;

        //if (logsEnabled)
        //    Log.e(TAG, "scaleUpFirstView: factor" + factor );

        if (toScaleView.getScaleX() < 1f)
        {
            // Scale up view here
            float scaling = factor;
            toScaleView.setScaleX( 1f - (1-SCALE_FACTOR)*scaling );
            toScaleView.setScaleY( 1f - (1-SCALE_FACTOR)*scaling );

            if (toScaleView.getScaleX() > 1f)
                toScaleView.setScaleX(1f);

            if (toScaleView.getScaleY() > 1f)
                toScaleView.setScaleY(1f);
        }
        else {
            toScaleView.setScaleX(1f);
            toScaleView.setScaleY(1f);
        }

    }

    /*
    public void downScaleViews(RecyclerView.Recycler recycler)
    {
        // Find First Visible Item
        findFirstVisiblePosition(recycler);

        View firstView = getChildAt(firstVisibleItem);
        View partialView = getChildAt(firstVisibleItem+1);

        int maxDistance = itemViewWidth;
        int distance = partialView.getLeft() - leftBorder;

        float percent = distance * 100 / maxDistance;

        if (firstView != null && firstView.getScaleX() > SCALE_FACTOR)
        {
            // Scale down
            float scaling = SCALE_FACTOR - (SCALE_FACTOR * percent / 100);
            firstView.setScaleX( (firstView.getScaleX() - scaling)<SCALE_FACTOR?SCALE_FACTOR:(firstView.getScaleX() - scaling) );
            firstView.setScaleY( (firstView.getScaleX() - scaling)<SCALE_FACTOR?SCALE_FACTOR:(firstView.getScaleX() - scaling) );
        }

        // Translation to left
        int translationDistance = (int)(SCALE_MARGIN*(100-percent)/100);
        firstView.offsetLeftAndRight(-translationDistance);

        if (logsEnabled)
        {
            Log.e(TAG, "downScaleViews: first: " + firstVisibleItem + " -- partial: " + (firstVisibleItem+1) + " --- max: " + maxDistance + " --- dist: " + distance);
            Log.w(TAG, "downScaleViews: Percetn: " + percent );
            //Log.w(TAG, "downScaleViews: Margin: " + (int)(SCALE_MARGIN*percent/100) );
            //Log.w(TAG, "downScaleViews: Scaling: " + scaling );
            Log.w(TAG, "downScaleViews: Translation: " + translationDistance );
        }
    }

    public void upScaleViews(RecyclerView.Recycler recycler)
    {
        // Find first visible item
        findFirstVisiblePosition(recycler);

        View firstView = getChildAt(firstVisibleItem);
        View partialView = getChildAt(firstVisibleItem+1);

        int maxDistance = itemViewWidth;
        int distance = itemViewWidth - partialView.getLeft();

        float percent = Math.abs(distance * 100 / (itemViewWidth + leftBorder));
        float scaling = 0f;

        if (firstView != null && firstView.getScaleX() < 1)
        {
            // Scale up
            scaling = ( (1-SCALE_FACTOR)*percent );
            firstView.setScaleX( firstView.getScaleX() + scaling>1f?1f:scaling );
            firstView.setScaleY( firstView.getScaleY() + scaling>1f?1f:scaling );
        }

        if (logsEnabled)
        {
            Log.e(TAG, "upScaleViews: first: " + firstVisibleItem + " -- partial: " + (firstVisibleItem+1) + " --- max: " + maxDistance + " --- dist: " + distance + " --- left border: " + leftBorder);
            Log.w(TAG, "upScaleViews: Percetn: " + percent );
            Log.w(TAG, "upScaleViews: Scaling: " + scaling );
        }

    }
    */

}
