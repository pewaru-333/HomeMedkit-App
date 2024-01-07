package ru.application.homemedkit.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.Arrays;

import ru.application.homemedkit.R;

public class ImageHelper {
    public static Drawable getIconType(Context context, String form) {
        TypedArray icons = context.getResources().obtainTypedArray(R.array.medicine_types_icons);
        String[] types = context.getResources().getStringArray(R.array.medicine_types);
        String type = StringHelper.formName(form).toUpperCase();
        Drawable icon = AppCompatResources.getDrawable(context, R.drawable.vector_type_unknown);

        int index = Arrays.asList(types).indexOf(type);
        if (index != -1) icon = icons.getDrawable(index);

        return icon;
    }
}
