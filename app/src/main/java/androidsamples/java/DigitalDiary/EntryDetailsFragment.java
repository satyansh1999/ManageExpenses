package androidsamples.java.DigitalDiary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EntryDetailsFragment # newInstance} factory method to
 * create an instance of this fragment.
 */

public class EntryDetailsFragment extends Fragment {
  private static final String TAG = "EntryDetailsFragment";
  private EditText mEditTitle, mAmount;
  private Button mEditDate;
  private EntryDetailsViewModel mEntryDetailsViewModel;
  private JournalEntry mEntry;
  private boolean edit;
  private String group;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    mEntryDetailsViewModel = new ViewModelProvider(requireActivity()).get(EntryDetailsViewModel.class);

    UUID entryId = EntryDetailsFragmentArgs.fromBundle(getArguments()).getEntryId();
    edit = EntryDetailsFragmentArgs.fromBundle(getArguments()).getEdit();
    group = EntryDetailsFragmentArgs.fromBundle(getArguments()).getGroup();
    Log.d(TAG, "Loading entry: " + entryId);

    if(edit) mEntryDetailsViewModel.loadEntry(entryId);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_entry_details, container, false);
    mEditTitle = view.findViewById(R.id.edit_title);
    mEditDate = view.findViewById(R.id.btn_entry_date);
    mAmount = view.findViewById(R.id.edit_amount);
    view.findViewById(R.id.btn_save).setOnClickListener(this::saveEntry);

    mEditDate.setOnClickListener(v -> {
      EntryListFragment.navController.navigate(R.id.datePickerAction);
    });

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if(edit) {
      mEntryDetailsViewModel.getEntryLiveData().observe(requireActivity(),
              entry -> {
                this.mEntry = entry;
                if (entry != null) updateUI();
              });
    }
    else{
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
      mEditDate.setText(simpleDateFormat.format(cal.getTime()));
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.fragment_entry_detail, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_delete_entry) {
      Log.d(TAG, "Delete button clicked");
      if(edit) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Delete Journal Entry");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
          mEntryDetailsViewModel.deleteEntry(mEntry);
          Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
          requireActivity().onBackPressed();
        });
        alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
        alert.show();
      }
      if(!edit)
        requireActivity().onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateUI() {
    mEditTitle.setText(mEntry.getText());
    mEditDate.setText(mEntry.getDate());
    double amt = mEntry.getAmount();
    if(amt > 0) mAmount.setText(String.valueOf(amt));
  }

  private void saveEntry(View v) {
    Log.d(TAG, "Save button clicked");
    if(edit) {
      String str = mAmount.getText().toString();
      double amt = 0.0;
      if (str.length() > 0) amt = Double.parseDouble(str);
      if (amt == 0.0) mEntryDetailsViewModel.deleteEntry(mEntry);
      else {
        mEntry.setText(mEditTitle.getText().toString());
        mEntry.setDate(mEditDate.getText().toString());
        mEntry.setAmount(amt);
        mEntryDetailsViewModel.saveEntry(mEntry);
        Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
      }
    }
    else {
      String str = mAmount.getText().toString();
      double amt = 0.0;
      if (str.length() > 0) amt = Double.parseDouble(str);
      if (amt > 0.0) {
        mEntry = new JournalEntry();
        mEntry.setGroup(group);
        mEntry.setText(mEditTitle.getText().toString());
        mEntry.setDate(mEditDate.getText().toString());
        mEntry.setAmount(amt);
        mEntryDetailsViewModel.insertEntry(mEntry);
        Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
      }
    }
    requireActivity().onBackPressed();
  }
}

