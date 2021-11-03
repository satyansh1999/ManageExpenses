package androidsamples.java.journalapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements EntryGroupsFragment.GroupCallbacks, EntryListFragment.Callbacks {
  static final String KEY_ENTRY_ID = "KEY_ENTRY_ID";
  private static final String TAG = "MainActivity";

  @RequiresApi(api = Build.VERSION_CODES.Q)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    setContentView(R.layout.activity_main);
    Log.d(TAG, "Entered MainActivity");
  }

  @Override
  public void onEntrySelected(UUID entryId) {
    Log.d(TAG, "Entry selected: " + entryId);
    NavDirections action = EntryListFragmentDirections.addEntryAction(entryId);
    EntryListFragment.navController.navigate(action);
  }

  @Override
  public void onGroupAdded() {
    // Log.d(TAG, "Group Added: " + id);
    NavDirections action = EntryGroupsFragmentDirections.groupAddedAction(false, "");
    EntryGroupsFragment.navController.navigate(action);
  }

  @Override
  public void onGroupSelected(String grp) {
    Log.d(TAG, "Group selected: " + grp);
    NavDirections action = EntryGroupsFragmentDirections.groupSelectedAction(grp);
    EntryGroupsFragment.navController.navigate(action);
  }
}