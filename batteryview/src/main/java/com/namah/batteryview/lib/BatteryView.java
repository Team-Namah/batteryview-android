package com.namah.batteryview.lib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

public final class BatteryView extends View {

    // ===================== INTERNAL STATE =====================

    private Paint batteryPaint;
    private Paint fillPaint;
    private Paint textPaint;

    private int batteryLevel = 85;
    private float animatedLevel = 85f;

    // Colors
    private int batteryBackgroundColor = Color.parseColor("#333333");
    private int batteryFillColor = Color.parseColor("#4ADE80");
    private int batteryLowColor = Color.parseColor("#EF4444");

    // Dimensions
    private static final float CORNER_RADIUS = 24f;
    private static final float FILL_CORNER_RADIUS = 16f;
    private static final float PADDING = 8f;

    private float textSizePx;
    private ValueAnimator levelAnimator;

    // ===================== CONSTRUCTORS =====================

    public BatteryView(Context context) {
        super(context);
        init(null);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    // ===================== INITIALIZATION =====================

    private void init(@Nullable AttributeSet attrs) {

        batteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        batteryPaint.setStyle(Paint.Style.FILL);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.BatteryView);

            batteryLevel = ta.getInt(
                    R.styleable.BatteryView_batteryLevel,
                    batteryLevel
            );

            batteryBackgroundColor = ta.getColor(
                    R.styleable.BatteryView_batteryBackgroundColor,
                    batteryBackgroundColor
            );

            batteryFillColor = ta.getColor(
                    R.styleable.BatteryView_batteryFillColor,
                    batteryFillColor
            );

            batteryLowColor = ta.getColor(
                    R.styleable.BatteryView_batteryLowColor,
                    batteryLowColor
            );

            ta.recycle();
        }

        animatedLevel = batteryLevel;

        if (isInEditMode()) {
            batteryLevel = 76;
            animatedLevel = 76;
        }

        batteryPaint.setColor(batteryBackgroundColor);
        updateFillColor();
    }

    // ===================== PUBLIC API (LOCKED) =====================

    /** Sets battery level (0–100). Animates smoothly. */
    public void setBatteryLevel(int level) {
        level = clamp(level, 0, 100);

        if (level == batteryLevel) return;

        animateBatteryLevel(batteryLevel, level);
        batteryLevel = level;
    }

    /** Returns the current battery level (0–100). */
    public int getBatteryLevel() {
        return batteryLevel;
    }

    /** Sets the fill color used above low-battery threshold. */
    public void setBatteryFillColor(int color) {
        batteryFillColor = color;
        invalidate();
    }

    /** Sets the background/body color of the battery. */
    public void setBatteryBackgroundColor(int color) {
        batteryBackgroundColor = color;
        batteryPaint.setColor(color);
        invalidate();
    }

    /** Sets the fill color used when battery is low (≤20%). */
    public void setLowBatteryColor(int color) {
        batteryLowColor = color;
        invalidate();
    }

    // ===================== INTERNAL LOGIC =====================

    private void animateBatteryLevel(float from, float to) {

        if (levelAnimator != null) {
            levelAnimator.cancel();
        }

        levelAnimator = ValueAnimator.ofFloat(from, to);
        levelAnimator.setInterpolator(new DecelerateInterpolator());

        long duration = (long) (Math.abs(to - from) * 8);
        levelAnimator.setDuration(Math.max(duration, 200));

        levelAnimator.addUpdateListener(animation -> {
            animatedLevel = (float) animation.getAnimatedValue();
            updateFillColor();
            invalidate();
        });

        levelAnimator.start();
    }

    private void updateFillColor() {
        fillPaint.setColor(animatedLevel > 20 ? batteryFillColor : batteryLowColor);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    // ===================== VIEW LIFECYCLE =====================

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                resolveSize(50, widthMeasureSpec),
                resolveSize(95, heightMeasureSpec)
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        textSizePx = w * 0.28f;
        textPaint.setTextSize(textSizePx);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        // Battery body
        canvas.drawRoundRect(
                new RectF(0, 0, width, height),
                CORNER_RADIUS,
                CORNER_RADIUS,
                batteryPaint
        );

        // Fill
        float maxFillHeight = height - (PADDING * 2);
        float fillHeight = maxFillHeight * (animatedLevel / 100f);
        float fillTop = height - fillHeight - PADDING;

        canvas.drawRoundRect(
                new RectF(
                        PADDING,
                        fillTop,
                        width - PADDING,
                        height - PADDING
                ),
                FILL_CORNER_RADIUS,
                FILL_CORNER_RADIUS,
                fillPaint
        );

        // Text (top-aligned)
        float textX = width / 2;
        float textTopOffset = height * 0.22f;

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = textTopOffset - fm.ascent;

        textPaint.setColor(
                fillTop < textY ? Color.parseColor("#111827") : Color.WHITE
        );

        canvas.drawText(
                Math.round(animatedLevel) + "%",
                textX,
                textY,
                textPaint
        );
    }
}
