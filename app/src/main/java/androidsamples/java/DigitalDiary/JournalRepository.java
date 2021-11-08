package androidsamples.java.DigitalDiary;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JournalRepository {
    private static final String DATABASE_NAME = "diary_table";
    private static JournalRepository sInstance;
    private final JournalEntryDAO mJournalEntryDao;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private JournalRepository(Context context) {
        JournalRoomDatabase db
                = Room.databaseBuilder(context.getApplicationContext(),
                JournalRoomDatabase.class,
                DATABASE_NAME).build();
        mJournalEntryDao = db.journalEntryDao();
    }

    public static void init(Context context) {
        if (sInstance == null) sInstance = new JournalRepository(context);
    }

    public static JournalRepository getInstance() {
        if (sInstance == null)
            throw new IllegalStateException("Repository must be initialized");
        return sInstance;
    }

    public void insert(JournalEntry entry) {
        mExecutor.execute(() -> mJournalEntryDao.insert(entry));
    }

    public void update(JournalEntry entry) {
        mExecutor.execute(() -> mJournalEntryDao.update(entry));
    }

    public void delete(JournalEntry entry) {
        mExecutor.execute(() -> mJournalEntryDao.delete(entry));
    }

    public LiveData<JournalEntry> getEntry(java.util.UUID id) {
        return mJournalEntryDao.getEntry(id);
    }

    public LiveData<List<JournalEntry>> getAllEntriesOfGroup(String grp) {
        return mJournalEntryDao.getAllEntriesOfGroup(grp);
    }

    public LiveData<List<String>> getAllGroups() {
        return mJournalEntryDao.getAllGroups();
    }

    public void deleteGroup(String grp) {
        mExecutor.execute(() -> mJournalEntryDao.deleteGroup(grp));
    }

    public void updateGroup(String grp_old, String grp_new) {
        mExecutor.execute(() -> mJournalEntryDao.updateGroup(grp_old, grp_new));
    }
}