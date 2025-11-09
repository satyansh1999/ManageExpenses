package androidsamples.java.ManageExpenses;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Comparator for sorting JournalEntry objects by date.
 * Handles malformed dates gracefully to prevent crashes.
 */
public class EntryComparator implements Comparator<JournalEntry> {
    private static final String TAG = "EntryComparator";
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
    
    private final HashMap<String, Integer> monthMap = new HashMap<>();

    public EntryComparator() {
        // Month name to number mapping
        monthMap.put("Jan", 1);
        monthMap.put("Feb", 2);
        monthMap.put("Mar", 3);
        monthMap.put("Apr", 4);
        monthMap.put("May", 5);
        monthMap.put("Jun", 6);
        monthMap.put("Jul", 7);
        monthMap.put("Aug", 8);
        monthMap.put("Sep", 9);
        monthMap.put("Oct", 10);
        monthMap.put("Nov", 11);
        monthMap.put("Dec", 12);
    }

    @Override
    public int compare(JournalEntry o1, JournalEntry o2) {
        // Null checks first
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        
        String date1 = o1.getDate();
        String date2 = o2.getDate();
        
        if (date1 == null && date2 == null) return 0;
        if (date1 == null) return -1;
        if (date2 == null) return 1;
        
        // Try parsing with SimpleDateFormat first (most robust)
        try {
            Date d1 = DATE_FORMAT.parse(date1);
            Date d2 = DATE_FORMAT.parse(date2);
            if (d1 != null && d2 != null) {
                return d1.compareTo(d2);
            }
        } catch (ParseException e) {
            Log.w(TAG, "Date parse failed, trying manual parsing: " + date1 + ", " + date2);
        }
        
        // Fallback to manual parsing with safety checks
        try {
            return compareManually(date1, date2);
        } catch (Exception e) {
            Log.e(TAG, "Failed to compare dates: " + date1 + " vs " + date2, e);
            // Final fallback to string comparison
            return date1.compareTo(date2);
        }
    }
    
    /**
     * Manual date comparison for format: "EEE, MMM dd, yyyy"
     * Example: "Mon, Jan 01, 2024"
     */
    private int compareManually(String str1, String str2) {
        // Validate minimum length
        if (str1.length() < 17 || str2.length() < 17) {
            return str1.compareTo(str2);
        }
        
        try {
            // Extract year (last 4 characters)
            int year1 = Integer.parseInt(str1.substring(str1.length() - 4));
            int year2 = Integer.parseInt(str2.substring(str2.length() - 4));
            if (year1 != year2) {
                return year1 - year2;
            }
            
            // Extract month name (positions 5-8: "MMM")
            String month1Str = str1.substring(5, 8);
            String month2Str = str2.substring(5, 8);
            
            // Check if month exists in map
            Integer month1 = monthMap.get(month1Str);
            Integer month2 = monthMap.get(month2Str);
            
            if (month1 == null || month2 == null) {
                Log.w(TAG, "Invalid month: " + month1Str + " or " + month2Str);
                return str1.compareTo(str2);
            }
            
            if (!month1.equals(month2)) {
                return month1 - month2;
            }
            
            // Extract day (positions 9-11: "dd")
            String day1Str = str1.substring(9, 11).trim();
            String day2Str = str2.substring(9, 11).trim();
            
            int day1 = Integer.parseInt(day1Str);
            int day2 = Integer.parseInt(day2Str);
            
            return day1 - day2;
            
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            Log.e(TAG, "Parse error in manual comparison", e);
            return str1.compareTo(str2);
        }
    }
}

