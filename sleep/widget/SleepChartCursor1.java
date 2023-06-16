package com.desay.iwan2.module.sleep.widget;

import android.graphics.*;
import android.view.MotionEvent;
import com.artfulbits.aiCharts.Base.*;
import dolphin.tools.util.StringUtil;

import java.text.DecimalFormat;

public class SleepChartCursor1 {
    public interface PositionListener {
        void onPositionChanged(SleepChartCursor1 cursor, double xPosition);
    }

    private double position;

    private ChartArea area;
    private ChartNamedCollection<ChartSeries> seriesCollection;
    private ChartSeries series;

    private PositionListener positionListener;

    private SleepChartView1 chartView;
    private boolean flag = false;

    private RectF rectF1 = new RectF();
    private RectF rectF2 = new RectF();

    private String text1, text2, text3;

    private DecimalFormat decimalFormat = new DecimalFormat("00");

    public SleepChartCursor1(SleepChartView1 chartView) {
        this.chartView = chartView;

        area = chartView.getAreas().get(0);
        seriesCollection = chartView.getSeries();
        series = seriesCollection.get(seriesCollection.size() - 1);
    }

    public void draw(Canvas canvas) {
        if (flag) {
            Rect rect = area.getSeriesBounds();

            float dialogW = rect.width() / 6.2f;
            float dialogH = dialogW / 1.9f;

            ChartAxisScale xScale = series.getActualXAxis().getScale();
            int x = (int) (rect.left + rect.width()
                    * xScale.valueToCoefficient(position));

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.argb(0x8f, 0x00, 0x00, 0x00));
            paint.setStyle(Paint.Style.FILL);
            //框背景
            rectF1.left = x - dialogW / 2;
            rectF1.top = rect.top;
            rectF1.right = rectF1.left + dialogW;
            rectF1.bottom = rectF1.top + dialogH;
            float radius = dialogH / 10;
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
            float textX = rectF1.left + rectF1.width() / 9.5f;
            float rowledge = radius;
            float textY = 0;
            float textSize2 = 0;
            if (!StringUtil.isBlank(text2)) {
                textSize2 = dialogH / 3.6f;
                paint.setTextSize(textSize2);
                textY = rectF1.top + (rectF1.height() + textSize2) / 2;
                canvas.drawText(text2, textX, textY, paint);
            }
            float textSize3 = 0;
            if (!StringUtil.isBlank(text3)) {
                textSize3 = dialogH / 5f;
                paint.setTextSize(textSize3);
                canvas.drawText(text3, textX, textY + rowledge + textSize3, paint);
            }
            float textSize1 = 0;
            if (!StringUtil.isBlank(text1)) {
                textSize1 = dialogH / 5.6f;
                paint.setTextSize(textSize1);
                canvas.drawText(text1, textX, textY - textSize2 - rowledge, paint);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                flag = true;
                Rect rect = area.getSeriesBounds();
                double coef = (event.getX() - rect.left) / rect.width();
                position = series.getActualXAxis().getScale().coefficientToValue(coef);

                //TODO 提示窗文字
                text1 = "03:12~0.:58";
                text2 = "做梦：0h46'";
                text3 = "心率：" + decimalFormat.format(getHeartrateValue()) + "次/分钟";

                if (positionListener != null)
                    positionListener.onPositionChanged(this, position);

                return true;
            }
            case MotionEvent.ACTION_UP:
                flag = false;
                return true;
        }

        return false;
    }

    private double getHeartrateValue() {
        ChartPoint prevPoint = null;
        ChartPoint nextPoint = null;

        for (ChartPoint point : seriesCollection.get("heartrate").getPointsCache()) {
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

        return nextPoint.getY(0);
    }

//    public PositionListener getPositionListener() {
//        return positionListener;
//    }
//
//    public void setPositionListener(PositionListener positionListener) {
//        this.positionListener = positionListener;
//    }
}
