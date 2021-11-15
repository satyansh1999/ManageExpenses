package androidsamples.java.DigitalDiary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class EntryGroupsFragment extends Fragment{
    private static final String TAG = "EntryGroupsFragment";
    private AppViewModel mAppViewModel;
    private GroupCallbacks mCallbacks = null;
    @SuppressLint("StaticFieldLeak")
    public static NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAppViewModel = new ViewModelProvider(this).get(AppViewModel.class);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
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
            mGroups = entries;
            notifyDataSetChanged();
        }
    }

}
