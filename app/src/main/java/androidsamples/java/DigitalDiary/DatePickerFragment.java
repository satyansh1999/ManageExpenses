package androidsamples.java.DigitalDiary;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment {
  private Calendar calendar;
  private Button mEditDate;

  @NonNull
  public static DatePickerFragment newInstance(Date date) {
    // TODO implement the method
    return null;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    calendar = Calendar.getInstance();
    mEditDate = requireActivity().findViewById(R.id.btn_entry_date);
    // TODO implement the method
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // TODO implement the method
    return new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
      calendar.set(Calendar.YEAR, y);
      calendar.set(Calendar.MONTH, m);
      calendar.set(Calendar.DAY_OF_MONTH, d);

      @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
      mEditDate.setText(simpleDateFormat.format(calendar.getTime()));
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
  }
}
