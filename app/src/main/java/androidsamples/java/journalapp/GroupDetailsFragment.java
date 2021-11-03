package androidsamples.java.journalapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EntryDetailsFragment # newInstance} factory method to
 * create an instance of this fragment.
 */

public class GroupDetailsFragment extends Fragment {
    private static final String TAG = "GroupDetailsFragment";
    private EditText mEditTitle;
    private GroupDetailsViewModel mGroupDetailsViewModel;
    private JournalEntry mEntry;
    private boolean edit;
    private String grp_old;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mGroupDetailsViewModel = new ViewModelProvider(requireActivity()).get(GroupDetailsViewModel.class);
        edit = GroupDetailsFragmentArgs.fromBundle(getArguments()).getEdit();
        grp_old = GroupDetailsFragmentArgs.fromBundle(getArguments()).getGrpOld();
        if(!edit) {
            UUID entryId = GroupDetailsFragmentArgs.fromBundle(getArguments()).getId();
            mGroupDetailsViewModel.loadEntry(entryId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_details, container, false);
        mEditTitle = view.findViewById(R.id.edit_title_group);
        view.findViewById(R.id.btn_save_group).setOnClickListener(this::saveEntry);
        return view;
    }

  @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!edit) {
            mGroupDetailsViewModel.getEntryLiveData().observe(requireActivity(),
                    entry -> {
                        this.mEntry = entry;
                        if (entry != null) updateUI();
                    });
        }
        else mEditTitle.setText(grp_old);
    }

    private void updateUI() {
        mEditTitle.setText(mEntry.getGroup());
    }

    private void saveEntry(View v) {
        String str = mEditTitle.getText().toString();
        if(!edit) {
            Log.d(TAG, "Save button clicked");
            if (mEntry == null) Log.d(TAG, "mEntry is null");
            else {
                if(str.length() > 0) {
                    mEntry.setGroup(str);
                    mGroupDetailsViewModel.saveEntry(mEntry);
                    Toast.makeText(getContext(), str + " created", Toast.LENGTH_SHORT).show();
                }
                else {
                    mGroupDetailsViewModel.deleteEntry(mEntry);
                    Toast.makeText(getContext(), "Folder must have a name", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            Log.d(TAG, "Edit button clicked");
            if(str.length() > 0) {
                mGroupDetailsViewModel.updateGroup(grp_old, str);
                Toast.makeText(getContext(), "Folder " + grp_old + " changed to " + str, Toast.LENGTH_SHORT).show();
            }
            else                  Toast.makeText(getContext(), "Folder must have a name", Toast.LENGTH_SHORT).show();
        }
        requireActivity().onBackPressed();
    }
}

