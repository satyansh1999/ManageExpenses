package androidsamples.java.ManageExpenses;

import android.app.Application;

public class JournalApplication extends Application {
  @java.lang.Override
  public void onCreate() {
    super.onCreate();
    JournalRepository.init(this);
  }
}
