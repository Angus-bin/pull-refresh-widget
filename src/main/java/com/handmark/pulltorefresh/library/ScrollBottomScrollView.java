package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ScrollBottomScrollView  extends ScrollView {

	private OnScrollChangedListener onScrollChangedListener;

	public ScrollBottomScrollView(Context context) {
		super(context);
	}

	public ScrollBottomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollBottomScrollView(Context context, AttributeSet attrs,int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt){
		if(t + getHeight() >=  computeVerticalScrollRange()){
			//ScrollView滑动到底部了
			if(onScrollChangedListener != null)
				onScrollChangedListener.scrollToBottom();
		}
	}

	public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener){
		this.onScrollChangedListener = onScrollChangedListener;
	}

	public interface OnScrollChangedListener{
		public void scrollToBottom();
	}

//	@Override
//	public void fling(int velocityY) {
//		super.fling(velocityY/2);
//	}
}
