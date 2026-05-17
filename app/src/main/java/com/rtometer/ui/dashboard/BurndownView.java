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

    private static final String[] MONTH_SHORT = {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private int totalWorkingDays;
    private int daysTarget;
    private List<int[]> series = Collections.emptyList();
    private List<int[]> monthBoundaries = Collections.emptyList();

    private final Paint targetPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint actualPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint monthLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint monthLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
        float dp = getResources().getDisplayMetrics().density;
        float strokeWidth = dp * 2f;

        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setColor(0xFFBDBDBD);
        targetPaint.setStrokeWidth(strokeWidth);
        targetPaint.setPathEffect(new DashPathEffect(new float[]{16f, 10f}, 0f));

        actualPaint.setStyle(Paint.Style.STROKE);
        actualPaint.setStrokeWidth(strokeWidth * 1.5f);
        actualPaint.setStrokeCap(Paint.Cap.ROUND);
        actualPaint.setStrokeJoin(Paint.Join.ROUND);

        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setColor(0xFFE0E0E0);
        axisPaint.setStrokeWidth(1f);

        monthLinePaint.setStyle(Paint.Style.STROKE);
        monthLinePaint.setColor(0xFFCFD8DC); // blue-grey 100
        monthLinePaint.setStrokeWidth(dp);
        monthLinePaint.setPathEffect(new DashPathEffect(new float[]{6f, 6f}, 0f));

        monthLabelPaint.setColor(0xFF9E9E9E); // grey 600
        monthLabelPaint.setTextSize(dp * 10f);
        monthLabelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int totalWorkingDays, int daysTarget,
                        List<int[]> series, List<int[]> monthBoundaries, PaceStatus status) {
        this.totalWorkingDays = totalWorkingDays;
        this.daysTarget = daysTarget;
        this.series = series != null ? series : Collections.emptyList();
        this.monthBoundaries = monthBoundaries != null ? monthBoundaries : Collections.emptyList();
        actualPaint.setColor(colorForStatus(status));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (totalWorkingDays == 0 || daysTarget == 0) return;

        float dp = getResources().getDisplayMetrics().density;
        float topPad    = dp * 16f; // room for month labels
        float sidePad   = dp * 8f;
        float bottomPad = dp * 8f;

        float chartW = getWidth()  - sidePad * 2f;
        float chartH = getHeight() - topPad - bottomPad;

        float xScale = chartW / totalWorkingDays;
        float yScale = chartH / daysTarget;

        float originX = sidePad;
        float originY = topPad + chartH;

        // Month vertical lines and labels (drawn first, behind everything else)
        for (int[] boundary : monthBoundaries) {
            int dayIdx  = boundary[0];
            int monthNum = boundary[1];
            float x = originX + dayIdx * xScale;
            String label = MONTH_SHORT[monthNum];

            // Don't draw a line at dayIndex=1 — it sits on top of the Y-axis
            if (dayIdx > 1) {
                canvas.drawLine(x, topPad, x, originY, monthLinePaint);
            }
            canvas.drawText(label, x, topPad - dp * 3f, monthLabelPaint);
        }

        // Axis lines
        canvas.drawLine(originX, topPad, originX, originY, axisPaint);
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
