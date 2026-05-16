package com.rtometer.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(tableName = "quarters")
public class Quarter {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public int quarterNumber;
    public LocalDate startDate;
    public LocalDate endDate;
    public float targetPercentage = 0.5f;
}
