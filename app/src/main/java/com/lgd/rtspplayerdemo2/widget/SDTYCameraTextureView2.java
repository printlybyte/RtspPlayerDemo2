

package com.lgd.rtspplayerdemo2.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.wfty.encoder.IVideoEncoder;
import com.wfty.widget.CameraViewInterface;

/**
 * change the view size with keeping the specified aspect ratio.
 * if you set this view with in a FrameLayout and set property "android:layout_gravity="center",
 * you can show this view in the center of screen and keep the aspect ratio of content
 * XXX it is better that can set the aspect raton a a xml property
 */
public class SDTYCameraTextureView2 extends TextureView    // API >= 14
	implements CameraViewInterface, TextureView.SurfaceTextureListener {

//	private static final boolean DEBUG = true;	// TODO set false on release
//	private static final String TAG = "UVCCameraTextureView2";

	private double mRequestedAspect = -1.0;
	private boolean mHasSurface;
	private IVideoEncoder mEncoder;

	public SDTYCameraTextureView2(final Context context) {
		this(context, null, 0);
	}

	public SDTYCameraTextureView2(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SDTYCameraTextureView2(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setSurfaceTextureListener(this);
	}

	@Override
	public void onResume() {
	}

	@Override
	public void onPause() {
	}

	@Override
    public void setAspectRatio(final double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }
        if (mRequestedAspect != aspectRatio) {
            mRequestedAspect = aspectRatio;
            requestLayout();
        }
    }

	@Override
    public void setAspectRatio(final int width, final int height) {
		setAspectRatio(width / (double)height);
    }

	@Override
	public double getAspectRatio() {
		return mRequestedAspect;
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if (mRequestedAspect > 0) {
			int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
			int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

			final int horizPadding = getPaddingLeft() + getPaddingRight();
			final int vertPadding = getPaddingTop() + getPaddingBottom();
			initialWidth -= horizPadding;
			initialHeight -= vertPadding;

			final double viewAspectRatio = (double)initialWidth / initialHeight;
			final double aspectDiff = mRequestedAspect / viewAspectRatio - 1;

			if (Math.abs(aspectDiff) > 0.01) {
				if (aspectDiff > 0) {
					// width priority decision
					initialHeight = (int) (initialWidth / mRequestedAspect);
				} else {
					// height priority decison
					initialWidth = (int) (initialHeight * mRequestedAspect);
				}
				initialWidth += horizPadding;
				initialHeight += vertPadding;
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
			}
		}

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

	@Override
	public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
		mHasSurface = true;
	}

	@Override
	public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
	}

	@Override
	public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
		mHasSurface = false;
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
		if (mEncoder != null)
			mEncoder.frameAvailableSoon();
	}

	@Override
	public void setCallback(final Callback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Surface getSurface() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasSurface() {
		return mHasSurface;
	}

	@Override
	public void setVideoEncoder(final IVideoEncoder encoder) {
		mEncoder = encoder;
	}

	@Override
	public Bitmap captureStillImage() {
		return null;
	}

}
