package com.devf5r.kalam.Utils;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PickerLayoutManager extends LinearLayoutManager {
    private boolean changeAlpha = true;
    private OnScrollStopListener onScrollStopListener;
    private RecyclerView recyclerView;
    private float scaleDownBy = 0.4f;
    private float scaleDownDistance = 0.5f;

    public interface OnScrollStopListener {
        void selectedView(int i);
    }

    public PickerLayoutManager(Context context, int i, boolean z) {
        super(context, i, z);
    }

    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scaleDownView();
    }

    public int scrollHorizontallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getOrientation() != RecyclerView.HORIZONTAL) {
            return 0;
        }
        int scrollHorizontallyBy = super.scrollHorizontallyBy(i, recycler, state);
        scaleDownView();
        return scrollHorizontallyBy;
    }

    private void scaleDownView() {
        float width = ((float) getWidth()) / 2.0f;
        float f = this.scaleDownDistance * width;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt != null) {
                float min = (((this.scaleDownBy * -1.0f) * Math.min(f, Math.abs(width - (((float) (getDecoratedLeft(childAt) + getDecoratedRight(childAt))) / 2.0f)))) / f) + 1.0f;
                childAt.setScaleX(min);
                childAt.setScaleY(min);
                if (this.changeAlpha) {
                    childAt.setAlpha(min);
                }
            }
        }
    }

    public void onAttachedToWindow(RecyclerView recyclerView2) {
        super.onAttachedToWindow(recyclerView2);
        this.recyclerView = recyclerView2;
    }

    private int getRecyclerViewCenterX() {
        return ((this.recyclerView.getRight() - this.recyclerView.getLeft()) / 2) + this.recyclerView.getLeft();
    }

    public void onScrollStateChanged(int i) {
        super.onScrollStateChanged(i);
        if (i == 0) {
            int recyclerViewCenterX = getRecyclerViewCenterX();
            int width = this.recyclerView.getWidth();
            int i2 = -1;
            for (int i3 = 0; i3 <= this.recyclerView.getChildCount(); i3++) {
                View childAt = this.recyclerView.getChildAt(i3);
                if (childAt != null) {
                    int abs = Math.abs((getDecoratedLeft(childAt) + ((getDecoratedRight(childAt) - getDecoratedLeft(childAt)) / 2)) - recyclerViewCenterX);
                    int abs2 = abs;
                    if (abs < width) {
                        i2 = this.recyclerView.getChildLayoutPosition(childAt);
                        width = abs2;
                    }
                }
            }
            this.onScrollStopListener.selectedView(i2);
        }
    }

    public void setScaleDownBy(float f) {
        this.scaleDownBy = f;
    }

    public void setScaleDownDistance(float f) {
        this.scaleDownDistance = f;
    }

    public void setChangeAlpha(boolean z) {
        this.changeAlpha = z;
    }

    public void setOnScrollStopListener(OnScrollStopListener onscrollstoplistener) {
        this.onScrollStopListener = onscrollstoplistener;
    }
}
