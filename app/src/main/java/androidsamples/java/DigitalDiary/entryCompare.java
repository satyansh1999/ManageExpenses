package androidsamples.java.DigitalDiary;

import android.util.Log;
import java.util.Comparator;
import java.util.HashMap;

public class entryCompare implements Comparator<JournalEntry> {
    HashMap<String,Integer> month = new HashMap<>();
    public entryCompare() {
        month.put("Jan", 1);
        month.put("Feb", 2);
        month.put("Mar", 3);
        month.put("Apr", 4);
        month.put("May", 5);
        month.put("Jun", 6);
        month.put("Jul", 7);
        month.put("Aug", 8);
        month.put("Sep", 9);
        month.put("Oct", 10);
        month.put("Nov", 11);
        month.put("Dec", 12);
    }

    @Override
    public int compare(JournalEntry o1, JournalEntry o2) {
        String str1 = o1.getDate();
        String str2 = o2.getDate();
        int a2 = Integer.parseInt(str1.substring(13));
        int b2 = Integer.parseInt(str2.substring(13));
        if(a2 != b2) return a2 - b2;

        int a1 = month.get(str1.substring(5, 8));
        int b1 = month.get(str2.substring(5, 8));
        if(a1 != b1) return a1 - b1;

        int a = Integer.parseInt(str1.substring(9,11));
        int b = Integer.parseInt(str2.substring(9,11));
        return a - b;
    }
}
