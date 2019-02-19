package com.handmark.pulltorefresh.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ScrollWebView  extends WebView {
	public OnScrollChangeListener listener;

	public ScrollWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ScrollWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ScrollWebView(Context context) {
		super(context);
		init();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void init(){
		this.getSettings().setJavaScriptEnabled(true);
	}
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {

		super.onScrollChanged(l, t, oldl, oldt);
			if(listener != null)
				listener.onScrollChanged(this.getScrollY());
	}

	public void setOnScrollChangeListener(OnScrollChangeListener listener) {
		this.listener = listener;
	}

	public interface OnScrollChangeListener {
		public void onScrollChanged(int top);

	}
}
