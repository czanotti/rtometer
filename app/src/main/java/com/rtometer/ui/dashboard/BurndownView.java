package com.rtometer.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.rtometer.calculator.PaceStatus;

import java.util.Collections;
import java.util.List;

public class BurndownView extends View {

    private int totalWorkingDays;
    private int daysTarget;
    private List<int[]> series = Collections.emptyList();

    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint actualPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BurndownView(Context context) {
        super(context);
        init();
    }

    public BurndownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BurndownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float strokeWidth = getResources().getDisplayMetrics().density * 2f;

        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setColor(0xFFBDBDBD); // grey 400
        targetPaint.setStrokeWidth(strokeWidth);
        targetPaint.setPathEffect(new DashPathEffect(new float[]{16f, 10f}, 0f));

        actualPaint.setStyle(Paint.Style.STROKE);
        actualPaint.setStrokeWidth(strokeWidth * 1.5f);
        actualPaint.setStrokeCap(Paint.Cap.ROUND);
        actualPaint.setStrokeJoin(Paint.Join.ROUND);

        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setColor(0xFFE0E0E0); // grey 300
        axisPaint.setStrokeWidth(1f);
    }

    public void setData(int totalWorkingDays, int daysTarget, List<int[]> series, PaceStatus status) {
        this.totalWorkingDays = totalWorkingDays;
        this.daysTarget = daysTarget;
        this.series = series != null ? series : Collections.emptyList();
        actualPaint.setColor(colorForStatus(status));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (totalWorkingDays == 0 || daysTarget == 0) return;

        int w = getWidth();
        int h = getHeight();
        int pad = (int) (getResources().getDisplayMetrics().density * 8);
        float chartW = w - pad * 2f;
        float chartH = h - pad * 2f;

        float xScale = chartW / totalWorkingDays;
        float yScale = chartH / daysTarget;

        float originX = pad;
        float originY = pad + chartH;

        // Axis lines
        canvas.drawLine(originX, pad, originX, originY, axisPaint);
        canvas.drawLine(originX, originY, originX + chartW, originY, axisPaint);

        // Target (pace) line
        Path targetPath = new Path();
        targetPath.moveTo(originX, originY);
        targetPath.lineTo(originX + totalWorkingDays * xScale, originY - daysTarget * yScale);
        canvas.drawPath(targetPath, targetPaint);

        // Actual line — starts from origin
        if (!series.isEmpty()) {
            Path actualPath = new Path();
            actualPath.moveTo(originX, originY);
            for (int[] point : series) {
                float x = originX + point[0] * xScale;
                float y = originY - point[1] * yScale;
                actualPath.lineTo(x, y);
            }
            canvas.drawPath(actualPath, actualPaint);
        }
    }

    private static int colorForStatus(PaceStatus status) {
        if (status == null) return 0xFF9E9E9E;
        switch (status) {
            case GREEN: return 0xFF388E3C;
            case AMBER: return 0xFFF57C00;
            default:    return 0xFFD32F2F;
        }
    }
}
