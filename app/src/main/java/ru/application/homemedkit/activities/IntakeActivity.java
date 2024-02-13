package ru.application.homemedkit.activities;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static java.lang.String.valueOf;
import static ru.application.homemedkit.helpers.ConstantsHelper.ADD;
import static ru.application.homemedkit.helpers.ConstantsHelper.INTAKE_ID;
import static ru.application.homemedkit.helpers.ConstantsHelper.MEDICINE_ID;
import static ru.application.homemedkit.helpers.ConstantsHelper.NEW_INTAKE;
import static ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON;
import static ru.application.homemedkit.helpers.StringHelperKt.decimalFormat;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ru.application.homemedkit.R;
import ru.application.homemedkit.databaseController.Intake;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.helpers.SettingsHelper;
import ru.application.homemedkit.pickers.ClockPicker;
import ru.application.homemedkit.pickers.CustomRangePicker;
import ru.application.homemedkit.pickers.CustomTimePicker;
import ru.application.homemedkit.pickers.EditClick;
import ru.application.homemedkit.pickers.IntervalPicker;
import ru.application.homemedkit.pickers.PeriodPicker;
import ru.application.homemedkit.pickers.SaveClick;

public class IntakeActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    public static String intervalType, periodType;
    public static boolean edit = false;
    private MedicineDatabase database;
    private MaterialToolbar toolbar;
    private FlexboxLayout timesGroup, datesLayout;
    private TextInputLayout periodLayout;
    private TextInputEditText productName, amount, startDate, finalDate;
    private MaterialAutoCompleteTextView interval, period;
    private FloatingActionButton buttonSave, buttonEdit;
    private long intakeId, medicineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intake);

        toolbar = findViewById(R.id.scanned_medicine_top_app_bar_toolbar);
        timesGroup = findViewById(R.id.linear_times_activity_intake);
        periodLayout = findViewById(R.id.intake_input_layout_period);
        datesLayout = findViewById(R.id.intake_layout_dates);
        productName = findViewById(R.id.intake_edit_text_product_name);
        amount = findViewById(R.id.intake_edit_text_amount);
        interval = findViewById(R.id.intake_edit_text_interval);
        period = findViewById(R.id.intake_edit_text_period);
        startDate = findViewById(R.id.intake_calendar_start);
        finalDate = findViewById(R.id.intake_calendar_finish);
        buttonSave = findViewById(R.id.intake_button_save);
        buttonEdit = findViewById(R.id.intake_button_edit);

        database = MedicineDatabase.getInstance(this);

        intakeId = getIntent().getLongExtra(INTAKE_ID, 0);
        medicineId = getIntent().getLongExtra(MEDICINE_ID, 0);
        boolean flag = getIntent().getBooleanExtra(ADD, false);


        if (flag) {
            setAddingLayout();
            toolbar.setVisibility(GONE);
        } else {
            setShowLayout();
        }
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(v -> backClick());

        buttonEdit.setOnClickListener(new EditClick(this));
        buttonSave.setOnClickListener(new SaveClick(this, intakeId, medicineId));

        getOnBackPressedDispatcher().addCallback(new BackClick());

        createNotificationChannel(this);
        checkPostNotificationPermission(this);
    }

    private void setAddingLayout() {
        boolean isLight = new SettingsHelper(this).getLightPeriod();

        String name = database.medicineDAO().getProductName(medicineId);

        productName.setText(name);

        interval.setOnItemClickListener(new IntervalPicker(this));
        period.setOnItemClickListener(new PeriodPicker(this));

        if (!isLight) {
            startDate.setOnClickListener(new CustomRangePicker(this));
            finalDate.setOnClickListener(new CustomRangePicker(this));
        }

        buttonEdit.setVisibility(INVISIBLE);
        buttonSave.setVisibility(VISIBLE);
    }

    private void setShowLayout() {
        String[] intervals = getResources().getStringArray(R.array.interval_types);
        String[] periods = getResources().getStringArray(R.array.period_types);

        Intake intake = database.intakeDAO().getByPK(intakeId);
        String name = database.medicineDAO().getProductName(intake.medicineId);

        productName.setText(name);

        amount.setText(decimalFormat(intake.amount));
        amount.setRawInputType(InputType.TYPE_NULL);
        amount.setFocusableInTouchMode(false);

        intervalType = intake.interval;
        if (intervalType.equals(intervals[0])) getAutoTextItem(interval, 0);
        else if (intervalType.equals(intervals[1])) getAutoTextItem(interval, 1);
        else if (intervalType.equals(intervals[2])) getAutoTextItem(interval, 2);
        else getAutoTextItem(interval, 3);

        periodType = intake.period;
        if (periodType.equals(periods[0])) getAutoTextItem(period, 0);
        else if (periodType.equals(periods[1])) getAutoTextItem(period, 1);
        else if (periodType.equals(periods[2])) getAutoTextItem(period, 2);
        else getAutoTextItem(period, 3);

        timesGroup.removeAllViews();
        for (String time : intake.time.split(SEMICOLON)) {
            timesGroup.addView(new CustomTimePicker(this, time));
        }

        periodLayout.setVisibility(VISIBLE);
        datesLayout.setVisibility(periodType.equals(periods[3]) ? GONE : VISIBLE);

        startDate.setText(intake.startDate);
        finalDate.setText(intake.finalDate);

        interval.setInputType(InputType.TYPE_NULL);
        period.setInputType(InputType.TYPE_NULL);

        buttonEdit.setVisibility(VISIBLE);
        buttonSave.setVisibility(INVISIBLE);
    }

    private void createNotificationChannel(Context context) {
        CharSequence name = context.getString(R.string.notification_channel_name);
        String description = context.getString(R.string.notification_channel_description);

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_name), name, importance);
        channel.setDescription(description);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private void checkPostNotificationPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
            }
        }
    }

    private void getAutoTextItem(AutoCompleteTextView view, int i) {
        view.setText(valueOf(view.getAdapter().getItem(i)), false);
    }

    public void setEditLayout() {
        boolean isLight = new SettingsHelper(this).getLightPeriod();

        amount.setRawInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        amount.setFocusableInTouchMode(true);
        amount.setCursorVisible(true);
        amount.setFocusable(true);

        for (int i = 0; i < timesGroup.getChildCount(); i++) {
            timesGroup.getChildAt(i).setOnClickListener(new ClockPicker(this,
                    (Chip) timesGroup.getChildAt(i)));
        }

        period.setSimpleItems(R.array.period_types_name);
        interval.setSimpleItems(R.array.interval_types_name);

        interval.setOnItemClickListener(new IntervalPicker(this));
        period.setOnItemClickListener(new PeriodPicker(this));

        if (!isLight) {
            startDate.setOnClickListener(new CustomRangePicker(this));
            finalDate.setOnClickListener(new CustomRangePicker(this));
        }
    }

    private void backClick() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(NEW_INTAKE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        PopupMenu popupMenu = new PopupMenu(this, toolbar.findViewById(R.id.top_app_bar_more));
        popupMenu.inflate(R.menu.menu_top_app_bar_more);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            database.intakeDAO().delete(new Intake(intakeId));
            backClick();
            return true;
        });
        popupMenu.show();

        return true;
    }

    private class BackClick extends OnBackPressedCallback {
        public BackClick() {
            super(true);
        }

        @Override
        public void handleOnBackPressed() {
            backClick();
        }
    }
}