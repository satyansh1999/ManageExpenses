package androidsamples.java.ManageExpenses;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;

public class EntryGroupsFragment extends Fragment{
    private static final String TAG = "EntryGroupsFragment";
    private AppViewModel mAppViewModel;
    private GroupCallbacks mCallbacks = null;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private static final int STORAGE_REQUEST_CODE = 1;
    private String[] storagePermissions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAppViewModel = new ViewModelProvider(this).get(AppViewModel.class);
        storagePermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if( result.getResultCode() == Activity.RESULT_OK ){
                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                                if(Environment.isExternalStorageManager()){
                                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(requireContext(), "Storage Permission Required...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry_groups, container, false);

        FloatingActionButton fab = view.findViewById(R.id.btn_add_entry_groups);
        fab.setOnClickListener(this::addNewEntry);

        RecyclerView entriesList = view.findViewById(R.id.recyclerViewGroups);
        entriesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        EntryListAdapter adapter = new EntryListAdapter(getActivity());
        entriesList.setAdapter(adapter);

        mAppViewModel.getAllGroups().observe(requireActivity(), adapter::setEntries);
        return view;
    }

    public void addNewEntry(View view) {
        //mCallbacks.onGroupAdded();
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        final View vi = getLayoutInflater().inflate(R.layout.fragment_group_details,null);
        EditText titleG = vi.findViewById(R.id.edit_title_group);

        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            String str = titleG.getText().toString().trim();
            if(str.length() > 0) {
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.group_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.backup) {
            Log.d(TAG, "Export Data button clicked");
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Export data");
            alert.setMessage("Are you sure you want to export data? The data will be saved in expenses.csv file in ManageExpenses folder.");
            alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                if(checkStoragePermission()) {
                    exportCSV();
                }
                else {
                    requestStoragePermission();
                }
            });
            alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
            alert.show();
        }
        else if (item.getItemId() == R.id.restore) {
            Log.d(TAG, "Import Data button clicked");
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Export data");
            alert.setMessage("Are you sure you want to import data from expenses.csv file in ManageExpenses folder? You will not loose your current data.");
            alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                if(checkStoragePermission()) {
                    importCSV();
                }
                else {
                    requestStoragePermission();
                }
            });
            alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
            alert.show();
        }
        else if (item.getItemId() == R.id.menu_delete_all) {
            deleteAll();
        }
        return super.onOptionsItemSelected(item);
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

    private void importCSV() {
        File file = new File(Environment.getExternalStorageDirectory() + "/ManageExpenses/expenses.csv");
        if(!file.exists()) {
            Toast.makeText(getContext(), "CSV file not found. Rename your file to expenses.csv and place it in ManageExpenses folder.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            CSVReader reader = new CSVReader(new FileReader(file.getAbsolutePath()));
            String[] nextLine;
            while( (nextLine = reader.readNext()) != null ) {
                Log.d(TAG, nextLine[0] + "," + nextLine[1]  + "," + nextLine[2]  + "," + nextLine[3]);
                JournalEntry entry = new JournalEntry();
                entry.setGroup(nextLine[0]);
                entry.setDate(nextLine[1] + "," + nextLine[2] + "," + nextLine[3]);
                entry.setAmount(Double.parseDouble(nextLine[4]));
                StringBuilder text = new StringBuilder(nextLine[5]);
                for(int i=6; i < nextLine.length;i++){
                    text.append(",");
                    text.append(nextLine[i]);
                }
                entry.setText(text.toString());
                mAppViewModel.insert(entry);
            }
            Toast.makeText(getContext(), "Data imported", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportCSV() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/ManageExpenses");
        Log.d(TAG, "CSV filepath : " + folder.getAbsolutePath());
        boolean isFolderCreated = false;
        if(!folder.exists()) {
            isFolderCreated = folder.mkdirs();
            if(!isFolderCreated) {
                Toast.makeText(getContext(), "Unable to create storage folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String filePath = folder.toString() + File.separator +  "expenses.csv";

        LiveData<List<JournalEntry>> data = mAppViewModel.getAllEntries();
        data.observe(requireActivity(), new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(List<JournalEntry> journalEntries) {
                try {
                    File csv = new File(filePath);
                    csv.createNewFile();
                    FileWriter fw = new FileWriter(filePath);
                    for (JournalEntry entry: journalEntries) {
                        fw.append(entry.getGroup() + ",");
                        fw.append(entry.getDate() + ",");
                        fw.append(entry.getAmount() + ",");
                        fw.append(entry.getText());
                        fw.append("\n");
                    }
                    fw.flush();
                    fw.close();
                    Toast.makeText(getContext(), "Data exported", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    Toast.makeText(getContext(), e.getClass() + " - " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                data.removeObserver(this);
            }
        });
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
                    if(str.length() > 0) {
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

    private class EntryListAdapter extends RecyclerView.Adapter<EntryViewHolder> {
        private final LayoutInflater mInflater;
        private List<String> mGroups;

        public EntryListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = mInflater.inflate(R.layout.fragment_group, parent, false);
            return new EntryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
            if (mGroups != null) {
                String current = mGroups.get(position);
                holder.bind(current);
            }
        }

        @Override
        public int getItemCount() {
            return (mGroups == null) ? 0 : mGroups.size();
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setEntries(List<String> entries) {
            Collections.reverse(entries);
            mGroups = entries;
            notifyDataSetChanged();
        }
    }

    private boolean checkStoragePermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }
        else{
            boolean readCheck = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean writeCheck = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            return readCheck && writeCheck;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.categoryDEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", requireContext().getApplicationContext().getPackageName())));
                activityResultLauncher.launch(intent);
            }
            catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activityResultLauncher.launch(intent);
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), storagePermissions, STORAGE_REQUEST_CODE);
        }
    }
}
