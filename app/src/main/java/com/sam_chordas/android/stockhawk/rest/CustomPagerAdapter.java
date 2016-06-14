package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.sam_chordas.android.stockhawk.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Alexey on 6/8/2016.
 */
public class CustomPagerAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    List<String> titles;
    String[] dates;
    float[] values;

    int axisMax;
    int axisMin;


    public void setData(String[] dates, float[] values, int axisMax, int axisMin) {
        this.dates = dates;
        this.axisMax = axisMax;
        this.axisMin = axisMin;
        this.values = values;

        notifyDataSetChanged();
    }

    public CustomPagerAdapter(Context context, List<String> titles) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.titles = titles;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LineChartView chartView = (LineChartView) layoutInflater.inflate(R.layout.line_graph, container, false);

        String[] strs = new String[0];
        float[] vals = new float[0];
        if (dates != null && values != null) {
            try {
                switch (position) {
                    case  0 : {
                        strs = Arrays.copyOfRange(dates, 0, 6);
                        vals = Arrays.copyOfRange(values, 0, 6);
                    }
                    case  1 : {
                        strs = Arrays.copyOfRange(dates, 0, 15);
                        vals = Arrays.copyOfRange(values, 0, 15);
                    }
                    case  2 : {
                        strs = dates;
                        vals = values;
                    }
                }
            } catch (Exception e) {
                Log.e("error", "instantiateItem: ", e);
            }


            LineSet dataSet = new LineSet(strs, vals);
            dataSet.setColor(Color.WHITE)
//                .setFill(Color.YELLOW)
//                .setDotsColor(Color.GREEN)
                    .setThickness(4)
                    .setSmooth(false);
//                .setDashed(new float[]{10f,10f});

            chartView
                    .setBorderSpacing(Tools.fromDpToPx(10))
                    .setAxisBorderValues(axisMin, axisMax)
                    .setYLabels(AxisController.LabelPosition.OUTSIDE)
                    .setLabelsColor(Color.WHITE)
                    .setXAxis(true)
                    .setAxisLabelsSpacing(10)
                    .setAxisColor(Color.WHITE)
                    .setYAxis(true);

            Animation anim = new Animation()
                    .setEasing(new BounceEase())
                    .setDuration(1000);


            chartView.addData(dataSet);
            chartView.show(anim);


        }

        container.addView(chartView);

        return chartView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
