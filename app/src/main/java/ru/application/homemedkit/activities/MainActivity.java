package ru.application.homemedkit.activities;

import static ru.application.homemedkit.helpers.ConstantsHelper.NEW_INTAKE;
import static ru.application.homemedkit.helpers.ConstantsHelper.NEW_MEDICINE;
import static ru.application.homemedkit.helpers.ConstantsHelper.SETTINGS_CHANGED;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationBarView;

import ru.application.homemedkit.R;
import ru.application.homemedkit.databinding.ActivityMainBinding;
import ru.application.homemedkit.fragments.FragmentHome;
import ru.application.homemedkit.fragments.FragmentIntakes;
import ru.application.homemedkit.fragments.FragmentMedicines;
import ru.application.homemedkit.fragments.FragmentSettings;
import ru.application.homemedkit.helpers.SettingsHelper;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new SettingsHelper(this).getAppTheme();

        binding.bottomNavigationBarView.setOnItemSelectedListener(pickMenuItem());

        getFragmentPage();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        recreate();
        getIntent().putExtra(SETTINGS_CHANGED, true);
    }

    private void getFragmentPage() {
        if (getIntent().getBooleanExtra(NEW_MEDICINE, false)) {
            binding.bottomNavigationBarView.setSelectedItemId(R.id.bottom_menu_medicines);
        } else if (getIntent().getBooleanExtra(NEW_INTAKE, false)) {
            binding.bottomNavigationBarView.setSelectedItemId(R.id.bottom_menu_intakes);
        } else if (getIntent().getBooleanExtra(SETTINGS_CHANGED, false)) {
            binding.bottomNavigationBarView.setSelectedItemId(R.id.bottom_menu_settings);
        } else {
            toHomePage();
        }
    }

    private NavigationBarView.OnItemSelectedListener pickMenuItem() {
        return item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_menu_main) replace(new FragmentHome());
            else if (itemId == R.id.bottom_menu_medicines) replace(new FragmentMedicines());
            else if (itemId == R.id.bottom_menu_intakes) replace(new FragmentIntakes());
            else if (itemId == R.id.bottom_menu_settings) replace(new FragmentSettings());

            return true;
        };
    }

    private void toHomePage() {
        String[] pages = getResources().getStringArray(R.array.fragment_pages);
        String homePage = new SettingsHelper(this).getHomePage();

        if (homePage.equals(pages[1]))
            binding.bottomNavigationBarView.setSelectedItemId(R.id.bottom_menu_medicines);
        else if (homePage.equals(pages[2]))
            binding.bottomNavigationBarView.setSelectedItemId(R.id.bottom_menu_intakes);
        else if (homePage.equals(pages[3]))
            binding.bottomNavigationBarView.setSelectedItemId(R.id.bottom_menu_settings);
        else binding.bottomNavigationBarView.setSelectedItemId(R.id.bottom_menu_main);
    }

    private void replace(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.constraintLayout, fragment).commit();
    }
}