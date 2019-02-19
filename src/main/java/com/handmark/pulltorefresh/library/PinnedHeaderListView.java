package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;

import java.util.logging.Logger;

public class PinnedHeaderListView extends ListView implements OnScrollListener {

    private OnScrollListener mOnScrollListener;

    public static interface PinnedSectionedHeaderAdapter {
        public boolean isSectionHeader(int position);

        public int getSectionForPosition(int position);

        public View getSectionHeaderView(int section, View convertView, ViewGroup parent, Bundle bundle);

        public boolean isHideFloatView();

        public int getSectionHeaderViewType(int section);

        public int getCount();

        public int getSectionCount();

        /**用于Title与浮动Title之间传递数据*/
        public Bundle getBundle(int section);

        //add by wanlaihuan 2014-09-21 回调出当前的浮动View
        public void setCurrentHeaderFloatView(View floatView);

        public void setVersion(String version);
    }

    private PinnedSectionedHeaderAdapter mAdapter;
    private boolean mShouldPin = true;
    private int mWidthMode;
    private int mHeightMode;

    /**
     * 用于保存section
     */
    private View[] sectionFloatView;

    public PinnedHeaderListView(Context context) {
        super(context);
        super.setOnScrollListener(this);
        mContext = context;
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnScrollListener(this);
        mContext = context;
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(this);
        mContext = context;
    }

    public void setPinHeaders(boolean shouldPin) {
        mShouldPin = shouldPin;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        //mCurrentHeader = null;
        mAdapter = (PinnedSectionedHeaderAdapter) adapter;
        super.setAdapter(adapter);
    }

    private Context mContext;
    private FrameLayout mFloatParent;
    public void setFloatView(FrameLayout floatView){
        mFloatParent = floatView;
    }

    public FrameLayout getsetFloatView(){
        return mFloatParent;
    }
    public void clearFloatHeadViewCahce(){
        sectionFloatView =null;
    }

    private static String format(String level, String tag, String msg)
    {
        return String.format("[%s][%s]%s", level, tag, msg);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        //add by wanlaihuan 2014-09-21 解决回到第0行静止不动时还会显示浮动Title的问题
        if(mFloatParent!= null && firstVisibleItem == 0)
            mFloatParent.setVisibility(View.INVISIBLE);

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        System.out.print(format("DEBUG", "onScroll", "firstVisibleItem = " + firstVisibleItem));
        System.out.print(format("DEBUG", "onScroll", "getHeaderViewsCount = " + getHeaderViewsCount()));
        if (mAdapter == null || mAdapter.getCount() == 0 || !mShouldPin || (firstVisibleItem < getHeaderViewsCount())) {
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                View header = getChildAt(i);
                if (header != null) {
                    header.setVisibility(VISIBLE);
                }
            }
            return;
        }

        firstVisibleItem -= getHeaderViewsCount();

        int section = mAdapter.getSectionForPosition(firstVisibleItem);
        // int viewType = mAdapter.getSectionHeaderViewType(section);

        //处理浮动title
        if(sectionFloatView == null && mAdapter.getSectionCount() > 0)
            sectionFloatView = new View[mAdapter.getSectionCount()];
        else{
            if(mAdapter.getSectionCount() != sectionFloatView.length)
                sectionFloatView = new View[mAdapter.getSectionCount()];
        }

        if(mFloatParent != null ){
            if(sectionFloatView != null && sectionFloatView[section] == null) {
                sectionFloatView[section] = mAdapter.isHideFloatView() ? null : mAdapter.getSectionHeaderView(section,
                        sectionFloatView[section], mFloatParent, mAdapter.getBundle(section));
                if (sectionFloatView[section] != null)
                    sectionFloatView[section].setTag(section);
            }

            try{
                if(sectionFloatView != null && sectionFloatView[section] != null){
                    ensurePinnedHeaderLayout(sectionFloatView[section]);
                    mFloatParent.removeAllViews();
                    mFloatParent.addView(sectionFloatView[section]);
                    mAdapter.setCurrentHeaderFloatView(sectionFloatView[section]);
                    mFloatParent.setVisibility(View.VISIBLE);
                }else
                    mFloatParent.setVisibility(View.INVISIBLE);
            }catch(Exception e){
                mFloatParent.setVisibility(View.INVISIBLE);

            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //Log.d("tag", "onScroll scrollState = "+scrollState);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    private void ensurePinnedHeaderLayout(View header) {
        if (header.isLayoutRequested()) {
            int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), mWidthMode);

            int heightSpec;
            ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
            if (layoutParams != null && layoutParams.height > 0) {
                heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }
            header.measure(widthSpec, heightSpec);
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
        }
    }

    /*@Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mAdapter == null || !mShouldPin || mCurrentHeader == null)
            return;
        int saveCount = canvas.save();
        canvas.translate(0, mHeaderOffset);
        canvas.clipRect(0, 0, getWidth(), mCurrentHeader.getMeasuredHeight()); // needed
        // for
        // <
        // HONEYCOMB
        mCurrentHeader.draw(canvas);
        canvas.restoreToCount(saveCount);
    }*/

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        mHeightMode = MeasureSpec.getMode(heightMeasureSpec);
    }

    public void setOnItemClickListener(PinnedHeaderListView.OnItemClickListener listener) {
        super.setOnItemClickListener(listener);
        if(listener != null) {
            mAdapter.setVersion("2.0");
            listener.setHeaderViewCount(getHeaderViewsCount());
        }
    }

    public static abstract class OnItemClickListener implements AdapterView.OnItemClickListener {

        private int headerViewCount = 0;
        public void setHeaderViewCount(int headerViewCount){
            this.headerViewCount = headerViewCount;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int rawPosition, long id) {
            SectionedBaseAdapter adapter;
            if (adapterView.getAdapter().getClass().equals(HeaderViewListAdapter.class)) {
                HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) adapterView.getAdapter();
                adapter = (SectionedBaseAdapter) wrapperAdapter.getWrappedAdapter();
            } else {
                adapter = (SectionedBaseAdapter) adapterView.getAdapter();
            }

            int toalPosition = rawPosition - 2;
            int sectionCount = adapter.getSectionCount();
            int position = 0;
            int section = 0;

            if(sectionCount == 1)
                position = toalPosition;
            else{
                int sectionItemCount = 0;
                int positionCount = 0;
                for(int i = 0; i < sectionCount; i++){
                    sectionItemCount = adapter.getCountForSection(i);
                    positionCount += sectionItemCount;

                    if(positionCount + i >= toalPosition) {
                        section = i;
                        position = toalPosition - (positionCount + section - sectionItemCount + 1)
                                + 1 - (headerViewCount - 1);
                        break;
                    }
                }
            }

            //System.out.println("点击选项坐标 ( "+section+", "+position+" )");
            if (position == -1) {
                onSectionClick(adapterView, view, section, id);
            } else {
                onItemClick(adapterView, view, section, position, id);
            }
        }

        public abstract void onItemClick(AdapterView<?> adapterView, View view, int section, int position, long id);

        public abstract void onSectionClick(AdapterView<?> adapterView, View view, int section, long id);

    }
}
