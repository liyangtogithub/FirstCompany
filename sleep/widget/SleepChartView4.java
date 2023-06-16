package com.desay.iwan2.module.sleep.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.artfulbits.aiCharts.Base.*;
import com.artfulbits.aiCharts.ChartGestureListener;
import com.artfulbits.aiCharts.ChartView;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.module.sleep.server.SleepYearServer;
import com.desay.iwan2.util.TimeUtil;
import dolphin.tools.util.DensityUtil;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class SleepChartView4 extends ChartView {

    private float textSize1, textSize2;
    private float dialogPadH, dialogPadV;
    private float rowledge;

    public SleepChartView4(Context context) {
        super(context);
        initDialog(context);
    }

    private float xLabelSize, yLabelSize;

    public SleepChartView4(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initDialog(context);

        TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.Charts);
        try {
            xLabelSize = attributes.getDimensionPixelSize(R.styleable.Charts_xLabelSize, DensityUtil.dip2px(getContext(), 11));
            yLabelSize = attributes.getDimensionPixelSize(R.styleable.Charts_yLabelSize, DensityUtil.dip2px(getContext(), 9));
        } finally {
            attributes.recycle();
        }

        setGestureDetector(new ChartGestureListener(this) {
            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                LogUtil.i("onSingleTapUp");
                flag = false;
                Rect rect = area.getSeriesBounds();
                double coef = (motionEvent.getX() - rect.left) / rect.width();
                ChartPoint nearPoint = getNearPoint(series, series.getActualXAxis().getScale().coefficientToValue(coef));
                position = nearPoint == null ? 0 : nearPoint.getX();
//                position = getNearPointX(series.getActualXAxis().getScale().coefficientToValue(coef));

                text1 = text2 = "";
                if (nearPoint != null && nearPoint.getTag() != null)
                    if (nearPoint.getTag() instanceof SleepYearServer.SleepDuration) {
                        flag = true;
                        SleepYearServer.SleepDuration tag = (SleepYearServer.SleepDuration) nearPoint.getTag();
                        text1 = SystemContant.timeFormat11.format(tag.date);
                        text2 = TimeUtil.formatTimeString((int) tag.duration);
                    }

                invalidate();
                return super.onSingleTapUp(motionEvent);
            }
        });
    }

    private void initDialog(Context context) {
        textSize1 = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_text_size1);
        textSize2 = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_text_size2);
        dialogPadV = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_pad_vertical);
        dialogPadH = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_pad_horizon);
        rowledge = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_text_rowledge);
    }

    private double getNearPointX(double position) {
        ChartPoint prevPoint = null;
        ChartPoint nextPoint = null;

        for (ChartPoint point : series.getPointsCache()) {
            nextPoint = point;

            if (position == nextPoint.getX())
                return nextPoint.getX();

            if (position < nextPoint.getX()) {
                if (prevPoint == null)
                    return nextPoint.getX();

                double x1 = prevPoint.getX();
                double x2 = nextPoint.getX();

                return position - x1 > x2 - position ? x2 : x1;
            }

            prevPoint = nextPoint;
        }

        return nextPoint.getX();
    }

    private void checkShow() {
        if (cameraListener != null) {
            double l = series.getActualXAxis().getScale().coefficientToValue(0);
            double r = series.getActualXAxis().getScale().coefficientToValue(1);

            double sum = 0;
            int count = 0;
            for (ChartPoint point : series.getPointsCache()) {
                if (point.getX() >= l && point.getX() <= r) {
                    if (point.getTag() instanceof Boolean && !(Boolean) point.getTag()) continue;
                    count++;
                    sum += point.getY(0);
                }
            }
            cameraListener.onShow(l, r, sum, count);
        }
    }

    private CameraListener cameraListener;

    public void setCameraListener(CameraListener cameraListener) {
        this.cameraListener = cameraListener;
    }

    public interface CameraListener {
        void onShow(double l, double r, double sum, int pcount);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        checkShow();

        if (flag) {
            Rect rect = area.getSeriesBounds();

            ChartAxisScale xScale = series.getActualXAxis().getScale();
            int x = (int) (rect.left + rect.width()
                    * xScale.valueToCoefficient(position));

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            Rect r1 = new Rect();
            if (!StringUtil.isBlank(text1)) {
                paint.setTextSize(textSize1);
                paint.getTextBounds(text1, 0, text1.length(), r1);
            }
            Rect r2 = new Rect();
            if (!StringUtil.isBlank(text2)) {
                paint.setTextSize(textSize2);
                paint.getTextBounds(text2, 0, text2.length(), r2);
            }
            int w = r2.width();
            if (w < r1.width()) w = r1.width();

            float dialogW = w + dialogPadH * 2;
            float dialogH = r1.height() + r2.height() + rowledge + dialogPadV * 2;
            float radius = dialogH / 10;

            paint.setColor(Color.argb(0x8f, 0x00, 0x00, 0x00));
            paint.setStyle(Paint.Style.FILL);
            //框背景
            rectF1.left = x - dialogW / 2;
            rectF1.top = rect.top;
            rectF1.right = rectF1.left + dialogW;
            rectF1.bottom = rectF1.top + dialogH;
            canvas.drawRoundRect(rectF1, radius, radius, paint);
            //杆
            float stickW = radius / 5;
            rectF2.left = x - stickW / 2;
            rectF2.top = rectF1.bottom;
            rectF2.right = rectF2.left + stickW;
            rectF2.bottom = rect.bottom;
            canvas.drawRect(rectF2, paint);

            //文字
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.rgb(0xff, 0xff, 0xff));
//            float textX = x - dialogW / 2 + dialogPadH;
            float textX = x;
            float textY = rectF1.top + dialogPadV;
            float y1 = 0;
            if (!StringUtil.isBlank(text1)) {
                paint.setTextSize(textSize1);
                y1 = textY + r1.height();
                canvas.drawText(text1, textX, y1, paint);
            }
            float y2 = y1;
            if (!StringUtil.isBlank(text2)) {
                paint.setTextSize(textSize2);
                y2 += r2.height() + rowledge;
                canvas.drawText(text2, textX, y2, paint);
            }
        }
    }

    private ChartArea area;
    private ChartNamedCollection<ChartSeries> seriesCollection;
    private ChartSeries series;
    private boolean flag = false;

    private double position;

    private RectF rectF1 = new RectF();
    private RectF rectF2 = new RectF();

    private String text1, text2;

    private DecimalFormat decimalFormat = new DecimalFormat("00");

    public void init() {
        area = getAreas().get(0);
        seriesCollection = getSeries();
        series = seriesCollection.get(seriesCollection.size() - 1);

        ChartAxis axisX = area.getDefaultXAxis();
        axisX.setLineType(0,Color.TRANSPARENT);
        axisX.setValueType(ChartAxis.ValueType.Date);
        axisX.setLabelFormat(new SimpleDateFormat("MMM"));
        axisX.setLabelsAdapter(ChartAxis.SERIES_LABELS_ADAPTER);
//        axisX.setLabelsAdapter(this);

        TextPaint xPaint = axisX.getLabelPaint();
        axisX.setPadding((int) (xLabelSize - xPaint.getTextSize()));
        xPaint.setColor(Color.rgb(0x71, 0x70, 0x70));
        xPaint.setTextSize(xLabelSize);

        area.getDefaultYAxis().setLineType(0,Color.TRANSPARENT);
        TextPaint yPaint = area.getDefaultYAxis().getLabelPaint();
        yPaint.setColor(Color.rgb(0x71, 0x70, 0x70));
        yPaint.setTextSize(yLabelSize);
    }

    private ChartPoint getNearPoint(ChartSeries series, double position) {
        ChartPoint prevPoint = null;
        ChartPoint nextPoint = null;

        for (ChartPoint point : series.getPointsCache()) {
            nextPoint = point;

            if (position == nextPoint.getX())
                return nextPoint;

            if (position < nextPoint.getX()) {
                if (prevPoint == null)
                    return nextPoint;

                double x1 = prevPoint.getX();
                double x2 = nextPoint.getX();
                return position - x1 > x2 - position ? nextPoint : prevPoint;
            }

            prevPoint = nextPoint;
        }

        return nextPoint;
    }
}
