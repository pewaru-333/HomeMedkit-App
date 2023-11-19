package ru.application.homemedkit.fragments;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static ru.application.homemedkit.helpers.ConstantsHelper.SETTINGS_CHANGED;
import static ru.application.homemedkit.helpers.SettingsHelper.APP_THEME;
import static ru.application.homemedkit.helpers.SettingsHelper.LANGUAGE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MainActivity;
import ru.application.homemedkit.helpers.SettingsHelper;

public class FragmentSettings extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

    private SettingsHelper preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new SettingsHelper(requireContext());
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        if (key != null) {
            ListPreference preference = findPreference(key);
            final String value = Objects.requireNonNull(preference).getValue();

            switch (key) {
                case LANGUAGE -> {
                    preferences.changeLanguage(value);
                    restartActivity();
                }
                case APP_THEME -> {
                    preferences.setAppTheme(value);
                    restartActivity();
                }
            }
        }
    }

    private void restartActivity() {
        FragmentActivity activity = requireActivity();
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(SETTINGS_CHANGED, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.finish();
        startActivity(intent);
    }
}