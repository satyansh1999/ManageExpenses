package androidsamples.java.DigitalDiary;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface JournalEntryDAO {
    @Insert
    void insert(JournalEntry entry);

    @Update
    void update(JournalEntry entry);

    @Delete
    void delete(JournalEntry entry);

    @Query("SELECT * from diary_table WHERE id=(:id)")
    LiveData<JournalEntry> getEntry(java.util.UUID id);

    @Query("SELECT * from diary_table where `group`=(:grp) and text != '_'")
    LiveData<List<JournalEntry>> getAllEntriesOfGroup(String grp);

    @Query("SELECT DISTINCT `group` from diary_table")
    LiveData<List<String>> getAllGroups();

    @Query("Delete from diary_table where `group`=(:grp)")
    void deleteGroup(String grp);

    @Query("UPDATE diary_table SET `group`=(:grp_new) where `group`=(:grp_old)")
    void updateGroup(String grp_old, String grp_new);
}
