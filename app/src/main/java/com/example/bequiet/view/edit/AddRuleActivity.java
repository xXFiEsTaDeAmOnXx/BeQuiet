package com.example.bequiet.view.edit;

import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bequiet.R;
import com.example.bequiet.databinding.ActivityAddRuleBinding;
import com.example.bequiet.model.database.Database;
import com.example.bequiet.model.dataclasses.AreaRule;
import com.example.bequiet.model.dataclasses.WlanRule;
import com.example.bequiet.view.GPSCoordinateSelectedListener;
import com.example.bequiet.view.fragments.SelectAreaFragment;
import com.example.bequiet.view.fragments.SelectWifiFragment;

import org.osmdroid.config.Configuration;

import java.util.Calendar;

public class AddRuleActivity extends AppCompatActivity implements GPSCoordinateSelectedListener, SelectWifiFragment.WifiSelectedListener {

    private String ruleName = "";

    private int startHour = -1;
    private int endHour = -1;
    private int startMinute = -1;
    private int endMinute = -1;

    private String wifissid = "";

    private double lat = -1000;
    private double lon = -1000;

    private float radius = 40;

    private int zoom = 20;

    private int state = 0; //0 = GPS, 1 = WIFI

    private Button btnSaveRule;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.bequiet.databinding.ActivityAddRuleBinding binding = ActivityAddRuleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Spinner typesSpinner = (Spinner) findViewById(R.id.spinnerRuleTypes);
        String[] types = getResources().getStringArray(R.array.ruletypes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typesSpinner.setAdapter(adapter);

        EditText editTextRulename = findViewById(R.id.editTextRulename);
        editTextRulename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ruleName = s.toString();
                changeButtonState();
            }
        });

        EditText editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextStartDate.setShowSoftInputOnFocus(false);
        editTextStartDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddRuleActivity.this, (view, hourOfDay, minute1) -> {
                    Resources res = getResources();
                    String text = String.format(res.getString(R.string.time_string), getLeadingZeroString(hourOfDay), getLeadingZeroString(minute1));
                    editTextStartDate.setText(text);
                    startHour = hourOfDay;
                    startMinute = minute1;
                    editTextStartDate.clearFocus();
                    changeButtonState();
                }, hour, minute, true);

                timePickerDialog.setOnCancelListener(dialog -> editTextStartDate.clearFocus());

                timePickerDialog.show();
            }
        });

        EditText editTextEndDate = findViewById(R.id.editTextEndDate);
        editTextEndDate.setShowSoftInputOnFocus(false);
        editTextEndDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddRuleActivity.this, (view, hourOfDay, minute12) -> {
                    Resources res = getResources();
                    String text = String.format(res.getString(R.string.time_string), getLeadingZeroString(hourOfDay), getLeadingZeroString(minute12));
                    editTextEndDate.setText(text);
                    endHour = hourOfDay;
                    endMinute = minute12;
                    editTextEndDate.clearFocus();
                    changeButtonState();
                }, hour, minute, true);
                timePickerDialog.setOnCancelListener(dialog -> editTextEndDate.clearFocus());
                timePickerDialog.show();
            }
        });
        typesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        state = 0;
                        SelectAreaFragment selectAreaFragment = new SelectAreaFragment();
                        selectAreaFragment.setGpsCoordinateSelectedListener(AddRuleActivity.this);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainerView, selectAreaFragment)
                                .commit();
                        break;
                    case 1:
                        state = 1;
                        SelectWifiFragment selectWifiFragment = new SelectWifiFragment();
                        selectWifiFragment.setWifiSelectedListener(AddRuleActivity.this);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainerView, selectWifiFragment)
                                .commit();
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Configuration.getInstance().

                load(AddRuleActivity.this, PreferenceManager.getDefaultSharedPreferences(AddRuleActivity.this));

        this.btnSaveRule = findViewById(R.id.btnSaveRule);
        this.btnSaveRule.setEnabled(false);

        SelectAreaFragment selectAreaFragment = new SelectAreaFragment();
        selectAreaFragment.setGpsCoordinateSelectedListener(AddRuleActivity.this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, selectAreaFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


    public void onClick(View view) {
        Database db = new Database(view.getContext());
        if (this.state == 0) {
            db.addRuleIntoDb(new AreaRule(this.ruleName, this.startHour, this.startMinute, this.endHour, this.endMinute, radius, lat, lon, zoom));
        } else {
            db.addRuleIntoDb(new WlanRule(this.ruleName, this.startHour, this.startMinute, this.endHour, this.endMinute, this.wifissid));
        }
        finish();
    }


    @Override
    public void onWifiSelected(String ssid) {
        this.wifissid = ssid;
        changeButtonState();
    }

    @Override
    public void onGPSCoordinateSelected(double lat, double lon, float radius, int zoom) {
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.zoom = zoom;
        changeButtonState();
    }

    private void changeButtonState() {
        btnSaveRule.setEnabled(insertedAllValues());
    }

    private boolean insertedAllValues() {
        if (ruleName.equals("")) return false;
        if (startHour == -1) return false;
        if (startMinute == -1) return false;
        if (endHour == -1) return false;
        if (endMinute == -1) return false;

        if (state == 0) {
            if (lat == -1000) return false;
            return lon != -1000;
        } else {
            return !wifissid.equals("");
        }
    }
    private String getLeadingZeroString(int time) {
        if (time < 10) {
            return "0" + time;
        } else
            return String.valueOf(time);
    }

}