package androidsamples.java.journalapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class EntryListViewModel extends ViewModel {
    private final JournalRepository mRepository;

    public EntryListViewModel() {
        mRepository = JournalRepository.getInstance();
    }

    public LiveData<List<JournalEntry>> getAllEntriesOfGroup(String grp) {
        return mRepository.getAllEntriesOfGroup(grp);
    }

    public void insert(JournalEntry entry) {
        mRepository.insert(entry);
    }

    public void update(JournalEntry entry) {
        mRepository.update(entry);
    }

    public void delete(JournalEntry entry) {
        mRepository.delete(entry);
    }

    public void deleteGroup(String grp) {
        mRepository.deleteGroup(grp);
    }
}
