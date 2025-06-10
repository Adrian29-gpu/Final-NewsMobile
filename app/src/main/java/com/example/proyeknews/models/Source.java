package com.example.proyeknews.models;

import androidx.room.ColumnInfo;
import java.io.Serializable;

public class Source implements Serializable {
    @ColumnInfo(name = "source_id")
    private String id;

    @ColumnInfo(name = "source_name")
    private String name;

    public Source(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}