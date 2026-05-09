package com.rtometer.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "placeholder")
public class PlaceholderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String value;
}
