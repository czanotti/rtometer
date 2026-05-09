package com.rtometer.data.db;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.rtometer.data.model.DayStatus;

import java.time.LocalDate;

@Entity(
    tableName = "attendance_days",
    foreignKeys = {
        @ForeignKey(
            entity = Quarter.class,
            parentColumns = "id",
            childColumns = "quarterId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Office.class,
            parentColumns = "id",
            childColumns = "detectedOfficeId",
            onDelete = ForeignKey.SET_NULL
        )
    },
    indices = {
        @Index("quarterId"),
        @Index("date")
    }
)
public class AttendanceDay {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public LocalDate date;
    public long quarterId;
    public DayStatus status;
    public boolean isManualOverride;
    @Nullable
    public Long detectedOfficeId;
}
