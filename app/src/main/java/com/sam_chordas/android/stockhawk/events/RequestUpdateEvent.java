package com.sam_chordas.android.stockhawk.events;


public class RequestUpdateEvent {


    /**
     * @param id id of the fragment that requested update
     */
    public RequestUpdateEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    int id;
}
