package com.desay.iwan2.module.sleep.widget;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.artfulbits.aiCharts.Base.*;
import com.artfulbits.aiCharts.ChartView;
import com.desay.fitband.R;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.util.TimeUtil;
import dolphin.tools.util.StringUtil;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public class SleepChartView1 extends ChartView {

    private float textSize1, textSize2;
    private float dialogPadH, dialogPadV;
    private float rowledge;

    public SleepChartView1(Context context) {
        super(context);
        initDialog(context);
    }

    public SleepChartView1(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initDialog(context);
    }

    private void initDialog(Context context) {
        textSize1 = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_text_size1);
//        LogUtil.i("textSize1="+textSize1);
        textSize2 = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_text_size2);
//        LogUtil.i("textSize2="+textSize2);
        dialogPadV = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_pad_vertical);
        dialogPadH = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_pad_horizon);
        rowledge = context.getResources().getDimension(R.dimen.sleep_statistics_day_chart_dialog_text_rowledge);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (flag) {
            Rect rect = area.getSeriesBounds();

            ChartAxisScale xScale = heartrateSeries.getActualXAxis().getScale();
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
            if (!StringUtil.isBlank(text2)) {
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                flag = true;
                Rect rect = area.getSeriesBounds();
                double coef = (event.getX() - rect.left) / rect.width();
                position = heartrateSeries.getActualXAxis().getScale().coefficientToValue(coef);

                // 提示窗文字
                String stateStr = null;
                ChartSeries stateSeries = null;
                if (getStateY(shallowSeries, position) > 0) {
                    stateStr = getContext().getString(R.string.SleepLight);
                    stateSeries = shallowSeries;
                } else if (getStateY(deepSeries, position) > 0) {
                    stateStr = getContext().getString(R.string.SleepDeep);
                    stateSeries = deepSeries;
                } else if (getStateY(dreamSeries, position) > 0) {
                    stateStr = getContext().getString(R.string.SleepDream);
                    stateSeries = dreamSeries;
                } else if (getStateY(wakeupSeries, position) > 0) {
                    stateStr = getContext().getString(R.string.SleepWake);
                    stateSeries = wakeupSeries;
                }
                if (stateSeries == null) {
                    text1 = "";
                    text2 = getContext().getString(R.string.tips_3);
                } else {
                    double[] stateRangeX = getStateRangeX(stateSeries, position);
                    Date date0 = new Date((long) stateRangeX[0]);
                    Date date1 = new Date((long) stateRangeX[1]);
                    text1 = SystemContant.timeFormat3.format(date0) +
                            "~" +
                            SystemContant.timeFormat3.format(date1);
                    text2 = stateStr + ":" + TimeUtil.formatTimeString((int) ((date1.getTime() - date0.getTime()) / 60000L));
                }
                ChartPoint nearPoint = getNearPoint(heartrateSeries, position);
                if (nearPoint != null && nearPoint.getY(0) == 254) {
                    text3 = getContext().getString(R.string.tips_2);
                } else {
                    text3 = getContext().getString(R.string.SleepHeart) + ":" + decimalFormat.format(getValueY(heartrateSeries, position)) +
                            getContext().getString(R.string.SleepHeartUnit);
                }


                invalidate();
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            case MotionEvent.ACTION_UP:
                flag = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }

        return super.onTouchEvent(event);
    }

    public ChartArea area;
    private ChartNamedCollection<ChartSeries> seriesCollection;
    public ChartSeries shallowSeries, deepSeries, dreamSeries, wakeupSeries, heartrateSeries;
    private boolean flag = false;

    private double position;

    private RectF rectF1 = new RectF();
    private RectF rectF2 = new RectF();

    private String text1, text2, text3;

    private DecimalFormat decimalFormat = new DecimalFormat("00");

    public void init() {
        area = getAreas().get(0);
        seriesCollection = getSeries();
        shallowSeries = seriesCollection.get("shallow");
        deepSeries = seriesCollection.get("deep");
        dreamSeries = seriesCollection.get("dream");
        wakeupSeries = seriesCollection.get("wakeup");
        heartrateSeries = seriesCollection.get("heartrate");
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

    private double getValueY(ChartSeries series, double position) {
        ChartPoint prevPoint = null;
        ChartPoint nextPoint = null;

        for (ChartPoint point : series.getPointsCache()) {
            nextPoint = point;

            if (position == nextPoint.getX())
                return nextPoint.getY(0);

            if (position < nextPoint.getX()) {
                if (prevPoint == null)
                    return nextPoint.getY(0);

                double x1 = prevPoint.getX();
                double x2 = nextPoint.getX();
                double y1 = prevPoint.getY(0);
                double y2 = nextPoint.getY(0);

                return y1 + (y2 - y1) * (position - x1) / (x2 - x1);
            }

            prevPoint = nextPoint;
        }

        return nextPoint == null ? 0 : nextPoint.getY(0);
    }

    private double getStateY(ChartSeries series, double position) {
        ChartPoint prevPoint = null;
        ChartPoint nextPoint = null;

        for (ChartPoint point : series.getPointsCache()) {
            nextPoint = point;

            if (position == nextPoint.getX())
                return nextPoint.getY(0);

            if (position < nextPoint.getX()) {
                if (prevPoint == null)
                    return nextPoint.getY(0);

                return prevPoint.getY(0);
            }

            prevPoint = nextPoint;
        }

        return nextPoint == null ? 0 : nextPoint.getY(0);
    }

    private double[] getStateRangeX(ChartSeries series, double position) {
        double[] range = new double[2];
        if (series == null) return range;
        ChartPoint prevPoint = null;
        ChartPoint nextPoint = null;

        List<ChartPoint> points = series.getPoints();

        ChartPoint point1 = null;
        for (ChartPoint point : series.getPointsCache()) {
            nextPoint = point;

            if (position == nextPoint.getX()) {
                point1 = nextPoint;
                break;
            }

            if (position < nextPoint.getX()) {
                if (prevPoint == null) {
                    point1 = nextPoint;
                }
                break;
            }

            prevPoint = nextPoint;
        }
        if (point1 == null)
            point1 = nextPoint;

        int index = points.indexOf(point1);
        if (point1.getY(0) == 0) {
            if (index > 0) {
                index--;
            }
        }
        for (int i = index; i > -1; i--) {
            ChartPoint point = points.get(i);
            if (point.getY(0) == 0) break;
            range[0] = point.getX();
        }

        for (int i = index; i < points.size(); i++) {
            ChartPoint point = points.get(i);
            if (point.getY(0) == 0) {
                break;
            }
            int m = (Integer) point.getTag();
            range[1] = point.getX() + m * 60000;
        }

        return range;
    }
}
