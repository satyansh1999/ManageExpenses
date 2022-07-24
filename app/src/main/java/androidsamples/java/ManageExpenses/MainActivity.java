package androidsamples.java.ManageExpenses;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavDirections;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements EntryGroupsFragment.GroupCallbacks, EntryListFragment.Callbacks {
  private static final String TAG = "MainActivity";
  private static final int STORAGE_REQUEST_CODE = 1;

  @RequiresApi(api = Build.VERSION_CODES.Q)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    setContentView(R.layout.activity_main);
    Log.d(TAG, "Entered MainActivity");
  }

  @Override
  public void onEntrySelected(UUID entryId, boolean edit, String group) {
    Log.d(TAG, "Entry selected: " + entryId);
    NavDirections action = EntryListFragmentDirections.addEntryAction(entryId, edit, group);
    EntryListFragment.navController.navigate(action);
  }

  @Override
  public void onGroupSelected(String grp) {
    Log.d(TAG, "Group selected: " + grp);
    NavDirections action = EntryGroupsFragmentDirections.groupSelectedAction(grp);
    EntryGroupsFragment.navController.navigate(action);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == STORAGE_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
              grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
      }
      else {
        Toast.makeText(this, "Storage Permission Required...", Toast.LENGTH_SHORT).show();
      }
    }
  }
}