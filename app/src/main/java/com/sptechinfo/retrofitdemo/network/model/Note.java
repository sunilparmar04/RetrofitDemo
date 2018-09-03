package com.sptechinfo.retrofitdemo.network.model;

/**
 * Created by sunil on 03/09/18.
 */

public class Note extends BaseResponse{
    int id;
    String note;
    String timestamp;

    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
