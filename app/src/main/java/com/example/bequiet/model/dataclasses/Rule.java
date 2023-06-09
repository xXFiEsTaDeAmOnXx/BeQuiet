package com.example.bequiet.model.dataclasses;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;

@Entity(tableName = "Rule")
public abstract class Rule {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "ruleName")
    private final String ruleName;
    @ColumnInfo(name = "startHour")
    private final int startHour;
    @ColumnInfo(name = "startMinute")
    private final int startMinute;

    @ColumnInfo(name = "endHour")
    private final int endHour;
    @ColumnInfo(name = "endMinute")
    private final int endMinute;
    @ColumnInfo(name = "reactionType")
    private NoiseType reactionType = NoiseType.SILENT;

    public Rule(String ruleName, int startHour, int startMinute, int endHour, int endMinute) {
        this.ruleName = ruleName;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    public NoiseType getReactionType() {
        return reactionType;
    }

    public void setReactionType(NoiseType reactionType) {
        this.reactionType = reactionType;
    }

    public String getRuleName() {
        return ruleName;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        Calendar now = Calendar.getInstance();

        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, endHour);
        end.set(Calendar.MINUTE, endMinute);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        // adjust end time to the next day if it is before start time
        if (end.before(start)) {
            end.add(Calendar.DAY_OF_MONTH, 1);
        }

        // check if the current time is between start and end times
        return now.after(start) && now.before(end);
    }

    public long getDurationEnd() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, endHour);
        end.set(Calendar.MINUTE, endMinute);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        // adjust end time to the next day if it is before start time
        if (end.before(start)) {
            end.add(Calendar.DAY_OF_MONTH, 1);
        }

        return end.getTimeInMillis() - start.getTimeInMillis();
    }

    public long getDurationStart() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        // calculate the start and end time of the rule for today
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);


        // adjust the start and end time to tomorrow if they have already passed today
        if (start.getTimeInMillis() <= now.getTimeInMillis()) {
            start.add(Calendar.DAY_OF_YEAR, 1);
        }

        return start.getTimeInMillis() - now.getTimeInMillis();
    }

}
