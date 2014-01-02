/**
 * Copyright 2013 Volodymyr Baydalka.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
  */

package com.zv.android.drawable;
 
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;

public class RadialClipDrawable extends Drawable implements Drawable.Callback
{
	private final static int MAX_LEVEL = 10000;
	
	private RadialClipState mState;
	private final Path mTmpPath = new Path();
	private final Rect mTmpRect = new Rect();
	private final RectF mTmpRectF = new RectF();

	private RadialClipDrawable(RadialClipState state, Resources res)
	{
		mState = new RadialClipState(state, this, res);
	}

	public RadialClipDrawable(Drawable drawable)
	{
		this(drawable, Gravity.FILL, 0);
	}

	public RadialClipDrawable(Drawable drawable, int gravity)
	{
		this(drawable, gravity, 0);
	}

	public RadialClipDrawable(Drawable drawable, int gravity, float startAngle)
	{
		this(null, null);

		mState.mDrawable = drawable;
		mState.mGravity = gravity;
		mState.mStartAngle = startAngle;

		if (drawable != null)
		{
			drawable.setCallback(this);
		}
	}

	public void invalidateDrawable(Drawable who)
	{
        invalidateSelf();
	}

	public void scheduleDrawable(Drawable who, Runnable what, long when)
	{
        scheduleSelf(what, when);
	}

	public void unscheduleDrawable(Drawable who, Runnable what)
	{
        unscheduleSelf(what);
	}

	@Override
	public int getChangingConfigurations()
	{
		return super.getChangingConfigurations() | mState.mChangingConfigurations
				| mState.mDrawable.getChangingConfigurations();
	}

	@Override
	public boolean getPadding(Rect padding)
	{
		return mState.mDrawable.getPadding(padding);
	}

	@Override
	public boolean setVisible(boolean visible, boolean restart)
	{
		mState.mDrawable.setVisible(visible, restart);
		return super.setVisible(visible, restart);
	}

	@Override
	public void setAlpha(int alpha)
	{
		mState.mDrawable.setAlpha(alpha);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
	public int getAlpha()
	{
		return mState.mDrawable.getAlpha();
	}

	@Override
	public void setColorFilter(ColorFilter cf)
	{
		mState.mDrawable.setColorFilter(cf);
	}

	@Override
	public int getOpacity()
	{
		return mState.mDrawable.getOpacity();
	}

	@Override
	public boolean isStateful()
	{
		return mState.mDrawable.isStateful();
	}

	@Override
	protected boolean onStateChange(int[] state)
	{
		return mState.mDrawable.setState(state);
	}

	@Override
	protected boolean onLevelChange(int level)
	{
		mState.mDrawable.setLevel(level);
		invalidateSelf();
		return true;
	}

	@Override
	protected void onBoundsChange(Rect bounds)
	{
		int w = mState.mDrawable.getIntrinsicWidth(); 
		int h = mState.mDrawable.getIntrinsicHeight(); 
		
		Gravity.apply(mState.mGravity, w, h, bounds, mTmpRect);
		mState.mDrawable.setBounds(mTmpRect);
	}

	@Override
	public void draw(Canvas canvas)
	{
		if (mState.mDrawable.getLevel() == 0)
		{
			return;
		}

		canvas.save();
		
		int level = getLevel();
		
		if(level != 0)
		{
			mTmpRectF.set(getBounds());
			
			mTmpPath.rewind();
			mTmpPath.moveTo(mTmpRectF.centerX(), mTmpRectF.centerY());
			mTmpPath.arcTo(mTmpRectF, mState.mStartAngle, 360 * level / MAX_LEVEL);
			mTmpPath.close();
			
			canvas.clipPath(mTmpPath);
			mState.mDrawable.draw(canvas);
		}
			
		canvas.restore();
	}

	@Override
	public int getIntrinsicWidth()
	{
		return mState.mDrawable.getIntrinsicWidth();
	}

	@Override
	public int getIntrinsicHeight()
	{
		return mState.mDrawable.getIntrinsicHeight();
	}

	@Override
	public ConstantState getConstantState()
	{
		if (mState.canConstantState())
		{
			mState.mChangingConfigurations = getChangingConfigurations();
			return mState;
		}
		return null;
	}

	final static class RadialClipState extends ConstantState
	{
		Drawable mDrawable;
		int mChangingConfigurations;
		int mGravity;
		float mStartAngle;

		private boolean mCheckedConstantState;
		private boolean mCanConstantState;

		RadialClipState(RadialClipState orig, RadialClipDrawable owner, Resources res)
		{
			if (orig != null)
			{
				if (res != null)
				{
					mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
				}
				else
				{
					mDrawable = orig.mDrawable.getConstantState().newDrawable();
				}

				mDrawable.setCallback(owner);
				mStartAngle = orig.mStartAngle;
				mGravity = orig.mGravity;
				mCheckedConstantState = mCanConstantState = true;
			}
		}

		@Override
		public Drawable newDrawable()
		{
			return new RadialClipDrawable(this, null);
		}

		@Override
		public Drawable newDrawable(Resources res)
		{
			return new RadialClipDrawable(this, res);
		}

		@Override
		public int getChangingConfigurations()
		{
			return mChangingConfigurations;
		}

		boolean canConstantState()
		{
			if (!mCheckedConstantState)
			{
				mCanConstantState = mDrawable.getConstantState() != null;
				mCheckedConstantState = true;
			}

			return mCanConstantState;
		}
	}
}