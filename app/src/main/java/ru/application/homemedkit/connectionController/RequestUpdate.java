package ru.application.homemedkit.connectionController;

import static ru.application.homemedkit.connectionController.RequestNew.getMedicine;
import static ru.application.homemedkit.helpers.ConstantsHelper.CATEGORY;
import static ru.application.homemedkit.helpers.ConstantsHelper.ID;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.application.homemedkit.activities.MedicineActivity;
import ru.application.homemedkit.connectionController.models.MainModel;
import ru.application.homemedkit.databaseController.Medicine;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.dialogs.LoadingDialog;
import ru.application.homemedkit.graphics.Snackbars;

public class RequestUpdate implements Callback<MainModel> {
    private final Activity activity;
    private final LoadingDialog dialog;
    private final long id;

    public RequestUpdate(Activity activity, LoadingDialog dialog, long id) {
        this.activity = activity;
        this.dialog = dialog;
        this.id = id;
    }

    @Override
    public void onResponse(@NonNull Call<MainModel> call, @NonNull Response<MainModel> response) {
        if (response.isSuccessful()) {
            MainModel body = response.body();
            if (body != null) {
                if (body.category == null) {
                    activity.runOnUiThread(dialog::dismissDialog);
                    new Snackbars(activity).wrongCode();
                } else if (body.category.equals(CATEGORY)) {
                    if (body.codeFounded && body.checkResult) {
                        MedicineDatabase database = MedicineDatabase.getInstance(activity);
                        Medicine medicine = getMedicine(body);

                        medicine.id = id;
                        database.medicineDAO().update(medicine);

                        Intent intent = new Intent(activity, MedicineActivity.class);
                        intent.putExtra(ID, id);

                        activity.runOnUiThread(dialog::dismissDialog);
                        activity.startActivity(intent);
                    } else {
                        activity.runOnUiThread(dialog::dismissDialog);
                        new Snackbars(activity).codeNotFound();
                    }
                } else {
                    activity.runOnUiThread(dialog::dismissDialog);
                    new Snackbars(activity).wrongCodeCategory();
                }
            }
        } else {
            activity.runOnUiThread(dialog::dismissDialog);
            new Snackbars(activity).fetchError();
        }
    }

    @Override
    public void onFailure(@NonNull Call<MainModel> call, @NonNull Throwable t) {
        activity.runOnUiThread(dialog::dismissDialog);
        new Snackbars(activity).noNetwork();
    }
}
