package androidsamples.java.ManageExpenses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntryGroupsFragment extends Fragment{
    private static final String TAG = "EntryGroupsFragment";
    private AppViewModel mAppViewModel;
    private GroupCallbacks mCallbacks = null;
    
    // Storage Access Framework launchers
    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        
        // Register export launcher
        exportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        performExport(uri);
                    }
                }
            }
        );
        
        // Register import launcher
        importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        performImport(uri);
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry_groups, container, false);

        FloatingActionButton fab = view.findViewById(R.id.btn_add_entry_groups);
        fab.setOnClickListener(this::addNewEntry);

        RecyclerView entriesList = view.findViewById(R.id.recyclerViewGroups);
        entriesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        EntryListAdapter adapter = new EntryListAdapter();
        entriesList.setAdapter(adapter);

        // Observe groups (placeholder entries) and submit to adapter sorted by date
        mAppViewModel.getAllGroups().observe(getViewLifecycleOwner(), placeholderEntries -> {
            // Remove duplicates (keep only one placeholder per group - the newest one)
            // This handles cases where there might be multiple placeholders for same group
            List<JournalEntry> uniquePlaceholders = new ArrayList<>();
            java.util.Set<String> seenGroups = new java.util.HashSet<>();
            
            // Sort placeholders by date descending (newest first) using EntryComparator
            List<JournalEntry> sortedPlaceholders = new ArrayList<>(placeholderEntries);
            sortedPlaceholders.sort(Collections.reverseOrder(new EntryComparator()));
            
            // Keep only first occurrence of each group (which is the newest due to sorting)
            for (JournalEntry entry : sortedPlaceholders) {
                if (!seenGroups.contains(entry.getGroup())) {
                    uniquePlaceholders.add(entry);
                    seenGroups.add(entry.getGroup());
                }
            }
            
            // Extract group names in date-sorted order
            List<String> sortedGroupNames = new ArrayList<>();
            for (JournalEntry entry : uniquePlaceholders) {
                sortedGroupNames.add(entry.getGroup());
            }
            
            adapter.submitList(sortedGroupNames);
        });
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Setup menu using the new MenuProvider API (replaces deprecated setHasOptionsMenu)
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.group_list_fragment, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.backup) {
                    Log.d(TAG, "Export Data button clicked");
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Export data");
                    alert.setMessage("Choose a location to save your expenses data as CSV.");
                    alert.setPositiveButton(android.R.string.yes, (dialog, which) -> launchExportFilePicker());
                    alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
                    alert.show();
                    return true;
                }
                else if (menuItem.getItemId() == R.id.restore) {
                    Log.d(TAG, "Import Data button clicked");
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Import data");
                    alert.setMessage("Select a CSV file to import. Your current data will be preserved.");
                    alert.setPositiveButton(android.R.string.yes, (dialog, which) -> launchImportFilePicker());
                    alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
                    alert.show();
                    return true;
                }
                else if (menuItem.getItemId() == R.id.menu_delete_all) {
                    deleteAll();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    public void addNewEntry(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        final View vi = getLayoutInflater().inflate(R.layout.fragment_group_details,null);
        EditText titleG = vi.findViewById(R.id.edit_title_group);

        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            String str = titleG.getText().toString().trim();
            if(!str.isEmpty()) {
                JournalEntry mEntry = new JournalEntry();
                mEntry.setText("_");
                mEntry.setGroup(str);
                mAppViewModel.insert(mEntry);
                Toast.makeText(getContext(), str + " created", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(getContext(), "Folder must have a name", Toast.LENGTH_SHORT).show();
        });
        alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
        alert.setView(vi);
        alert.create().show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (GroupCallbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void deleteAll() {
        Log.d(TAG, "Delete Data button clicked");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Delete all data");
        alert.setMessage("Are you sure you want to delete all the data that you currently have?");
        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mAppViewModel.deleteAll();
            Toast.makeText(getContext(), "Data deleted", Toast.LENGTH_SHORT).show();
        });
        alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    /**
     * Launch file picker to select export location using Storage Access Framework.
     * No permissions required - works on all Android versions.
     */
    private void launchExportFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        
        // Suggest filename with timestamp (Locale.US for consistent filename format)
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        intent.putExtra(Intent.EXTRA_TITLE, "expenses_" + timestamp + ".csv");
        
        exportLauncher.launch(intent);
    }

    /**
     * Launch file picker to select CSV file to import using Storage Access Framework.
     * No permissions required - works on all Android versions.
     * <p>
     * Note: Uses universal MIME type to allow selecting any file since some file 
     * managers don't properly recognize CSV MIME types. The actual validation 
     * happens during import.
     */
    private void launchImportFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        // Use "*/*" to show all files - some file managers don't recognize "text/csv"
        // This allows users to select CSV files regardless of how they were created
        intent.setType("*/*");
        
        // Optionally suggest CSV and text files (not all file managers support this)
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "text/plain", "text/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        
        importLauncher.launch(intent);
    }

    /**
     * Export all entries to a CSV file at the specified URI.
     * Uses Storage Access Framework for modern, permission-less file access.
     * 
     * @param uri URI where the file should be saved (from file picker)
     */
    private void performExport(Uri uri) {
        LiveData<List<JournalEntry>> data = mAppViewModel.getAllEntries();
        data.observe(getViewLifecycleOwner(), new Observer<>() {
            @Override
            public void onChanged(List<JournalEntry> journalEntries) {
                try {
                    OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
                    if (os == null) {
                        Toast.makeText(getContext(), "Failed to open file for writing", 
                            Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    OutputStreamWriter writer = new OutputStreamWriter(os);
                    
                    // Write CSV header
                    writer.write("Group,Date,Amount,Description\n");
                    
                    // Write entries (including placeholder/group header entries)
                    int exportCount = 0;
                    for (JournalEntry entry : journalEntries) {
                        // Escape group field for CSV (handle commas, quotes, newlines)
                        String group = entry.getGroup();
                        if (group.contains(",") || group.contains("\"") || group.contains("\n")) {
                            group = "\"" + group.replace("\"", "\"\"") + "\"";
                        }
                        
                        // Escape date field for CSV - dates contain commas (e.g., "Mon, Jan 01, 2024")
                        String date = entry.getDate();
                        if (date.contains(",") || date.contains("\"") || date.contains("\n")) {
                            date = "\"" + date.replace("\"", "\"\"") + "\"";
                        }
                        
                        // Escape description field for CSV (handle commas, quotes, newlines)
                        String text = entry.getText().replace("\"", "\"\"");
                        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
                            text = "\"" + text + "\"";
                        }
                        
                        // Write row with proper formatting (Locale.US for consistent CSV format)
                        writer.write(String.format(Locale.US, "%s,%s,%.2f,%s\n",
                            group,
                            date,
                            entry.getAmount(),
                            text
                        ));
                        exportCount++;
                    }
                    
                    writer.flush();
                    writer.close();
                    
                    Toast.makeText(getContext(), 
                        "Exported " + exportCount + " entries successfully", 
                        Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Export successful: " + exportCount + " entries");
                    
                } catch (Exception e) {
                    Log.e(TAG, "Export error", e);
                    Toast.makeText(getContext(), 
                        "Export failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
                data.removeObserver(this);
            }
        });
    }

    /**
     * Converts date string to timestamp format if it's in old format (date only).
     * Old format: "EEE, MMM dd, yyyy" → New format: "EEE, MMM dd, yyyy 00:00:00"
     * If date already has timestamp, returns it unchanged.
     * 
     * @param date Date string to convert
     * @return Date string in timestamp format
     */
    private String convertDateToTimestampFormat(String date) {
        if (date == null || date.isEmpty()) {
            return date;
        }
        
        // Check if date already has timestamp (contains time separator ":")
        if (date.contains(":")) {
            // Already in timestamp format
            return date;
        }
        
        // Old format without timestamp - add default midnight time
        // Example: "Sun, Nov 09, 2025" → "Sun, Nov 09, 2025 00:00:00"
        return date + " 00:00:00";
    }

    /**
     * Import entries from a CSV file at the specified URI.
     * Uses Storage Access Framework for modern, permission-less file access.
     * Handles both old format (date only) and new format (date with timestamp).
     * 
     * @param uri URI of the CSV file to import (from file picker)
     */
    private void performImport(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is == null) {
                Toast.makeText(getContext(), "Failed to open file", Toast.LENGTH_SHORT).show();
                return;
            }
            
            CSVReader reader = new CSVReader(new InputStreamReader(is));
            String[] nextLine;
            int importCount = 0;
            int skipCount = 0;
            boolean isFirstLine = true;
            
            while ((nextLine = reader.readNext()) != null) {
                // Skip header row if present
                if (isFirstLine) {
                    isFirstLine = false;
                    if (nextLine.length > 0 && "Group".equalsIgnoreCase(nextLine[0])) {
                        continue;
                    }
                }
                
                // Debug: Log the row being processed
                Log.d(TAG, "Processing row with " + nextLine.length + " fields");
                
                // Skip incomplete rows
                if (nextLine.length < 4) {
                    skipCount++;
                    Log.w(TAG, "Skipping incomplete row with " + nextLine.length + " fields. Row: " + 
                        java.util.Arrays.toString(nextLine));
                    continue;
                }
                
                try {
                    // Parse and validate entry
                    String group = nextLine[0].trim();
                    String date = nextLine[1].trim();
                    String amountStr = nextLine[2].trim();
                    
                    // Handle description - may span multiple fields if commas weren't escaped
                    StringBuilder descBuilder = new StringBuilder(nextLine[3].trim());
                    for (int i = 4; i < nextLine.length; i++) {
                        descBuilder.append(",").append(nextLine[i].trim());
                    }
                    String description = descBuilder.toString();
                    
                    double amount = Double.parseDouble(amountStr);
                    
                    // Validate required fields
                    if (group.isEmpty() || date.isEmpty()) {
                        skipCount++;
                        Log.w(TAG, "Skipping row with empty required fields. Group: '" + group + 
                            "', Date: '" + date + "'");
                        continue;
                    }
                    
                    // Convert old date format to new format with timestamp if needed
                    date = convertDateToTimestampFormat(date);
                    
                    // Check if this is a placeholder/group header entry
                    boolean isPlaceholder = "_".equals(description) && amount == 0.0;
                    
                    if (isPlaceholder) {
                        // This is a group header entry - use it to establish group sort order
                        // Create placeholder entry to maintain group ordering by date
                        JournalEntry entry = new JournalEntry();
                        entry.setGroup(group);
                        entry.setDate(date);
                        entry.setAmount(0.0);
                        entry.setText("_");
                        
                        mAppViewModel.insert(entry);
                        importCount++;
                        Log.d(TAG, "Imported group header: " + group + " / " + date);
                    } else {
                        // Regular expense entry - validate it has description and positive amount
                        if (description.isEmpty()) {
                            skipCount++;
                            Log.w(TAG, "Skipping entry with empty description in group: " + group);
                            continue;
                        }
                        
                        if (amount <= 0) {
                            skipCount++;
                            Log.w(TAG, "Skipping entry with invalid amount: " + amount + " in group: " + group);
                            continue;
                        }
                        
                        // Create and insert regular entry
                        JournalEntry entry = new JournalEntry();
                        entry.setGroup(group);
                        entry.setDate(date);
                        entry.setAmount(amount);
                        entry.setText(description);
                        
                        mAppViewModel.insert(entry);
                        importCount++;
                        Log.d(TAG, "Successfully imported: " + group + " / " + date + " / " + amount);
                    }
                    
                } catch (NumberFormatException e) {
                    skipCount++;
                    Log.w(TAG, "Skipping row with invalid number format. Row: " + 
                        java.util.Arrays.toString(nextLine), e);
                }
            }
            
            reader.close();
            
            String message = "Imported " + importCount + " entries";
            if (skipCount > 0) {
                message += " (skipped " + skipCount + " invalid rows)";
            }
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Import complete: " + importCount + " entries, " + skipCount + " skipped");
            
        } catch (Exception e) {
            Log.e(TAG, "Import error", e);
            Toast.makeText(getContext(), 
                "Import failed: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }

    interface GroupCallbacks {
        void onGroupSelected(String grp);
    }

    private class EntryViewHolder extends RecyclerView.ViewHolder {
        private String Group;
        private final TextView mTxtTitle;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);

            mTxtTitle = itemView.findViewById(R.id.txt_item_title_group);
            itemView.setOnClickListener(this::launchJournalEntryFragment);

            itemView.findViewById(R.id.edit_group).setOnClickListener( v ->{
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                final View view = getLayoutInflater().inflate(R.layout.fragment_group_details,null);
                EditText mEditTitle = view.findViewById(R.id.edit_title_group);
                mEditTitle.setText(Group);

                alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    String str = mEditTitle.getText().toString().trim();
                    if(!str.isEmpty()) {
                        mAppViewModel.updateGroup(Group, str);
                        Toast.makeText(getContext(), "Folder " + Group + " changed to " + str, Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(getContext(), "Folder must have a name", Toast.LENGTH_SHORT).show();
                });
                alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
                alert.setView(view);
                alert.create().show();
            });
        }

        private void launchJournalEntryFragment(View v) {
            mCallbacks.onGroupSelected(Group);
        }

        void bind(String grp) {
            Group = grp;
            this.mTxtTitle.setText(Group);
        }
    }

    /**
     * RecyclerView adapter using ListAdapter with DiffUtil for efficient updates.
     * Automatically handles animations for add/edit/delete operations.
     */
    private class EntryListAdapter extends ListAdapter<String, EntryViewHolder> {

        public EntryListAdapter() {
            super(new GroupDiffCallback());
        }

        @NonNull
        @Override
        public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group, parent, false);
            return new EntryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
            String current = getItem(position);
            holder.bind(current);
        }
        
        // getItemCount() is automatically provided by ListAdapter
        // No need to override it
    }
}
