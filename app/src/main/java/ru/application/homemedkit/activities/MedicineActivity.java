package ru.application.homemedkit.activities;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static java.lang.String.valueOf;
import static ru.application.homemedkit.helpers.ConstantsHelper.ACCEPTED_KEYS;
import static ru.application.homemedkit.helpers.ConstantsHelper.ADD;
import static ru.application.homemedkit.helpers.ConstantsHelper.ADDING;
import static ru.application.homemedkit.helpers.ConstantsHelper.BLANK;
import static ru.application.homemedkit.helpers.ConstantsHelper.CIS;
import static ru.application.homemedkit.helpers.ConstantsHelper.DUPLICATE;
import static ru.application.homemedkit.helpers.ConstantsHelper.ID;
import static ru.application.homemedkit.helpers.ConstantsHelper.MEDICINE_ID;
import static ru.application.homemedkit.helpers.ConstantsHelper.NEW_MEDICINE;
import static ru.application.homemedkit.helpers.DateHelper.toExpDate;
import static ru.application.homemedkit.helpers.DateHelper.toTimestamp;
import static ru.application.homemedkit.helpers.ImageHelper.setImage;
import static ru.application.homemedkit.helpers.StringHelper.decimalFormat;
import static ru.application.homemedkit.helpers.StringHelper.fromHTML;
import static ru.application.homemedkit.helpers.StringHelper.parseAmount;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toolbar.OnMenuItemClickListener;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

import ru.application.homemedkit.R;
import ru.application.homemedkit.connectionController.RequestAPI;
import ru.application.homemedkit.databaseController.Medicine;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.databaseController.Technical;
import ru.application.homemedkit.dialogs.ExpDateDialog;
import ru.application.homemedkit.graphics.ExpandAnimation;
import ru.application.homemedkit.graphics.Snackbars;

public class MedicineActivity extends AppCompatActivity implements TextWatcher, OnMenuItemClickListener {

    private MedicineDatabase database;
    private Medicine medicine;
    private MaterialToolbar toolbar;
    private ImageView image;
    private TextInputEditText productName, expDate, prodFormNormName, prodDNormName, prodNormAmount, phKinetics, comment;
    private FloatingActionButton buttonFetch, buttonAddIntake;
    private MaterialButton buttonSave;
    private String cis;
    private long primaryKey;
    private boolean duplicate, scanned, verified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);

        toolbar = findViewById(R.id.scanned_medicine_top_app_bar_toolbar);
        image = findViewById(R.id.image_medicine_scanned_type);
        productName = findViewById(R.id.medicine_scanned_product_name);
        expDate = findViewById(R.id.medicine_scanned_exp_date);
        prodFormNormName = findViewById(R.id.medicine_scanned_prod_form_norm_name);
        prodDNormName = findViewById(R.id.medicine_scanned_prod_d_norm_name);
        prodNormAmount = findViewById(R.id.medicine_scanned_amount);
        phKinetics = findViewById(R.id.medicine_scanned_ph_kinetics);
        comment = findViewById(R.id.medicine_scanned_comment);
        buttonSave = findViewById(R.id.medicine_card_save_changes);
        buttonFetch = findViewById(R.id.button_fetch_data);
        buttonAddIntake = findViewById(R.id.button_add_intake);

        database = MedicineDatabase.getInstance(this);
        primaryKey = getIntent().getLongExtra(ID, 0);
        duplicate = getIntent().getBooleanExtra(DUPLICATE, false);
        cis = getIntent().getStringExtra(CIS);

        if (primaryKey == 0) {
            setAddingLayout();
        } else {
            medicine = database.medicineDAO().getByPK(primaryKey);
            scanned = medicine.technical.scanned;
            verified = medicine.technical.verified;
            if (scanned && verified) setScannedLayout();
            else setAddedLayout();
        }

        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        toolbar.setNavigationOnClickListener(v -> backClick());
        buttonAddIntake.setOnClickListener(new AddIntake());
        getOnBackPressedDispatcher().addCallback(new BackButtonPressed());
    }

    private void setAddingLayout() {
        setFieldsClickable(true);

        findViewById(R.id.top_app_bar_more).setVisibility(GONE);
        buttonAddIntake.setVisibility(View.INVISIBLE);
        expDate.setOnClickListener(v -> new ExpDateDialog(this));
        buttonSave.setOnClickListener(new AddMedicine());
    }

    private void setScannedLayout() {
        String form = medicine.prodFormNormName;

        image.setImageDrawable(setImage(this, form));
        productName.setText(medicine.productName);
        expDate.setText(getString(R.string.exp_date_until, toExpDate(medicine.expDate)));
        prodFormNormName.setText(form);
        prodDNormName.setText(medicine.prodDNormName);
        prodNormAmount.setText(decimalFormat(medicine.prodAmount));
        phKinetics.setText(fromHTML(medicine.phKinetics));
        comment.setText(medicine.comment);

        setFieldsClickable(false);

        buttonSave.setOnClickListener(new SaveChanges());

        TextInputLayout layout = findViewById(R.id.medicine_scanned_layout_ph_kinetics);
        layout.setClickable(true);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        layout.setEndIconOnClickListener(new ExpandAnimation(this, layout));

        duplicateCheck();
    }

    private void setAddedLayout() {
        productName.setText(medicine.productName);
        expDate.setText(medicine.expDate != -1 ? getString(R.string.exp_date_until, toExpDate(medicine.expDate)) : BLANK);
        prodFormNormName.setText(medicine.prodFormNormName);
        prodDNormName.setText(medicine.prodDNormName);
        prodNormAmount.setText(decimalFormat(medicine.prodAmount));
        phKinetics.setText(medicine.phKinetics);
        comment.setText(medicine.comment);

        setFieldsClickable(true);

        expDate.setOnClickListener(v -> new ExpDateDialog(this));

        buttonFetch.setVisibility(medicine.cis != null ? VISIBLE : View.INVISIBLE);
        buttonFetch.setOnClickListener(v -> new RequestAPI(this)
                .onDecoded(primaryKey, medicine.cis));

        buttonSave.setOnClickListener(new SaveChanges());

        duplicateCheck();
    }

    private void backClick() {
        Intent intent = new Intent(this, MainActivity.class);
        if (!getIntent().getBooleanExtra(ADDING, false))
            intent.putExtra(NEW_MEDICINE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void duplicateCheck() {
        if (duplicate) new Snackbars(this).duplicateMedicine();
    }

    private void setFieldsClickable(boolean flag) {
        Arrays.asList(productName, prodFormNormName, prodDNormName, phKinetics)
                .forEach(item -> {
                    if (flag) {
                        item.addTextChangedListener(this);
                        item.setRawInputType(InputType.TYPE_CLASS_TEXT);
                    } else {
                        item.addTextChangedListener(null);
                        item.setRawInputType(InputType.TYPE_NULL);
                    }
                    item.setFocusableInTouchMode(flag);
                    item.setFocusable(flag);
                    item.setCursorVisible(flag);
                });
        Arrays.asList(prodNormAmount, comment).forEach(item -> {
            item.addTextChangedListener(this);
            if (item != prodNormAmount)
                item.setRawInputType(InputType.TYPE_CLASS_TEXT);
            item.setFocusableInTouchMode(true);
            item.setFocusable(true);
            item.setCursorVisible(true);
        });
        expDate.addTextChangedListener(flag ? this : null);
        comment.setKeyListener(DigitsKeyListener.getInstance(ACCEPTED_KEYS));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        buttonSave.setVisibility(VISIBLE);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        PopupMenu popupMenu = new PopupMenu(this, toolbar.findViewById(R.id.top_app_bar_more));
        popupMenu.inflate(R.menu.menu_top_app_bar_more);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            database.medicineDAO().delete(new Medicine(primaryKey));
            backClick();
            return true;
        });
        popupMenu.show();

        return true;
    }

    private class BackButtonPressed extends OnBackPressedCallback {
        public BackButtonPressed() {
            super(true);
        }

        @Override
        public void handleOnBackPressed() {
            backClick();
        }
    }

    private class SaveChanges implements OnClickListener {
        @Override
        public void onClick(View v) {
            String cis = medicine.cis;
            String name = valueOf(productName.getText());
            long date = toTimestamp(valueOf(expDate.getText()));
            String formNormName = valueOf(prodFormNormName.getText());
            String normName = valueOf(prodDNormName.getText());
            double prodAmount = parseAmount(valueOf(prodNormAmount.getText()));
            String kinetics = valueOf(phKinetics.getText());
            String textComment = valueOf(comment.getText());

            database.medicineDAO().update(
                    new Medicine(primaryKey, cis, name, date, formNormName, normName, prodAmount, kinetics, textComment,
                            new Technical(scanned, verified)));

            buttonSave.setVisibility(GONE);

            finish();
            startActivity(new Intent(getIntent()));
        }
    }

    private class AddMedicine implements OnClickListener {
        @Override
        public void onClick(View v) {
            String name = valueOf(productName.getText());
            long date = toTimestamp(valueOf(expDate.getText()));
            String formNormName = valueOf(prodFormNormName.getText());
            String normName = valueOf(prodDNormName.getText());
            double prodAmount = parseAmount(valueOf(prodNormAmount.getText()));
            String kinetics = valueOf(phKinetics.getText());
            String textComment = valueOf(comment.getText());
            boolean scanned = cis != null;
            boolean verified = false;

            long id = database.medicineDAO().add(
                    new Medicine(cis, name, date, formNormName, normName, prodAmount, kinetics, textComment,
                            new Technical(scanned, verified)));

            buttonSave.setVisibility(GONE);

            finish();

            Intent intent = new Intent(getIntent());
            intent.putExtra(ID, id);
            startActivity(intent);
        }
    }

    private class AddIntake implements OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MedicineActivity.this, IntakeActivity.class);
            intent.putExtra(MEDICINE_ID, primaryKey);
            intent.putExtra(ADD, true);
            startActivity(intent);
        }
    }
}