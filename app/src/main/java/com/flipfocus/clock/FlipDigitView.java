package com.flipfocus.clock;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/** Self-contained, library-free animated split-flap numeral. */
public final class FlipDigitView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Typeface numberFont = Typeface.create("sans-serif-condensed", Typeface.BOLD);
    private char value = '\0';
    private char former = '\0';
    private float phase = 1f;
    private ValueAnimator animator;

    public FlipDigitView(Context context) { super(context); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }

    public void clear() {
        if (animator != null) animator.cancel();
        value = '\0'; former = '\0'; phase = 1f; invalidate();
    }

    public void setDigit(char digit) {
        if (value == digit) return;
        if (animator != null) animator.cancel();
        former = value;
        value = digit;
        if (former == '\0') {
            phase = 1f;
            invalidate();
            return;
        }
        phase = 0;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(480L);
        animator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width == 0) width = (int) (75 * getResources().getDisplayMetrics().density);
        int natural = Math.round(width * 1.42f);
        int resolved = resolveSize(natural, heightMeasureSpec);
        setMeasuredDimension(width, resolved);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (value == '\0') return;
        float w = getWidth(), h = getHeight(), cy = h / 2f;
        if (phase >= 1 || former == '\0') {
            drawHalf(canvas, value, true, 1f);
            drawHalf(canvas, value, false, 1f);
        } else if (phase < .5f) {
            drawHalf(canvas, value, true, 1f);
            drawHalf(canvas, former, false, 1f);
            float scale = (float) Math.cos(Math.PI * phase);
            drawHalf(canvas, former, true, Math.max(.01f, scale));
        } else {
            drawHalf(canvas, value, true, 1f);
            drawHalf(canvas, former, false, 1f);
            float scale = (float) Math.cos(Math.PI * (1f - phase));
            drawHalf(canvas, value, false, Math.max(.01f, scale));
        }
        paint.setColor(Color.rgb(10, 11, 14));
        canvas.drawRect(0, cy - 1.6f, w, cy + 1.6f, paint);
        paint.setColor(Color.rgb(11, 12, 14));
        canvas.drawCircle(0, cy, h * .023f, paint);
        canvas.drawCircle(w, cy, h * .023f, paint);
    }

    private void drawHalf(Canvas canvas, char digit, boolean upper, float scale) {
        float w = getWidth(), h = getHeight(), mid = h / 2f;
        canvas.save();
        canvas.translate(0, mid);
        canvas.scale(1f, scale);
        canvas.translate(0, -mid);
        canvas.clipRect(0, upper ? 0 : mid, w, upper ? mid : h);

        rect.set(0, 0, w, h);
        paint.setColor(Color.rgb(44, 45, 51));
        canvas.drawRoundRect(rect, h * .095f, h * .095f, paint);
        paint.setColor(Color.rgb(29, 30, 35));
        canvas.drawRect(0, mid, w, h, paint);
        paint.setColor(Color.rgb(246, 243, 235));
        paint.setTypeface(numberFont);
        paint.setTextSize(h * .82f);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float baseline = h / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(String.valueOf(digit), w / 2f - .018f * w, baseline, paint);
        canvas.restore();
    }
}
