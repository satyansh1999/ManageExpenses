package androidsamples.java.journalapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class EntryGroupsViewModel extends ViewModel {
    private final JournalRepository mRepository;

    public EntryGroupsViewModel() {
        mRepository = JournalRepository.getInstance();
    }

    public LiveData<List<String>> getAllGroups() {
        return mRepository.getAllGroups();
    }

    public void insert(JournalEntry entry) {
        mRepository.insert(entry);
    }

    public void update(JournalEntry entry) {
        mRepository.insert(entry);
    }

    public void delete(JournalEntry entry) {
        mRepository.insert(entry);
    }
}
