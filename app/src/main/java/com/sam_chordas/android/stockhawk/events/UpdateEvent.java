package com.sam_chordas.android.stockhawk.events;


public class UpdateEvent {



    private int id;
    private String[] dates;
    private float[] values;
    private int max;
    private int min;

    /**
     * @param id     id of fragment
     * @param dates  dates values for a chart
     * @param values values for a chart
     * @param max    min value for yAxis
     * @param min    max value for YAxis
     */
    public UpdateEvent(int id, String[] dates, float[] values, int max, int min) {
        this.id = id;
        this.dates = dates;
        this.values = values;
        this.max = max;
        this.min = min;
    }

    public String[] getDates() {
        return dates;
    }

        public int getId() {
        return id;
    }
//    public void setId(int id) {
//        this.id = id;
//    }
    public float[] getValues() {
        return values;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }


}
