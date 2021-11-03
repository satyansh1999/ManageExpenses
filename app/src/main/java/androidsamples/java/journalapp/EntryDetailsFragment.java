package androidsamples.java.journalapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
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

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    mEntryDetailsViewModel = new ViewModelProvider(requireActivity()).get(EntryDetailsViewModel.class);

    UUID entryId = EntryDetailsFragmentArgs.fromBundle(getArguments()).getEntryId();
    Log.d(TAG, "Loading entry: " + entryId);

    mEntryDetailsViewModel.loadEntry(entryId);
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
    mEntryDetailsViewModel.getEntryLiveData().observe(requireActivity(),
            entry -> {
              this.mEntry = entry;
              if (entry != null) updateUI();
            });
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
      mEntryDetailsViewModel.deleteEntry(mEntry);
      Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
      requireActivity().onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }


  private void updateUI() {
    mEditTitle.setText(mEntry.getText());
    mEditDate.setText(mEntry.getDate());
    mAmount.setText(String.valueOf(mEntry.getAmount()));
  }

  private void saveEntry(View v) {
    Log.d(TAG, "Save button clicked");
    mEntry.setText(mEditTitle.getText().toString());
    mEntry.setDate(mEditDate.getText().toString());
    mEntry.setAmount(Double.parseDouble(mAmount.getText().toString()));
    mEntryDetailsViewModel.saveEntry(mEntry);
    Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
    requireActivity().onBackPressed();
  }
}

