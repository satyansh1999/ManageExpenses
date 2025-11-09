package androidsamples.java.ManageExpenses;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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
  private AppViewModel mAppViewModel;
  private JournalEntry mEntry;
  private boolean edit;
  private String group;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    mAppViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

    UUID entryId = EntryDetailsFragmentArgs.fromBundle(getArguments()).getEntryId();
    edit = EntryDetailsFragmentArgs.fromBundle(getArguments()).getEdit();
    group = EntryDetailsFragmentArgs.fromBundle(getArguments()).getGroup();
    Log.d(TAG, "Loading entry: " + entryId);

    if(edit) mAppViewModel.loadEntry(entryId);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_entry_details, container, false);
    mEditTitle = view.findViewById(R.id.edit_title);
    mEditDate = view.findViewById(R.id.btn_entry_date);
    mAmount = view.findViewById(R.id.edit_amount);
    
    // Set input type for amount field to accept decimal numbers
    mAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    
    // Add text watcher to clear error when user starts typing
    mAmount.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
      
      @Override
      public void afterTextChanged(Editable s) {
        mAmount.setError(null);
      }
    });
    
    mEditTitle.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
      
      @Override
      public void afterTextChanged(Editable s) {
        mEditTitle.setError(null);
      }
    });
    
    view.findViewById(R.id.btn_save).setOnClickListener(this::saveEntry);

    mEditDate.setOnClickListener(v -> {
      Navigation.findNavController(v).navigate(R.id.datePickerAction);
    });

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if(edit) {
      mAppViewModel.getEntryLiveData().observe(requireActivity(),
              entry -> {
                this.mEntry = entry;
                if (entry != null) updateUI();
              });
    }
    else{
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
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
          mAppViewModel.delete(mEntry);
          Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
          requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
        alert.show();
      }
      if(!edit)
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateUI() {
    mEditTitle.setText(mEntry.getText());
    mEditDate.setText(mEntry.getDate());
    double amt = mEntry.getAmount();
    if(amt > 0) mAmount.setText(String.valueOf(amt));
  }

  /**
   * Safely parse amount string to double, handling invalid input.
   * @param amountStr The string to parse
   * @return Parsed amount or null if invalid
   */
  private Double parseAmount(String amountStr) {
    if (amountStr == null || amountStr.trim().isEmpty()) {
      return null;
    }
    
    try {
      // Clean the input - remove currency symbols and commas
      String cleaned = amountStr.trim()
          .replace("$", "")
          .replace("€", "")
          .replace("£", "")
          .replace(",", "");
          
      double amount = Double.parseDouble(cleaned);
      
      // Validate reasonable range
      if (amount < 0) {
        return null; // Negative amounts not allowed
      }
      if (amount > 999999999) {
        return null; // Unreasonably large
      }
      
      // Round to 2 decimal places
      return Math.round(amount * 100.0) / 100.0;
      
    } catch (NumberFormatException e) {
      Log.w(TAG, "Invalid amount format: " + amountStr, e);
      return null;
    }
  }

  /**
   * Validate all input fields before saving.
   * @return true if all inputs are valid, false otherwise
   */
  private boolean validateInput() {
    boolean isValid = true;
    
    // Validate title/description
    String title = mEditTitle.getText().toString().trim();
    if (title.isEmpty()) {
      mEditTitle.setError("Description is required");
      isValid = false;
    } else if (title.length() > 200) {
      mEditTitle.setError("Description too long (max 200 characters)");
      isValid = false;
    }
    
    // Validate amount
    String amountStr = mAmount.getText().toString().trim();
    Double amount = parseAmount(amountStr);
    
    if (amount == null) {
      if (amountStr.isEmpty()) {
        mAmount.setError("Amount is required");
      } else {
        mAmount.setError("Invalid amount format");
      }
      isValid = false;
    } else if (amount <= 0) {
      mAmount.setError("Amount must be greater than zero");
      isValid = false;
    }
    
    return isValid;
  }

  private void saveEntry(View v) {
    Log.d(TAG, "Save button clicked");
    
    // Validate input first
    if (!validateInput()) {
      return; // Don't save if validation fails
    }
    
    String title = mEditTitle.getText().toString().trim();
    String dateStr = mEditDate.getText().toString();
    Double amount = parseAmount(mAmount.getText().toString());
    
    // Should never be null here due to validation, but defensive check
    if (amount == null) {
      Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
      return;
    }
    
    if (edit) {
      // Update existing entry
      if (mEntry != null) {
        mEntry.setText(title);
        mEntry.setDate(dateStr);
        mEntry.setAmount(amount);
        mAppViewModel.update(mEntry);
        Toast.makeText(getContext(), "Item updated", Toast.LENGTH_SHORT).show();
      }
    } else {
      // Create new entry
      mEntry = new JournalEntry();
      mEntry.setGroup(group);
      mEntry.setText(title);
      mEntry.setDate(dateStr);
      mEntry.setAmount(amount);
      mAppViewModel.insert(mEntry);
      Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
    }
    
    requireActivity().getOnBackPressedDispatcher().onBackPressed();
  }
}

