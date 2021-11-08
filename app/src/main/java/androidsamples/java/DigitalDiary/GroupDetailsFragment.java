package androidsamples.java.DigitalDiary;

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
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_details, container, false);
        mEditTitle = view.findViewById(R.id.edit_title_group);
        if(edit) mEditTitle.setText(grp_old);
        view.findViewById(R.id.btn_save_group).setOnClickListener(this::saveEntry);
        return view;
    }

    private void saveEntry(View v) {
        String str = mEditTitle.getText().toString().trim();
        if(!edit) {
            Log.d(TAG, "Save button clicked");
            if(str.length() > 0) {
                mEntry = new JournalEntry();
                mEntry.setText("_");
                mEntry.setGroup(str);
                mGroupDetailsViewModel.saveEntry(mEntry);
                Toast.makeText(getContext(), str + " created", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getContext(), "Folder must have a name", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.d(TAG, "Edit button clicked");
            if(str.length() > 0) {
                mGroupDetailsViewModel.updateGroup(grp_old, str);
                Toast.makeText(getContext(), "Folder " + grp_old + " changed to " + str, Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(getContext(), "Folder must have a name", Toast.LENGTH_SHORT).show();
        }
        requireActivity().onBackPressed();
    }
}

