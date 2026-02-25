package com.zmmxkid.pitool;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class DiagonalCutImageView extends AppCompatImageView {

    private Path mClipPath;
    private float mCutRatio = 0.15f;  // 默认切掉 15%（浅切）
    private Paint mEdgePaint;
    private boolean mShowEdge = false;

    public DiagonalCutImageView(Context context) {
        super(context);
        init(null);
    }

    public DiagonalCutImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DiagonalCutImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mClipPath = new Path();

        // 读取 XML 属性
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.DiagonalCutImageView);
            mCutRatio = ta.getFloat(R.styleable.DiagonalCutImageView_cutRatio, 0.15f);
            int edgeColor = ta.getColor(R.styleable.DiagonalCutImageView_cutEdgeColor, Color.TRANSPARENT);
            float edgeWidth = ta.getDimension(R.styleable.DiagonalCutImageView_cutEdgeWidth, 0f);
            ta.recycle();

            if (edgeWidth > 0) {
                mShowEdge = true;
                mEdgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mEdgePaint.setColor(edgeColor);
                mEdgePaint.setStyle(Paint.Style.STROKE);
                mEdgePaint.setStrokeWidth(edgeWidth);
            }
        }

        // 确保比例在合理范围（0-0.5）
        mCutRatio = Math.max(0f, Math.min(0.5f, mCutRatio));
    }

    /**
     * 动态设置切角比例（0.0 - 0.5）
     * @param ratio 0.0=不切，0.5=切到中心，0.15=轻微刀切，0.3=深水痕
     */
    public void setCutRatio(float ratio) {
        mCutRatio = Math.max(0f, Math.min(0.5f, ratio));
        invalidate();
    }

    public float getCutRatio() {
        return mCutRatio;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        if (w == 0 || h == 0) {
            super.onDraw(canvas);
            return;
        }

        // 计算实际切角尺寸（取最小边的比例，保持45度）
        float cutSize = Math.min(w, h) * mCutRatio;

        mClipPath.reset();

        // 构建切角路径：缺右上（Top-Right）和 缺左下（Bottom-Left）
        mClipPath.moveTo(0, 0);                    // 左上（保留）
        mClipPath.lineTo(w - cutSize, 0);          // 上边 → 右上切角起点
        mClipPath.lineTo(w, cutSize);              // 右上切角终点（斜线）
        mClipPath.lineTo(w, h);                    // 右下（保留）
        mClipPath.lineTo(cutSize, h);              // 下边 → 左下切角起点
        mClipPath.lineTo(0, h - cutSize);          // 左下切角终点（斜线）
        mClipPath.close();                         // 回到左上

        // 保存画布状态并裁剪
        canvas.save();
        canvas.clipPath(mClipPath);

        // 绘制图片内容
        super.onDraw(canvas);

        // 绘制切面边缘（可选，增强立体感）
        if (mShowEdge && mEdgePaint != null) {
            canvas.drawPath(mClipPath, mEdgePaint);
        }

        canvas.restore();
    }
}