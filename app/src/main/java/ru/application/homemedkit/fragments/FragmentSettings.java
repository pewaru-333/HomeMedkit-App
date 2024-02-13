package ru.application.homemedkit.fragments;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static ru.application.homemedkit.helpers.ConstantsHelper.CHECK_EXP_DATE;
import static ru.application.homemedkit.helpers.ConstantsHelper.SETTINGS_CHANGED;
import static ru.application.homemedkit.helpers.SettingsHelper.APP_THEME;
import static ru.application.homemedkit.helpers.SettingsHelper.LANGUAGE;
import static ru.application.homemedkit.helpers.SettingsHelper.LIGHT_PERIOD;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import ru.application.homemedkit.R;
import ru.application.homemedkit.helpers.SettingsHelper;

public class FragmentSettings extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

    private SettingsHelper preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new SettingsHelper(requireActivity().getBaseContext());
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
            var value = sharedPreferences.getAll().get(key);

            switch (key) {
                case LANGUAGE -> {
                    requireActivity().getIntent().putExtra(SETTINGS_CHANGED, true);
                    preferences.changeLanguage((String) value);
                }
                case APP_THEME -> {
                    requireActivity().getIntent().putExtra(SETTINGS_CHANGED, true);
                    preferences.setAppTheme((String) value);
                }
                case CHECK_EXP_DATE ->
                        preferences.setExpDateChecker(requireActivity(), (Boolean) value);
                case LIGHT_PERIOD -> preferences.setLightPeriod((Boolean) value);
            }
        }
    }
}