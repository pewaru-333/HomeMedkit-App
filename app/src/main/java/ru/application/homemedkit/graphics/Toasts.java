package ru.application.homemedkit.graphics;

import android.content.Context;
import android.widget.Toast;

public class Toasts extends Toast {
    public Toasts(Context context, int resId) {
        super(context);

        makeText(context, resId, LENGTH_SHORT).show();
    }
}
