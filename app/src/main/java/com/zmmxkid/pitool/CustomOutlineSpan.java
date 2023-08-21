package com.zmmxkid.pitool;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

public class CustomOutlineSpan extends ReplacementSpan {

    private int outlineColor;
    private float outlineWidth;

    public CustomOutlineSpan(int outlineColor, float outlineWidth) {
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
    }

    @Override
    public int getSize(
            Paint paint,
            CharSequence text,
            int start,
            int end,
            Paint.FontMetricsInt fm) {
        return (int) (paint.measureText(text, start, end) + 2 * outlineWidth);
    }

    @Override
    public void draw(
            Canvas canvas,
            CharSequence text,
            int start,
            int end,
            float x,
            int top,
            int y,
            int bottom,
            Paint paint) {
        TextPaint textPaint = new TextPaint(paint);
        textPaint.setColor(outlineColor);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(outlineWidth);

        canvas.drawText(text, start, end, x, y, textPaint);
        canvas.drawText(text, start, end, x, y, paint);
    }
}

