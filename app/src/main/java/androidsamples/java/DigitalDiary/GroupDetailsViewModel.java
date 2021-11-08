package androidsamples.java.DigitalDiary;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.UUID;

public class GroupDetailsViewModel extends ViewModel {
    private static final String TAG = "GroupDetailsViewModel";
    private final JournalRepository mRepository;
    private final MutableLiveData<UUID> entryIdLiveData = new MutableLiveData<>();

    public GroupDetailsViewModel() {
        mRepository = JournalRepository.getInstance();
    }

    void saveEntry(JournalEntry entry) {
        Log.d(TAG, "Saving entry: " + entry.getUid());
        mRepository.insert(entry);
    }

    void updateGroup(String grp_old, String grp_new) {
        Log.d(TAG, "Updating Group: " + grp_old);
        mRepository.updateGroup(grp_old,grp_new);
    }
}
