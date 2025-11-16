package com.gamex.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Custom LinearLayoutManager that expands to fit all items.
 * This is needed when RecyclerView is inside a ScrollView.
 */
public class FullyExpandedLayoutManager extends LinearLayoutManager {

    public FullyExpandedLayoutManager(Context context) {
        super(context);
    }

    public FullyExpandedLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        int itemCount = getItemCount();

        // If no items, use default measurement
        if (itemCount == 0) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        // Calculate the height needed to display all items
        int height = 0;
        int widthSize = View.MeasureSpec.getSize(widthSpec);

        for (int i = 0; i < itemCount; i++) {
            try {
                View view = recycler.getViewForPosition(i);

                if (view != null) {
                    measureChildView(view, widthSpec, heightSpec);
                    int measuredHeight = view.getMeasuredHeight();
                    RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                    height += measuredHeight + lp.topMargin + lp.bottomMargin;
                }
            } catch (Exception e) {
                android.util.Log.e("LayoutManager", "Error measuring child at position " + i, e);
            }
        }

        // Set the calculated height
        int finalHeightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        super.onMeasure(recycler, state, widthSpec, finalHeightSpec);
    }

    private void measureChildView(View child, int widthSpec, int heightSpec) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();

        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                getPaddingLeft() + getPaddingRight(), lp.width);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                getPaddingTop() + getPaddingBottom(), lp.height);

        child.measure(childWidthSpec, childHeightSpec);
    }
}
