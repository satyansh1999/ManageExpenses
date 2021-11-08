package androidsamples.java.DigitalDiary;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.UUID;

/**
 * A fragment representing a list of Items.
 */
public class EntryListFragment extends Fragment {
  private static final String TAG = "EntryListFragment";
  private EntryListViewModel mEntryListViewModel;
  private Callbacks mCallbacks = null;
  private String group;
  private double grp_total = 0;
  private TextView tv;

  @SuppressLint("StaticFieldLeak")
  public static NavController navController;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    mEntryListViewModel = new ViewModelProvider(this).get(EntryListViewModel.class);
    group = EntryListFragmentArgs.fromBundle(getArguments()).getGroup();
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
    EntryListAdapter adapter = new EntryListAdapter(getActivity());
    entriesList.setAdapter(adapter);

    mEntryListViewModel.getAllEntriesOfGroup(group).observe(requireActivity(), adapter::setEntries);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
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
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_delete_group) {
      Log.d(TAG, "Group Delete button clicked");

      AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
      alert.setTitle("Delete Folder");
      alert.setMessage("Are you sure you want to delete " + group + "?");
      alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
        mEntryListViewModel.deleteGroup(group);
        Toast.makeText(getContext(), group + " deleted", Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();
      });
      alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
      alert.show();
    }
    return super.onOptionsItemSelected(item);
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
      this.mDate.setText(mEntry.getDate());
      this.mGroup.setText(mEntry.getGroup());
      this.mAmount.setText(String.valueOf(mEntry.getAmount()));
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
      mEntries = entries;
      grp_total = 0;
      for(int i = 0 ; i < entries.size() ; i++ ){
        grp_total += entries.get(i).getAmount();
      }
      tv.setText(String.valueOf(grp_total));
      notifyDataSetChanged();
    }
  }

}
