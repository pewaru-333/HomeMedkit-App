package ru.application.homemedkit.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.util.Objects;

import ru.application.homemedkit.R;

public class ImageHelper {

    public static final String SPRAY = "СПРЕЙ";

    public static Drawable setImage(Context context, String form) {
        String type = StringHelper.formName(form);
        int id = R.drawable.no_medicine_type;

        if (Objects.equals(type, SPRAY))
            id = R.drawable.nasal_spray;

        return ContextCompat.getDrawable(context, id);
    }
}
