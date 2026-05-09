package com.rtometer.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offices")
public class Office {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public double latitude;
    public double longitude;
    public int radiusMeters = 200;
    public boolean isPrimary;
}
