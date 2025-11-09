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
@Database(entities = {JournalEntry.class}, version = 1, exportSchema = true)
@TypeConverters(JournalTypeConverters.class)
public abstract class JournalRoomDatabase extends RoomDatabase {
    public abstract JournalEntryDAO journalEntryDao();
    
    /**
     * Example migration from version 1 to 2.
     * This shows how to add a new column when the schema changes.
     * 
     * When you need this:
     * 1. Increment @Database version to 2
     * 2. Add new field to JournalEntry.java
     * 3. Uncomment the migration registration in JournalRepository
     * 4. Test thoroughly before release
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Example: Add a currency field
            // database.execSQL(
            //     "ALTER TABLE diary_table ADD COLUMN currency TEXT NOT NULL DEFAULT 'USD'"
            // );
        }
    };
    
    /**
     * Example migration from version 2 to 3.
     * This shows how to add database indices for better performance.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Example: Add indices for frequently queried columns
            // database.execSQL(
            //     "CREATE INDEX IF NOT EXISTS index_diary_table_group ON diary_table(`group`)"
            // );
            // database.execSQL(
            //     "CREATE INDEX IF NOT EXISTS index_diary_table_date ON diary_table(date)"
            // );
        }
    };
}
