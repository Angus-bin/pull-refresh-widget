package com.handmark.pulltorefresh.library;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.handmark.pulltorefresh.library.PinnedHeaderListView.PinnedSectionedHeaderAdapter;

/**
 * 
 * @author wanlh
 */
public abstract class SectionedBaseAdapter extends BaseAdapter implements PinnedSectionedHeaderAdapter {

    private static int HEADER_VIEW_TYPE = 0;
    private static int ITEM_VIEW_TYPE = 0;

    /**
     * Holds the calculated values of @{link getPositionInSectionForPosition}
     */
    private SparseArray<Integer> mSectionPositionCache;
    /**
     * Holds the calculated values of @{link getSectionForPosition}
     */
    private SparseArray<Integer> mSectionCache;
    /**
     * Holds the calculated values of @{link getCountForSection}
     */
    private SparseArray<Integer> mSectionCountCache;

    /**
     * Caches the item count
     */
    private int mCount;
    /**
     * Caches the section count
     */
    private int mSectionCount;
	
    public Bundle[] mBundleArr = null;
	
	//add by wanlaihuan 2014-09-21
    private View mCurrentFloatView;
	
    private OnItemListClickListener mOnItemListClickListener;
    public void setOnItemListClickListener(OnItemListClickListener onItemListClickListener){
    	mOnItemListClickListener = onItemListClickListener;
    }   
    public interface OnItemListClickListener{
    	public void onClick(int section, int position, View view);
    }
    
	public SectionedBaseAdapter() {
        super();
        mSectionCache = new SparseArray<Integer>();
        mSectionPositionCache = new SparseArray<Integer>();
        mSectionCountCache = new SparseArray<Integer>();
        mCount = -1;
        mSectionCount = -1;
    }

    @Override
    public void notifyDataSetChanged() {
        mSectionCache.clear();
        mSectionPositionCache.clear();
        mSectionCountCache.clear();
        mCount = -1;
        mSectionCount = -1;
        if(mCurrentFloatView != null)
        	currentHeaderFloatView(mCurrentFloatView);//add by wanlaihuan 2014-09-21
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        mSectionCache.clear();
        mSectionPositionCache.clear();
        mSectionCountCache.clear();
        mCount = -1;
        mSectionCount = -1;
        if(mCurrentFloatView != null)
        	currentHeaderFloatView(mCurrentFloatView);//add by wanlaihuan 2014-09-21
        super.notifyDataSetInvalidated();
    }

    @Override
    public final int getCount() {
        if (mCount >= 0) {
            return mCount;
        }
        int count = 0;
        int internalGetSectionCount = internalGetSectionCount();
        
      //初始化Bundle
//    	if(mBundleArr == null){
	    	mBundleArr = new Bundle[internalGetSectionCount];
	    	for(int i = 0; i < internalGetSectionCount; i++){
	    		if(mBundleArr[i] == null)
	    			mBundleArr[i] = new Bundle();
	    	}
//    	}
    	
        for (int i = 0; i < internalGetSectionCount; i++) {
            count += internalGetCountForSection(i);
            count++; // for the header view
        }
        mCount = count;
        return count;
    }

    @Override
    public final Object getItem(int position) {
        return getItem(getSectionForPosition(position), getPositionInSectionForPosition(position));
    }

    @Override
    public final long getItemId(int position) {
        return getItemId(getSectionForPosition(position), getPositionInSectionForPosition(position));
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (isSectionHeader(position)) {
            return getSectionHeaderView(getSectionForPosition(position), 
            		convertView, parent, getBundle(getSectionForPosition(position)));
        }
        
        int section = getSectionForPosition(position);
        int inSectionForposition = getPositionInSectionForPosition(position);
        View itemView = getItemView(section, inSectionForposition, convertView, parent);
        
        if(TextUtils.isEmpty(version))//兼容低版本
        	itemView.setOnClickListener(new MyOnItemClickListener(section, inSectionForposition));
        
        return itemView;
    }
    
    public class MyOnItemClickListener implements OnClickListener{
		private int section;
		private int position;
		
		public MyOnItemClickListener(int section, int position){
			this.section = section;
			this.position = position;
		}
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			//Log.d("tag", "MyOnClickListener section = "+section+",position = "+position);
			if(mOnItemListClickListener != null)
				mOnItemListClickListener.onClick(section, position, view);
		}
	}
    
    @Override
    public final int getItemViewType(int position) {
        if (isSectionHeader(position)) {
            return getItemViewTypeCount() + getSectionHeaderViewType(getSectionForPosition(position));
        }
        return getItemViewType(getSectionForPosition(position), getPositionInSectionForPosition(position));
    }

    @Override
    public final int getViewTypeCount() {
        return getItemViewTypeCount() + getSectionHeaderViewTypeCount();
    }

    public final int getSectionForPosition(int position) {
        // first try to retrieve values from cache
        Integer cachedSection = mSectionCache.get(position);
        if (cachedSection != null) {
            return cachedSection;
        }
        int sectionStart = 0;
        int internalGetSectionCount = internalGetSectionCount();
        for (int i = 0; i < internalGetSectionCount; i++) {
            int sectionCount = internalGetCountForSection(i);
            int sectionEnd = sectionStart + sectionCount + 1;
            if (position >= sectionStart && position < sectionEnd) {
                mSectionCache.put(position, i);
                return i;
            }
            sectionStart = sectionEnd;
        }
        return 0;
    }

    public int getPositionInSectionForPosition(int position) {
        // first try to retrieve values from cache
        Integer cachedPosition = mSectionPositionCache.get(position);
        if (cachedPosition != null) {
            return cachedPosition;
        }
        int sectionStart = 0;
        int internalGetSectionCount = internalGetSectionCount();
        for (int i = 0; i < internalGetSectionCount; i++) {
            int sectionCount = internalGetCountForSection(i);
            int sectionEnd = sectionStart + sectionCount + 1;
            if (position >= sectionStart && position < sectionEnd) {
                int positionInSection = position - sectionStart - 1;
                mSectionPositionCache.put(position, positionInSection);
                return positionInSection;
            }
            sectionStart = sectionEnd;
        }
        return 0;
    }

    public final boolean isSectionHeader(int position) {
        int sectionStart = 0;
        int internalGetSectionCount = internalGetSectionCount();
        for (int i = 0; i < internalGetSectionCount; i++) {
            if (position == sectionStart) {
                return true;
            } else if (position < sectionStart) {
                return false;
            }
            sectionStart += internalGetCountForSection(i) + 1;
        }
        return false;
    }

    public int getItemViewType(int section, int position) {
        return ITEM_VIEW_TYPE;
    }

    public int getItemViewTypeCount() {
        return 1;
    }

    public int getSectionHeaderViewType(int section) {
        return HEADER_VIEW_TYPE;
    }

    public int getSectionHeaderViewTypeCount() {
        return 1;
    }
    
    @Override
	public Bundle getBundle(int section) {
		// TODO Auto-generated method stub
		return mBundleArr[section];
	}
    
    public abstract Object getItem(int section, int position);

    public abstract long getItemId(int section, int position);

    public abstract int getSectionCount();

    public abstract int getCountForSection(int section);

    public abstract View getItemView(int section, int position, View convertView, ViewGroup parent);

    public abstract View getSectionHeaderView(int section, View convertView, ViewGroup parent, Bundle bundle);
    
	/**
     * 是否隐藏浮动的Title
     */
    public abstract boolean isHideFloatView();

  //+add by wanlaihuan 2014-09-21
    public abstract void currentHeaderFloatView(View floatView);

    protected int currentClickSection;
    public void setCurrentHeaderFloatView(View floatView){
    	mCurrentFloatView = floatView;
        // [Bug]设置悬浮HeaderView, 同时修改当前该HeaderView角标为currentClickSection值;
        currentClickSection = (Integer) mCurrentFloatView.getTag();
    }
  //-add by wanlaihuan 2014-09-21
    
    private String version;
    /**
     * 适配器的版本
     */
    @Override
    public void setVersion(String version){
    	this.version = version;
    }
    
    private int internalGetCountForSection(int section) {
        Integer cachedSectionCount = mSectionCountCache.get(section);
        if (cachedSectionCount != null) {
            return cachedSectionCount;
        }
        int sectionCount = getCountForSection(section);
        mSectionCountCache.put(section, sectionCount);
        return sectionCount;
    }

    private int internalGetSectionCount() {
        if (mSectionCount >= 0) {
            return mSectionCount;
        }
        mSectionCount = getSectionCount();
        return mSectionCount;
    }

}
