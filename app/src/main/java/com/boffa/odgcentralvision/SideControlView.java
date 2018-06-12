package com.boffa.odgcentralvision;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * TODO: document your custom view class.
 */
public class SideControlView extends ConstraintLayout {
    private String mText;
    private Drawable mIcon;

    private TextView mTextView;
    private ImageView mImageView;

    private boolean mControlSelected;
    private boolean mControlEnabled;

    public SideControlView(Context context) {
        super(context);
        init(null, 0);
        mControlSelected = false;
        mControlEnabled = true;
    }

    public SideControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        mControlSelected = false;
        mControlEnabled = true;
    }

    public SideControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
        mControlSelected = false;
        mControlEnabled = true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        ConstraintSet cons = new ConstraintSet();
        cons.clone(this);

        cons.connect(mTextView.getId(), ConstraintSet.BOTTOM, getId(), ConstraintSet.BOTTOM, 16);
        cons.connect(mTextView.getId(), ConstraintSet.LEFT, getId(), ConstraintSet.LEFT, 0);
        cons.connect(mTextView.getId(), ConstraintSet.RIGHT, getId(), ConstraintSet.RIGHT, 0);

        cons.connect(mImageView.getId(), ConstraintSet.TOP, getId(), ConstraintSet.TOP, 16);
        cons.connect(mImageView.getId(), ConstraintSet.LEFT, getId(), ConstraintSet.LEFT, 16);
        cons.connect(mImageView.getId(), ConstraintSet.RIGHT, getId(), ConstraintSet.RIGHT, 16);

        cons.applyTo(this);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // Load attributes.
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SideControlView, defStyle, 0);

        mText = a.getString(
                R.styleable.SideControlView_scText);

        if (a.hasValue(R.styleable.SideControlView_scIcon)) {
            mIcon = a.getDrawable(R.styleable.SideControlView_scIcon);
        }

        a.recycle();

        // Set the background/border.
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_control));

        // Add a text view.
        mTextView = new TextView(getContext());
        mTextView.setId(R.id.side_control_text);
        mTextView.setText(mText);
        addView(mTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Add an image view.
        mImageView = new ImageView(getContext());
        mImageView.setId(R.id.side_control_icon);
        mImageView.setImageDrawable(mIcon);
        addView(mImageView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public void setControlSelected(boolean selected)
    {
        mControlSelected = selected;
        updateControlBackground();
    }

    public void setControlEnabled(boolean enabled)
    {
        mControlEnabled = enabled;
        updateControlBackground();
    }

    private void updateControlBackground()
    {
        if (!mControlEnabled)
        {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_control_disabled));
        }
        else if (mControlSelected)
        {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_control_selected));
        }
        else
        {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_control));
        }
    }

    public boolean isControlEnabled()
    {
        return mControlEnabled;
    }

    public boolean isControlSelected()
    {
        return mControlSelected;
    }

    public void setControlText(String text)
    {
        if (mTextView != null)
            mTextView.setText(text);
    }

    public String getControlText()
    {
        return mTextView.getText().toString();
    }
}
