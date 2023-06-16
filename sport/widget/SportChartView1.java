package com.desay.iwan2.module.sport.widget;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.artfulbits.aiCharts.ChartGestureListener;
import com.artfulbits.aiCharts.ChartView;
import com.artfulbits.aiCharts.Base.ChartArea;
import com.artfulbits.aiCharts.Base.ChartAxis;
import com.artfulbits.aiCharts.Base.ChartAxis.Label;
import com.artfulbits.aiCharts.Base.ChartAxis.LabelsAdapter;
import com.artfulbits.aiCharts.Base.ChartAxisScale;
import com.artfulbits.aiCharts.Base.ChartCollection;
import com.artfulbits.aiCharts.Base.ChartNamedCollection;
import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartSeries;
import com.artfulbits.aiCharts.Enums.Alignment;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.Sport;

import dolphin.tools.util.DensityUtil;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

public class SportChartView1 extends ChartView /*implements LabelsAdapter*/ {

    private float textSize1, textSize2;
    private float dialogPadH, dialogPadV;
    private float rowledge;

    public SportChartView1(Context context) {
        super(context);
        initDialog(context);
    }

    private float xLabelSize, yLabelSize;

    public SportChartView1(Context context, AttributeSet attributeSet) {
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
//                LogUtil.i("onSingleTapUp");
                flag = true;
                Rect rect = area.getSeriesBounds();
                double coef = (motionEvent.getX() - rect.left) / rect.width();
                position = getNearPointX(series.getActualXAxis().getScale().coefficientToValue(coef));

                // 提示窗文字
                ChartPoint nowPoint = getSelectPoint();
                if (nowPoint != null) {
                    Sport s = (Sport) nowPoint.getTag();
                    if (s == null) {
                        text1 = "";
                        text2 = "";
                        text3 = "";
                    } else {
                        text1 = SystemContant.timeFormat3.format(s.getStartTime()) +
                                "~" +
                                SystemContant.timeFormat3.format(new Date(s.getStartTime().getTime() + SystemContant.sportMotionInterval * 60 * 1000));
                        text2 = getContext().getString(s.getAerobics() ? R.string.SportAerobic : R.string.SportAnaerobic);
                        text3 = "" + SystemContant.sportMotionInterval + getContext().getString(R.string.SportTimeUnit) +
                                " "
                                + s.getStepCount() + getContext().getString(R.string.SportStepUnit);

                        invalidate();
                    }
                }

                return super.onSingleTapUp(motionEvent);
            }
        });
    }

    private void initDialog(Context context) {
        textSize1 = context.getResources().getDimension(R.dimen.sport_statistics_day_chart_dialog_text_size1);
        textSize2 = context.getResources().getDimension(R.dimen.sport_statistics_day_chart_dialog_text_size2);
        dialogPadV = context.getResources().getDimension(R.dimen.sport_statistics_day_chart_dialog_pad_vertical);
        dialogPadH = context.getResources().getDimension(R.dimen.sport_statistics_day_chart_dialog_pad_horizon);
        rowledge = context.getResources().getDimension(R.dimen.sport_statistics_day_chart_dialog_text_rowledge);
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

        if (nextPoint == null)
            return 0;

        return nextPoint.getX();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (flag && (!StringUtil.isBlank(text1) || !StringUtil.isBlank(text2) || !StringUtil.isBlank(text3))) {
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
            boolean isShowAerobicMark = getContext().getString(R.string.SportAerobic).equals(text2);
            Rect r2 = new Rect();
            if (!StringUtil.isBlank(text2) && isShowAerobicMark) {
                paint.setTextSize(textSize2);
                paint.getTextBounds(text2, 0, text2.length(), r2);
            }
            Rect r3 = new Rect();
            if (!StringUtil.isBlank(text3)) {
                paint.setTextSize(textSize1);
                paint.getTextBounds(text3, 0, text3.length(), r3);
            }
            int w = r3.width();
            if (w < r2.width()) w = r2.width();
            if (w < r1.width()) w = r1.width();

            float dialogW = w + dialogPadH * 2;
            float dialogH = r1.height() + r2.height() + r3.height() + rowledge * 2 + dialogPadV * 2;
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
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.rgb(0xff, 0xff, 0xff));
//            float textX = x - dialogW / 2 + dialogPadH;
            float textX = x - w / 2;
            float textY = rectF1.top + dialogPadV;
            float y1 = 0;
            if (!StringUtil.isBlank(text1)) {
                paint.setTextSize(textSize1);
                y1 = textY + r1.height();
                canvas.drawText(text1, textX, y1, paint);
            }
            float y2 = y1;
            if (!StringUtil.isBlank(text2) && isShowAerobicMark) {
                paint.setTextSize(textSize2);
                y2 += r2.height() + rowledge;
                canvas.drawText(text2, textX, y2, paint);
            }
            float y3 = y2;
            if (!StringUtil.isBlank(text3)) {
                paint.setTextSize(textSize1);
                y3 += r3.height() + rowledge;
                canvas.drawText(text3, textX, y3, paint);
            }
        }
    }

    private ChartArea area;
    private ChartAxis axisX;
    private ChartNamedCollection<ChartSeries> seriesCollection;
    private ChartSeries series;
    private boolean flag = false;

    private double position;

    private RectF rectF1 = new RectF();
    private RectF rectF2 = new RectF();

    private String text1, text2, text3;

    private Date todayDate;

//    private DecimalFormat decimalFormat = new DecimalFormat("00");

    public void init() {
        area = getAreas().get(0);
        seriesCollection = getSeries();
        series = seriesCollection.get(seriesCollection.size() - 1);

        axisX = area.getDefaultXAxis();
        axisX.setLineType(0,Color.TRANSPARENT);
//        axisX.setLabelLayoutMode(ChartAxis.LabelLayoutMode.Hide);
//        axisX.setValueType(ChartAxis.ValueType.Date);
//        axisX.setLabelsAdapter(null);


        TextPaint xPaint = axisX.getLabelPaint();
        axisX.setPadding((int) (xLabelSize - xPaint.getTextSize()));
        xPaint.setColor(Color.rgb(0x71, 0x70, 0x70));
        xPaint.setTextSize(xLabelSize);

        area.getDefaultYAxis().setLineType(0,Color.TRANSPARENT);
        TextPaint yPaint = area.getDefaultYAxis().getLabelPaint();
        yPaint.setColor(Color.rgb(0x71, 0x70, 0x70));
        yPaint.setTextSize(yLabelSize);

    }

    public void setTodayDate(Date todayDate) {
        this.todayDate = todayDate;

        ChartCollection<Label> labels = axisX.getCustomLabels();
        Calendar cl = Calendar.getInstance();
        cl.setTime(todayDate);
        cl.set(Calendar.HOUR_OF_DAY, 1);
        cl.set(Calendar.MINUTE, 0);
        Label label = new Label("01:00", cl);
        labels.add(label);

        cl.set(Calendar.HOUR_OF_DAY, 12);
        label = new Label("12:00", cl);
        labels.add(label);

        cl.set(Calendar.HOUR_OF_DAY, 23);
        label = new Label("23:00", cl);
        labels.add(label);

    }

//    public ChartArea getArea() {
//        return area;
//    }

    private ChartPoint getSelectPoint() {
        ChartPoint nextPoint = null;

        for (ChartPoint point : series.getPointsCache()) {
            nextPoint = point;
            double x = nextPoint.getX();
            double y = nextPoint.getY(0);
            if (position == x)
                return nextPoint;
        }

        return null;
    }

//    @Override
//    public void updateLabels(ChartAxis arg0, List<Label> arg1) {
//        if (arg1.size() > 0) {
//            LogUtil.e("label position " + arg1.get(0).getPosition());
//        }
//    }
}
