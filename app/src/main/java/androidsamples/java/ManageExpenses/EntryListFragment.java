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
import androidx.recyclerview.widget.ListAdapter;
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
    adapter = new EntryListAdapter();
    entriesList.setAdapter(adapter);

    // Observe entries and handle sorting + total calculation
    mAppViewModel.getAllEntriesOfGroup(group).observe(getViewLifecycleOwner(), entries -> {
      // Create mutable copy and sort
      List<JournalEntry> sortedList = new ArrayList<>(entries);
      if (sortDescending) {
        Collections.sort(sortedList, Collections.reverseOrder(new EntryComparator()));
      } else {
        Collections.sort(sortedList, new EntryComparator());
      }
      
      // Submit sorted list to adapter (DiffUtil handles the diff)
      adapter.submitList(sortedList);
      
      // Calculate and display total
      grp_total = 0;
      for (JournalEntry entry : sortedList) {
        grp_total += entry.getAmount();
      }
      tv.setText(String.format(Locale.US, "%.2f", grp_total));
    });
    
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
      
      // Trigger re-sort by getting current list and re-submitting
      List<JournalEntry> currentList = adapter.getCurrentList();
      if (currentList != null && !currentList.isEmpty()) {
        List<JournalEntry> sortedList = new ArrayList<>(currentList);
        if (sortDescending) {
          Collections.sort(sortedList, Collections.reverseOrder(new EntryComparator()));
        } else {
          Collections.sort(sortedList, new EntryComparator());
        }
        adapter.submitList(sortedList);
      }
      
      String message = sortDescending ? "Sorted by newest first" : "Sorted by oldest first";
      Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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

  /**
   * RecyclerView adapter using ListAdapter with DiffUtil for efficient updates.
   * Automatically handles animations for add/edit/delete operations.
   */
  private class EntryListAdapter extends ListAdapter<JournalEntry, EntryViewHolder> {

    public EntryListAdapter() {
      super(new EntryDiffCallback());
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.fragment_entry, parent, false);
      return new EntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
      JournalEntry current = getItem(position);
      holder.bind(current);
    }
    
    // getItemCount() is automatically provided by ListAdapter
    // No need to override it
  }

}
