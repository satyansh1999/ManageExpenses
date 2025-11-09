package androidsamples.java.ManageExpenses;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements EntryGroupsFragment.GroupCallbacks, EntryListFragment.Callbacks {
  private static final String TAG = "MainActivity";
  private NavController navController;
  private AppBarConfiguration appBarConfiguration;

  @RequiresApi(api = Build.VERSION_CODES.Q)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    setContentView(R.layout.activity_main);
    
    // Set up Navigation with ActionBar
    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
        .findFragmentById(R.id.nav_host_fragment);
    if (navHostFragment != null) {
      navController = navHostFragment.getNavController();
      appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
      NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }
    
    Log.d(TAG, "Entered MainActivity");
  }
  
  @Override
  public boolean onSupportNavigateUp() {
    if (navController != null && appBarConfiguration != null) {
      return NavigationUI.navigateUp(navController, appBarConfiguration)
          || super.onSupportNavigateUp();
    }
    return super.onSupportNavigateUp();
  }

  @Override
  public void onEntrySelected(UUID entryId, boolean edit, String group) {
    Log.d(TAG, "Entry selected: " + entryId);
    if (navController != null) {
      NavDirections action = EntryListFragmentDirections.addEntryAction(entryId, edit, group);
      navController.navigate(action);
    }
  }

  @Override
  public void onGroupSelected(String grp) {
    Log.d(TAG, "Group selected: " + grp);
    if (navController != null) {
      NavDirections action = EntryGroupsFragmentDirections.groupSelectedAction(grp);
      navController.navigate(action);
    }
  }
}