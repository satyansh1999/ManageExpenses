package androidsamples.java.ManageExpenses;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

@Entity(tableName = "diary_table")
public class JournalEntry {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public UUID mUid;

    @ColumnInfo(name = "group")
    private String mGroup;

    @ColumnInfo(name = "text")
    private String mText;

    @ColumnInfo(name = "date")
    private String mDate;

    @ColumnInfo(name = "amount")
    private double mAmount;

    @SuppressLint("SimpleDateFormat")
    public JournalEntry() {
        Calendar calendar = Calendar.getInstance();

        mUid = UUID.randomUUID();
        mText = "";
        mGroup = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
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

    public void setGroup(String d){
        this.mGroup = d;
    }
    public String getGroup(){
        return this.mGroup;
    }
}
