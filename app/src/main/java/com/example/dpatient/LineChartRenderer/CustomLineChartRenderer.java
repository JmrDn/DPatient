package com.example.dpatient.LineChartRenderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class CustomLineChartRenderer extends LineChartRenderer {
    public CustomLineChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

   protected void drawFilled(Canvas c, ILineDataSet dataSet, int stackIndex, int entryIndex, float fromX, float fromY, float toX, float toY) {
       // Customize the background below the line here
       Paint fillPaint = new Paint();
       fillPaint.setStyle(Paint.Style.FILL);

       // Example: Use a LinearGradient for a gradient background
       int startColor = Color.parseColor("#FFD700"); // Replace with your start color
       int endColor = Color.parseColor("#FF6347");   // Replace with your end color
       LinearGradient gradient = new LinearGradient(fromX, fromY, toX, toY, startColor, endColor, Shader.TileMode.CLAMP);

       fillPaint.setShader(gradient);

       // Draw the filled area below the line
       c.drawRect(fromX, fromY, toX, toY, fillPaint);
   }
}
