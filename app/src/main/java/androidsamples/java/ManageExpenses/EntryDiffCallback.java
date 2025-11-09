package androidsamples.java.ManageExpenses;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

/**
 * DiffUtil callback for efficiently computing list differences for JournalEntry items.
 * This enables smooth animations and efficient updates to the RecyclerView.
 */
public class EntryDiffCallback extends DiffUtil.ItemCallback<JournalEntry> {
    
    /**
     * Check if two items represent the same entry (same UUID).
     * @param oldItem Previous entry
     * @param newItem New entry
     * @return true if items have the same UUID
     */
    @Override
    public boolean areItemsTheSame(@NonNull JournalEntry oldItem, @NonNull JournalEntry newItem) {
        return oldItem.getUid().equals(newItem.getUid());
    }

    /**
     * Check if the contents of two entries are the same.
     * Called only if areItemsTheSame() returns true.
     * @param oldItem Previous entry
     * @param newItem New entry
     * @return true if all fields are equal
     */
    @Override
    public boolean areContentsTheSame(@NonNull JournalEntry oldItem, @NonNull JournalEntry newItem) {
        return oldItem.getText().equals(newItem.getText()) &&
               oldItem.getAmount() == newItem.getAmount() &&
               oldItem.getDate().equals(newItem.getDate()) &&
               oldItem.getGroup().equals(newItem.getGroup());
    }
}

