package androidsamples.java.ManageExpenses;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

/**
 * DiffUtil callback for efficiently computing list differences for String (group names).
 * This enables smooth animations and efficient updates to the groups RecyclerView.
 */
public class GroupDiffCallback extends DiffUtil.ItemCallback<String> {
    
    /**
     * Check if two items represent the same group (same name).
     * @param oldItem Previous group name
     * @param newItem New group name
     * @return true if group names are equal
     */
    @Override
    public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
        return oldItem.equals(newItem);
    }

    /**
     * Check if the contents of two groups are the same.
     * For strings, if items are the same, contents are always the same.
     * @param oldItem Previous group name
     * @param newItem New group name
     * @return true if strings are equal
     */
    @Override
    public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
        return oldItem.equals(newItem);
    }
}

