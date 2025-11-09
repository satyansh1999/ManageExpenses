package androidsamples.java.ManageExpenses;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

@Entity(
    tableName = "diary_table",
    indices = {
        @Index(value = "group"),
        @Index(value = "date")
    }
)
public class JournalEntry {
    // Date format with timestamp for database storage and sorting
    public static final String DATE_TIME_FORMAT = "EEE, MMM dd, yyyy HH:mm:ss";
    // Date format for UI display (date only, no time)
    public static final String DATE_DISPLAY_FORMAT = "EEE, MMM dd, yyyy";
    
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public UUID mUid;

    @ColumnInfo(name = "group")
    private String mGroup;

    @ColumnInfo(name = "text")
    private String mText;

    @ColumnInfo(name = "date")
    private String mDate;  // Stored with timestamp: "EEE, MMM dd, yyyy HH:mm:ss"

    @ColumnInfo(name = "amount")
    private double mAmount;

    public JournalEntry() {
        Calendar calendar = Calendar.getInstance();

        mUid = UUID.randomUUID();
        mText = "";
        mGroup = "";
        // Store current date with timestamp for precise sorting
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.US);
        mDate = simpleDateFormat.format(calendar.getTime());
        mAmount = 0.0;
    }

    public void setText(String t){
        this.mText = t;
    }
    public String getText(){
        return this.mText;
    }

    public void setId(UUID id){
        this.mUid = id;
    }
    public UUID getUid(){
        return this.mUid;
    }

    public void setAmount(double t){
        this.mAmount = t;
    }
    public double getAmount(){
        return this.mAmount;
    }

    public void setDate(String d){
        this.mDate = d;
    }
    public String getDate(){
        return this.mDate;
    }
    
    /**
     * Get formatted date for UI display (date only, without time).
     * Extracts just the date portion from the stored timestamp.
     * @return Date string in format "EEE, MMM dd, yyyy"
     */
    public String getFormattedDate() {
        if (mDate == null || mDate.isEmpty()) {
            return "";
        }
        
        // If date already has timestamp format, extract just the date part
        if (mDate.length() > DATE_DISPLAY_FORMAT.length() && mDate.contains(":")) {
            // Extract "EEE, MMM dd, yyyy" from "EEE, MMM dd, yyyy HH:mm:ss"
            return mDate.substring(0, DATE_DISPLAY_FORMAT.length());
        }
        
        // If it's already in display format (legacy data), return as is
        return mDate;
    }

    public void setGroup(String d){
        this.mGroup = d;
    }
    public String getGroup(){
        return this.mGroup;
    }
}
