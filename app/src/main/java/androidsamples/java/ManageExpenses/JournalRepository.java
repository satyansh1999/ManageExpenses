package androidsamples.java.ManageExpenses;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JournalRepository {
    private static final String DATABASE_NAME = "diary_table";
    private static JournalRepository sInstance;
    private final JournalEntryDAO mJournalEntryDao;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private JournalRepository(Context context) {
        JournalRoomDatabase db = Room.databaseBuilder(
                context.getApplicationContext(),
                JournalRoomDatabase.class,
                DATABASE_NAME)
                // Register migrations in order
                .addMigrations(
                    JournalRoomDatabase.MIGRATION_1_2,  // v1->v2: Add indices
                    JournalRoomDatabase.MIGRATION_2_3   // v2->v3: Add timestamps to dates
                )
                .build();
        mJournalEntryDao = db.journalEntryDao();
    }

    public static void init(@NonNull Context context) {
        if (sInstance == null) sInstance = new JournalRepository(context);
    }

    @NonNull
    public static JournalRepository getInstance() {
        if (sInstance == null)
            throw new IllegalStateException("Repository must be initialized");
        return sInstance;
    }

    public void insert(@NonNull JournalEntry entry) {
        mExecutor.execute(() -> mJournalEntryDao.insert(entry));
    }

    public void update(@NonNull JournalEntry entry) {
        mExecutor.execute(() -> mJournalEntryDao.update(entry));
    }

    public void delete(@NonNull JournalEntry entry) {
        mExecutor.execute(() -> mJournalEntryDao.delete(entry));
    }

    @NonNull
    public LiveData<JournalEntry> getEntry(@NonNull UUID id) {
        return mJournalEntryDao.getEntry(id);
    }

    @NonNull
    public LiveData<List<JournalEntry>> getAllEntriesOfGroup(@NonNull String grp) {
        return mJournalEntryDao.getAllEntriesOfGroup(grp);
    }

    @NonNull
    public LiveData<List<JournalEntry>> getAllEntries() {
        return mJournalEntryDao.getAllEntries();
    }

    @NonNull
    public LiveData<List<JournalEntry>> getAllGroups() {
        return mJournalEntryDao.getAllGroups();
    }

    public void deleteGroup(@NonNull String grp) {
        mExecutor.execute(() -> mJournalEntryDao.deleteGroup(grp));
    }

    public void deleteAll() {
        mExecutor.execute(mJournalEntryDao::deleteAll);
    }

    public void updateGroup(@NonNull String grp_old, @NonNull String grp_new) {
        mExecutor.execute(() -> mJournalEntryDao.updateGroup(grp_old, grp_new));
    }
}