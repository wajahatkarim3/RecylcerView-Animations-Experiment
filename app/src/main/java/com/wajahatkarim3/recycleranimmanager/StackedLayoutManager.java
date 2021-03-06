package com.wajahatkarim3.recycleranimmanager;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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

    int itemViewHeight = 0;

    /**
     * The number of views to scale behind the first item
     */
    int numViewsToScale = 2;

    int leftBorder = 0;

    /**
     * Enable / disable logs
     */
    boolean logsEnabled = true;
    private int SCALE_MARGIN = 40;
    private float SCALE_FACTOR = 0.7f;

    public StackedLayoutManager() {

    }

    public StackedLayoutManager(int SCALE_MARGIN, float SCALE_FACTOR) {
        this.SCALE_MARGIN = SCALE_MARGIN;
        this.SCALE_FACTOR = SCALE_FACTOR;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
    public void onMeasure(final RecyclerView.Recycler recycler, final RecyclerView.State state, final int widthSpec, final int heightSpec) {

        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);

        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        final boolean hasWidthSize = widthMode != View.MeasureSpec.UNSPECIFIED;
        final boolean hasHeightSize = heightMode != View.MeasureSpec.UNSPECIFIED;

        final boolean exactWidth = widthMode == View.MeasureSpec.EXACTLY;
        final boolean exactHeight = heightMode == View.MeasureSpec.EXACTLY;

        if (exactWidth && exactHeight) {
            // in case of exact calculations for both dimensions let's use default "onMeasure" implementation
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }


        /*


        //int heightMode = View.MeasureSpec.getMode(heightSpec);
        int he = View.MeasureSpec.getSize(heightSpec);
        int wi = View.MeasureSpec.getSize(widthSpec);

        he = View.MeasureSpec.makeMeasureSpec(heightSpec, View.MeasureSpec.UNSPECIFIED);
        wi = View.MeasureSpec.makeMeasureSpec(widthSpec, View.MeasureSpec.UNSPECIFIED);

        Log.e(TAG, "onMeasure: " + wi + ", " + he);
        //setMeasuredDimension(wi, he);
        super.onMeasure(recycler, state, wi, he);
        */
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @Override
    public void onAdapterChanged(final RecyclerView.Adapter oldAdapter, final RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);

        removeAllViews();
    }

    @Override
    public boolean canScrollHorizontally() {
        return 0 != getChildCount();
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

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    public void fillTheView(RecyclerView.Recycler recycler)
    {
        // Obtain the first visible item position
        findFirstVisiblePosition(recycler);

        //before we layout child views, we first scrap all current attached views
        detachAndScrapAttachedViews(recycler);

        // Layout scaled views here

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

            itemViewWidth = width;
            itemViewHeight = height;

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

            scaleDownFirstView(recycler, firstVisibleItem+1);
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

    private void scaleDownFirstView(RecyclerView.Recycler recycler, int firstVisibleItem) {

        View toAlphaView = getChildAt(firstVisibleItem-1);
        View toScaleView = getChildAt(firstVisibleItem);
        View partialView = getChildAt(firstVisibleItem + 1);

        if (toScaleView == null || partialView == null) return;

        //if (logsEnabled)
        //    Log.w(TAG, "scrollToLeft: Scaling Down: " + firstVisibleItem);

        float scale = SCALE_FACTOR;

        //view.setAlpha(0.5f);
        toScaleView.setPivotX(-(int) (toScaleView.getWidth() * 0.3));
        toScaleView.setPivotY((int) (toScaleView.getHeight() * 0.4));

        int maxDistance = itemViewWidth;
        int distance = partialView.getLeft() - leftBorder;
        float percent = distance * 100 / maxDistance;

        float valueToScale = 1 - scale;
        float totalScaleToDecrease = 0f;
        totalScaleToDecrease = (valueToScale * percent / 100);
        totalScaleToDecrease = scale + totalScaleToDecrease;

        if (toScaleView.getScaleX() > scale)
        {
            // Scale down here
            toScaleView.setScaleX(totalScaleToDecrease < scale ? scale : totalScaleToDecrease);
            toScaleView.setScaleY(totalScaleToDecrease < scale ? scale : totalScaleToDecrease);

            if (toScaleView.getScaleX() > 1f)
                toScaleView.setScaleX(1f);

            if (toScaleView.getScaleY() > 1f)
                toScaleView.setScaleY(1f);

            if (toScaleView.getScaleX() < scale)
                toScaleView.setScaleX(scale);

            if (toScaleView.getScaleY() < scale)
                toScaleView.setScaleY(scale);
        } else {
            toScaleView.setScaleX(scale);
            toScaleView.setScaleY(scale);
        }

        // Perform alpha scaling for second view here
        if (toAlphaView != null)
        {
            scale = SCALE_FACTOR / 1.3f;
            valueToScale = SCALE_FACTOR - scale;
            totalScaleToDecrease = (valueToScale * percent / 100);
            totalScaleToDecrease = scale + totalScaleToDecrease;

            if (toAlphaView.getScaleX() > scale)
            {
                // Scale down here
                toAlphaView.setScaleX(totalScaleToDecrease < scale ? scale : totalScaleToDecrease);
                toAlphaView.setScaleY(totalScaleToDecrease < scale ? scale : totalScaleToDecrease);
                toAlphaView.setAlpha(0.5f + (0.5f*percent / 100));

                //Log.w(TAG, "scaleDownFirstView: Alpha " + (0.5f*percent / 100) + "" );

                if (toAlphaView.getScaleX() > 1f)
                    toAlphaView.setScaleX(1f);

                if (toAlphaView.getScaleY() > 1f)
                    toAlphaView.setScaleY(1f);

                if (toAlphaView.getScaleX() < scale)
                    toAlphaView.setScaleX(scale);

                if (toAlphaView.getScaleY() < scale)
                    toAlphaView.setScaleY(scale);
            }
            else
            {
                toAlphaView.setScaleX(scale);
                toAlphaView.setScaleY(scale);
            }
        }

        //if (logsEnabled)
        //{
        //    Log.w(TAG, "scaleDownFirstView: distance: " + distance );
        //    Log.e(TAG, "scaleDownFirstView: x: " + toScaleView.getScaleX() + ", y: " + toScaleView.getScaleY() + " at percent: " + percent + " with scaling: " + totalScaleToDecrease);
        //}

        if (toScaleView.getLeft() < (leftBorder - SCALE_MARGIN)) {
            toScaleView.setLeft((leftBorder - SCALE_MARGIN));
            toScaleView.setRight(leftBorder + (int) (itemViewWidth * scale));
        }

    }

    private void scaleUpFirstView(RecyclerView.Recycler recycler, int firstVisibleItem)
    {
        View toAlphaView = getChildAt(firstVisibleItem-1);
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

        // Scale up the alpha view here and decrease alpha
        if (toAlphaView != null)
        {
            // Current scale will be SCALE_FACTOR / 1.3f = 0.5
            // Scaling from 0.5 to 0.7
            if (toAlphaView.getScaleX() < SCALE_FACTOR )
            {
                percent = 100f - percent;
                float valueToScale = SCALE_FACTOR - (SCALE_FACTOR/1.3f);            // 0.2
                float totalScaleToIncrease = 0f;
                totalScaleToIncrease = (valueToScale * percent / 100);
                totalScaleToIncrease = (SCALE_FACTOR/1.3f) + totalScaleToIncrease;

                toAlphaView.setScaleX( totalScaleToIncrease );
                toAlphaView.setScaleY( totalScaleToIncrease );
                toAlphaView.setAlpha(0.5f + 0.5f*percent);

                if (toAlphaView.getScaleX() > SCALE_FACTOR)
                    toAlphaView.setScaleX(SCALE_FACTOR);

                if (toAlphaView.getScaleY() > SCALE_FACTOR)
                    toAlphaView.setScaleY(SCALE_FACTOR);
            }
            else {
                toAlphaView.setScaleX(SCALE_FACTOR);
                toAlphaView.setScaleY(SCALE_FACTOR);
            }
        }

    }

}
