package com.sam_chordas.android.stockhawk.ui;


import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.Tooltip;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.events.RequestUpdateEvent;
import com.sam_chordas.android.stockhawk.events.UpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabsFragment extends Fragment {
    private static final String TAG = "TabsFragment";
    private int fragmentId;
    ChartView chartView;
    Tooltip tooltip;


    public TabsFragment() {
    }


    public static TabsFragment newInstance(int id) {
        TabsFragment fragmentFirst = new TabsFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentId = getArguments().getInt("id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =
                inflater.inflate(R.layout.line_graph, container, false);

        chartView = (ChartView) view.findViewById(R.id.linechart);

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#ffffff"));
        gridPaint.setStyle(Paint.Style.FILL);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(.1f));


        chartView

                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint)
                .setAxisThickness(.5f)
                .setAxisColor(Color.WHITE)
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(Color.WHITE)
                .setXAxis(true)
                .setAxisLabelsSpacing(Tools.fromDpToPx(10))
                .setYAxis(true)
                .setTopSpacing(Tools.fromDpToPx(5));


        if (getResources().getBoolean(R.bool.show_y_axis)) {
            chartView.setYLabels(AxisController.LabelPosition.OUTSIDE);
        } else {
            chartView.setYLabels(AxisController.LabelPosition.NONE);
        }

        tooltip = new Tooltip(getContext(), R.layout.linechart_toolltip, R.id.value);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            tooltip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f));

            tooltip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA,0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X,0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y,0f));
        }

        chartView.setTooltips(tooltip);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new RequestUpdateEvent(fragmentId));
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onReceiveUpdate(UpdateEvent event) {
        if (event.getId() != fragmentId) {
            return;
        }

        chartView.reset();
        String[] labels = event.getDates();
        float[] values = event.getValues();

        LineSet lineSet = new LineSet(labels, values);
        lineSet.setSmooth(true);
        lineSet.setDotsRadius(Tools.fromDpToPx(3.5f));
        lineSet.setDotsColor(Color.WHITE);
        lineSet.setThickness(3f).setColor(Color.WHITE);
        chartView.addData(lineSet);

        int min = event.getMin();
        int max = event.getMax();


        min = min - 1;
        max = max + 1;
        if (max - min > 10) {
            // this will prevent XAxis overlay bug of the chartview library
            if (min % 2 != 0) min--;
            if (max % 2 != 0) max++;
            chartView.setAxisBorderValues(min, max);
        }
        else {
            chartView.setAxisBorderValues(min, max, 1);
        }

        chartView.show();

    }

}
