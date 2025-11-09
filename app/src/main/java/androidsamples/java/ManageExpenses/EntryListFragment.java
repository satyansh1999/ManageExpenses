package androidsamples.java.ManageExpenses;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * A fragment representing a list of Items.
 */
public class EntryListFragment extends Fragment {
  private static final String TAG = "EntryListFragment";
  private AppViewModel mAppViewModel;
  private Callbacks mCallbacks = null;
  private String group;
  private double grp_total = 0;
  private TextView tv;
  private EntryListAdapter adapter;
  private boolean sortDescending = true; // Default: newest first (descending)

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    mAppViewModel = new ViewModelProvider(this).get(AppViewModel.class);
    
    // Safely extract navigation arguments with null check
    Bundle args = getArguments();
    if (args != null) {
      group = EntryListFragmentArgs.fromBundle(args).getGroup();
    } else {
      Log.e(TAG, "No arguments passed to EntryListFragment, using default group");
      group = "Default";
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_entry_list, container, false);

    tv = view.findViewById(R.id.group_total);
    FloatingActionButton fab = view.findViewById(R.id.btn_add_entry);
    fab.setOnClickListener(this::addNewEntry);

    RecyclerView entriesList = view.findViewById(R.id.recyclerView);
    entriesList.setLayoutManager(new LinearLayoutManager(getActivity()));
    adapter = new EntryListAdapter(getActivity());
    entriesList.setAdapter(adapter);

    mAppViewModel.getAllEntriesOfGroup(group).observe(getViewLifecycleOwner(), adapter::setEntries);
    return view;
  }

  public void addNewEntry(View view) {
    mCallbacks.onEntrySelected(UUID.randomUUID(), false, group);
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    mCallbacks = (Callbacks) context;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mCallbacks = null;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.fragment_entry_list, menu);
    
    // Set initial menu item title and icon based on default sort order
    MenuItem sortItem = menu.findItem(R.id.menu_sort_entries);
    if (sortItem != null) {
      updateSortMenuItem(sortItem);
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_delete_group) {
      Log.d(TAG, "Group Delete button clicked");

      AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
      alert.setTitle("Delete Folder");
      alert.setMessage("Are you sure you want to delete " + group + "?");
      alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
        mAppViewModel.deleteGroup(group);
        Toast.makeText(getContext(), group + " deleted", Toast.LENGTH_SHORT).show();
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
      });
      alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
      alert.show();
      return true;
    } else if (item.getItemId() == R.id.menu_sort_entries) {
      sortDescending = !sortDescending; // Toggle sort order
      
      // Update menu item title and icon to reflect current sort order
      updateSortMenuItem(item);
      
      // Re-sort and refresh the list
      if (adapter != null) {
        adapter.refreshSorting();
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Updates the sort menu item's title and icon based on current sort order.
   * @param sortItem The menu item to update
   */
  private void updateSortMenuItem(MenuItem sortItem) {
    if (sortDescending) {
      // Descending: Newest first (down arrow - newer dates at top)
      sortItem.setTitle("Sort: Newest First");
      sortItem.setIcon(R.drawable.ic_sort_descending);
    } else {
      // Ascending: Oldest first (up arrow - older dates at top)
      sortItem.setTitle("Sort: Oldest First");
      sortItem.setIcon(R.drawable.ic_sort_ascending);
    }
  }

  interface Callbacks {
    void onEntrySelected(UUID id, boolean edit, String group);
  }

  private class EntryViewHolder extends RecyclerView.ViewHolder {
    private JournalEntry mEntry;
    private final TextView mTxtTitle;
    private final TextView mDate;
    private final TextView mGroup;
    private final TextView mAmount;

    public EntryViewHolder(@NonNull View itemView) {
      super(itemView);

      mTxtTitle = itemView.findViewById(R.id.txt_item_title);
      mDate = itemView.findViewById(R.id.txt_item_date);
      mGroup = itemView.findViewById(R.id.txt_item_group);
      mAmount = itemView.findViewById(R.id.txt_item_amount);

      itemView.setOnClickListener(this::launchJournalEntryFragment);
    }

    private void launchJournalEntryFragment(View v) {
      mCallbacks.onEntrySelected(mEntry.getUid(), true, group);
    }

      void bind(JournalEntry entry) {
        mEntry = entry;
        this.mTxtTitle.setText(mEntry.getText());
        // Display only date (not timestamp) to user
        this.mDate.setText(mEntry.getFormattedDate());
        this.mGroup.setText(mEntry.getGroup());
        this.mAmount.setText(String.format(Locale.US, "%.2f", mEntry.getAmount()));
      }
  }

  private class EntryListAdapter extends RecyclerView.Adapter<EntryViewHolder> {
    private final LayoutInflater mInflater;
    private List<JournalEntry> mEntries;

    public EntryListAdapter(Context context) {
      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView = mInflater.inflate(R.layout.fragment_entry, parent, false);
      return new EntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
      if (mEntries != null) {
        JournalEntry current = mEntries.get(position);
        holder.bind(current);
      }
    }

    @Override
    public int getItemCount() {
      return (mEntries == null) ? 0 : mEntries.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setEntries(List<JournalEntry> entries) {
      // Create a mutable copy of the list from LiveData so we can sort it
      mEntries = new ArrayList<>(entries);
      sortEntries();
      calculateTotal();
      notifyDataSetChanged();
    }
    
    /**
     * Re-sorts the current list based on the sort order and refreshes the view.
     * Called when user toggles sort order.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshSorting() {
      if (mEntries != null && !mEntries.isEmpty()) {
        sortEntries();
        notifyDataSetChanged();
      }
    }
    
    /**
     * Sorts entries based on the current sort order.
     * Descending (default): Newest entries first
     * Ascending: Oldest entries first
     */
    private void sortEntries() {
      if (mEntries == null || mEntries.isEmpty()) {
        return;
      }
      
      if (sortDescending) {
        // Sort descending (newest first) - reverse the normal order
        Collections.sort(mEntries, Collections.reverseOrder(new EntryComparator()));
      } else {
        // Sort ascending (oldest first) - normal order
        Collections.sort(mEntries, new EntryComparator());
      }
    }
    
    /**
     * Calculates and displays the total amount for all entries.
     */
    private void calculateTotal() {
      grp_total = 0;
      if (mEntries != null) {
        for (JournalEntry entry : mEntries) {
          grp_total += entry.getAmount();
        }
      }
      tv.setText(String.format(Locale.US, "%.2f", grp_total));
    }
  }

}
