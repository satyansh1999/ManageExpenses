package androidsamples.java.ManageExpenses;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Room database for managing expense entries.
 * 
 * IMPORTANT: When modifying the schema:
 * 1. Increment the version number
 * 2. Create a new Migration (e.g., MIGRATION_1_2)
 * 3. Register the migration in JournalRepository
 * 4. Test the migration with MigrationTest
 * 
 * Schema export is enabled to track changes - exported schemas are in app/schemas/
 */
@Database(entities = {JournalEntry.class}, version = 3, exportSchema = true)
@TypeConverters(JournalTypeConverters.class)
public abstract class JournalRoomDatabase extends RoomDatabase {
    public abstract JournalEntryDAO journalEntryDao();
    
    /**
     * Migration from version 1 to 2: Add database indices for better performance.
     * Adds indices on 'group' and 'date' columns to speed up queries.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add index on group column for faster filtering by group
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_diary_table_group` ON `diary_table` (`group`)"
            );
            
            // Add index on date column for faster sorting and date queries
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_diary_table_date` ON `diary_table` (`date`)"
            );
        }
    };
    
    /**
     * Migration from version 2 to 3: Add timestamp to date format.
     * Converts existing dates from "EEE, MMM dd, yyyy" to "EEE, MMM dd, yyyy HH:mm:ss"
     * Sets default time to 00:00:00 for existing entries to maintain date-only sorting.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Update all existing date entries to include timestamp
            // Appends " 00:00:00" to existing dates that don't already have a time component
            // This preserves existing date values while adding time precision
            database.execSQL(
                "UPDATE diary_table SET date = date || ' 00:00:00' " +
                "WHERE date NOT LIKE '% __:__:__'"
            );
        }
    };
}
