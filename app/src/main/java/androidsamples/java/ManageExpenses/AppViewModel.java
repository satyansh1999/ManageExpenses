package androidsamples.java.ManageExpenses;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.UUID;

public class AppViewModel extends ViewModel {
    private static final String TAG = "EntryDetailsViewModel";
    private final JournalRepository mRepository;
    private final MutableLiveData<UUID> entryIdLiveData = new MutableLiveData<>();

    public AppViewModel() {
        mRepository = JournalRepository.getInstance();
    }

    LiveData<JournalEntry> getEntryLiveData() {
        Log.d(TAG, "getEntryLiveData called");
        return Transformations.switchMap(entryIdLiveData, mRepository::getEntry);
    }
    void loadEntry(UUID entryId) {
        Log.d(TAG, "loading entry: " + entryId);
        entryIdLiveData.setValue(entryId);
    }
    void update(JournalEntry entry) {
        Log.d(TAG, "Saving entry: " + entry.getUid());
        mRepository.update(entry);
    }
    void insert(JournalEntry entry) {
        Log.d(TAG, "Insert entry: " + entry.getUid());
        mRepository.insert(entry);
    }
    void delete(JournalEntry entry) {
        Log.d(TAG, "Deleting entry: " + entry.getUid());
        mRepository.delete(entry);
    }
    void deleteAll() {
        Log.d(TAG, "Deleting all data");
        mRepository.deleteAll();
    }
    public void deleteGroup(String grp) {
        mRepository.deleteGroup(grp);
    }

    public LiveData<List<JournalEntry>> getAllEntriesOfGroup(String grp) {
        return mRepository.getAllEntriesOfGroup(grp);
    }
    public LiveData<List<JournalEntry>> getAllEntries() {
        return mRepository.getAllEntries();
    }
    public LiveData<List<String>> getAllGroups() {
        return mRepository.getAllGroups();
    }

    void updateGroup(String grp_old, String grp_new) {
        Log.d(TAG, "Updating Group: " + grp_old);
        mRepository.updateGroup(grp_old,grp_new);
    }
}
