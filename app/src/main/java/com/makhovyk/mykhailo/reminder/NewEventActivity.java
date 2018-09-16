package com.makhovyk.mykhailo.reminder;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.notifications.AlarmHelper;
import com.makhovyk.mykhailo.reminder.utils.Constants;
import com.makhovyk.mykhailo.reminder.utils.CustomDatePickerDialog;
import com.makhovyk.mykhailo.reminder.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;


public class NewEventActivity extends AppCompatActivity {

    private static final int SELECT_CONTACT = 200;

    final String TAG = "TAG";
    private final String[] types = {Constants.TYPE_BIRTHDAY, Constants.TYPE_ANNIVERSARY,
            Constants.TYPE_OTHER_EVENT};

    @BindView(R.id.sp_type)
    Spinner spType;
    @BindView(R.id.et_event_name)
    EditText etEventName;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_phone)
    EditText etPhone;
    @BindView(R.id.et_date)
    EditText etDate;
    @BindView(R.id.ctv_hide_year)
    CheckedTextView ctvHideYear;
    @BindView(R.id.bt_ok)
    Button btOk;
    @BindView(R.id.bt_contact)
    Button btContact;
    @BindView(R.id.il_event_name)
    TextInputLayout ilEventName;
    @BindView(R.id.il_name)
    TextInputLayout ilName;
    @BindView(R.id.il_phone)
    TextInputLayout ilPhone;
    @BindView(R.id.il_date)
    TextInputLayout ilDate;
    @BindView(R.id.ll_event_name)
    LinearLayout llEventName;

    final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Utils.setupNightMode(this);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
            Log.v("TAG", "Dark");
        } else {
            setTheme(R.style.LightTheme);
            Log.v("TAG", "Light");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final Drawable errorBg = ContextCompat.getDrawable(this, R.drawable.et_border);
        final Drawable defaultBg = etName.getBackground();

        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateDateField();
            }
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewEventActivity.this,
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(adapter);
        ctvHideYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ctvHideYear.isChecked()) {
                    ctvHideYear.setChecked(false);
                } else {
                    ctvHideYear.setChecked(true);
                }
            }
        });

        etName.addTextChangedListener(new MyTextWatcher(etName));
        etEventName.addTextChangedListener(new MyTextWatcher(etEventName));
        etDate.addTextChangedListener(new MyTextWatcher(etDate));

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomDatePickerDialog datePickerDialog = new CustomDatePickerDialog(NewEventActivity.this, onDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH), ctvHideYear.isChecked());
                datePickerDialog.show();
                if (ctvHideYear.isChecked()) {
//                    datePickerDialog.getDatePicker().findViewById(getResources()
//                            .getIdentifier("year","id","android")).setVisibility(View.GONE);

//                    DatePicker dp = findDatePicker((ViewGroup) datePickerDialog.getWindow().getDecorView());
//                    if (dp != null) {
//                        ((ViewGroup) dp.getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
//                    }

                    if (Build.VERSION.SDK_INT < 21) {
                        datePickerDialog.getDatePicker().setCalendarViewShown(false);
                        ((ViewGroup) datePickerDialog.getDatePicker()).findViewById(Resources.getSystem()
                                .getIdentifier("year", "id", "android"))
                                .setVisibility(View.GONE);
                    } else {
                        DatePicker dp = findDatePicker((ViewGroup) datePickerDialog.getWindow().getDecorView());
                        if (dp != null) {
                            ((ViewGroup) dp.getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
                        }
                    }

                }
            }
        });

        // btOk.setEnabled(false);
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    saveEvent();
                    Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                    startActivity(intent);
                }
            }
        });
/*
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().equals("")) {
                    etName.setBackground(errorBg);
                } else {
                    etName.setBackground(defaultBg);
                }
                checkFields();
            }
        });
        etEventName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().equals("")) {
                    etEventName.setBackground(errorBg);
                } else {
                    etEventName.setBackground(defaultBg);
                }
                checkFields();
            }
        });
        etDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().equals("")) {
                    etDate.setBackground(errorBg);
                } else {
                    etDate.setBackground(defaultBg);
                }
                checkFields();
            }
        });
*/
    }

    private void updateDateField() {
        String format = "MM.dd.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    @OnItemSelected(R.id.sp_type)
    public void spinnerItemSelected(Spinner spinner, View selectedItemView, int position) {
        switch (types[position]) {
            case Constants.TYPE_BIRTHDAY:
                llEventName.setVisibility(View.GONE);
                break;
            case Constants.TYPE_ANNIVERSARY:
                llEventName.setVisibility(View.GONE);
                break;
            case Constants.TYPE_OTHER_EVENT:
                llEventName.setVisibility(View.VISIBLE);
                etEventName.requestFocus();
                break;
        }
    }

    private void saveEvent() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy", Locale.getDefault());

        Event event = new Event();
        event.setYearUnknown(ctvHideYear.isChecked());
        event.setPersonName(etName.getText().toString());
        event.setType(spType.getSelectedItem().toString());
        event.setPhone(etPhone.getText().toString());
        event.setTimestamp();
        try {
            event.setDate(sdf.parse(etDate.getText().toString()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (spType.getSelectedItem().toString().equals(Constants.TYPE_OTHER_EVENT)) {
            event.setEventName(etEventName.getText().toString());
        }
        Log.v(TAG, event.toString());
        Log.d("hello", "new event");
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(this);
        dbHelper.writeEvent(event);
        new AlarmHelper(getApplicationContext()).setAlarm(event, false);


    }

//    private void checkFields() {
//        if (spType.getSelectedItem().toString().equals(Constants.TYPE_OTHER_EVENT)) {
//            if (etName.getText().toString().trim().equals("")
//                    || etEventName.getText().toString().trim().equals("")
//                    || etDate.getText().toString().trim().equals("")) {
//                btOk.setEnabled(false);
//            } else {
//                btOk.setEnabled(true);
//            }
//        } else {
//            if (etName.getText().toString().trim().equals("")
//                    || etDate.getText().toString().trim().equals("")) {
//                btOk.setEnabled(false);
//            } else {
//                btOk.setEnabled(true);
//            }
//        }
//    }

    private DatePicker findDatePicker(ViewGroup group) {
        if (group != null) {
            for (int i = 0, j = group.getChildCount(); i < j; i++) {
                View child = group.getChildAt(i);
                if (child instanceof DatePicker) {
                    return (DatePicker) child;
                } else if (child instanceof ViewGroup) {
                    DatePicker result = findDatePicker((ViewGroup) child);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;

    }

    @OnClick(R.id.bt_contact)
    public void readContact() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, SELECT_CONTACT);
        //checkPermissions();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_CONTACT && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = this.getContentResolver().query(contactUri, projection,
                    null, null, null);

            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String number = cursor.getString(numberIndex);
                String name = cursor.getString(nameIndex);
                // Do something with the phone number
                etName.setText(name);
                etPhone.setText(number);


            }

            cursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validate() {
        if (!validateEventName()) {
            Log.d("hello", "event name validated");
            return false;
        }
        if (!validateName()) {
            Log.d("hello", " name validated");
            return false;
        }
        if (!validateDate()) {
            Log.d("hello", "date validated");
            return false;
        }
        return true;
    }

    private boolean validateName() {
        if (etName.getText().toString().trim().isEmpty()) {
            ilName.setError(getString(R.string.error_name));
            requestFocus(etName);
            return false;
        } else {
            ilName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEventName() {
        if (etEventName.getText().toString().trim().isEmpty()
                && (llEventName.getVisibility() == View.VISIBLE)) {
            ilEventName.setError(getString(R.string.error_event_name));
            requestFocus(etEventName);
            return false;
        } else {
            ilEventName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateDate() {
        if (etDate.getText().toString().trim().isEmpty()) {
            ilDate.setError(getString(R.string.error_date));
            requestFocus(etDate);
            return false;
        } else {
            ilDate.setErrorEnabled(false);
        }
        return true;
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_name:
                    validateName();
                    break;
                case R.id.et_event_name:
                    validateEventName();
                    break;
                case R.id.et_date:
                    validateDate();
                    break;
            }
        }
    }
}
