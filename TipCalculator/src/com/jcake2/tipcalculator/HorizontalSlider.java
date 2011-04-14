package com.jcake2.tipcalculator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HorizontalSlider extends ProgressBar {

	private OnProgressChangeListener listener;

	private static int padding = 2;

	private int minProgress = 0;

	public void setMinProgress(int minProgress) {
		this.minProgress = minProgress;
		if (this.getProgress() < this.minProgress) {
			updateProgress(minProgress);
		}
	}

	private HorizontalSlider linkedSlider = null;

	public void setLinkedSlider(HorizontalSlider linkedSlider) {
		this.linkedSlider = linkedSlider;
	}
	
	private TextView percentTracker = null;
	
	public void setPercentTracker(TextView view){
		this.percentTracker = view;
	}

	public interface OnProgressChangeListener {
		void onProgressChanged(View v, int progress);
	}

	public HorizontalSlider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HorizontalSlider(Context context, AttributeSet attrs) {
		super(context, attrs, android.R.attr.progressBarStyleHorizontal);
	}

	public HorizontalSlider(Context context) {
		super(context);

	}

	public void setOnProgressChangeListener(OnProgressChangeListener l) {
		listener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();

		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_MOVE) {
			float x_mouse = event.getX() - padding;
			float width = getWidth() - 2 * padding;
			int progress = Math.round((float) getMax() * (x_mouse / width));

			if (linkedSlider != null) {
				linkedSlider.setMinProgress(progress);
			}

			updateProgress(progress);

		}

		return true;
	}

	public void updateProgress(int progress) {
		if (progress < minProgress){
			progress = minProgress;
		}

		this.setProgress(progress);
		
		if(percentTracker != null){
			double percent = PercentHelper.getPercent(progress, this.getMax());
			percentTracker.setText(percent + " %");
		}
		
		if (listener != null)
			listener.onProgressChanged(this, progress);
	}
}