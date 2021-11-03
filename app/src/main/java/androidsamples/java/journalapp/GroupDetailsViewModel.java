package androidsamples.java.journalapp;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.UUID;

public class GroupDetailsViewModel extends ViewModel {
    private static final String TAG = "GroupDetailsViewModel";
    private final JournalRepository mRepository;
    private final MutableLiveData<UUID> entryIdLiveData = new MutableLiveData<>();

    public GroupDetailsViewModel() {
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

    void saveEntry(JournalEntry entry) {
        Log.d(TAG, "Saving entry: " + entry.getUid());
        mRepository.update(entry);
    }

    void updateGroup(String grp_old, String grp_new) {
        Log.d(TAG, "Updating Group: " + grp_old);
        mRepository.updateGroup(grp_old,grp_new);
    }

    void deleteEntry(JournalEntry entry){
        Log.d(TAG, "Deleting Group: " + entry.getUid());
        mRepository.delete(entry);
    }
}
