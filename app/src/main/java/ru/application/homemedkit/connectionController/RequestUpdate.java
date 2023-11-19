package ru.application.homemedkit.connectionController;

import static ru.application.homemedkit.helpers.ConstantsHelper.ID;
import static ru.application.homemedkit.helpers.StringHelper.parseJSON;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.application.homemedkit.activities.MedicineActivity;
import ru.application.homemedkit.databaseController.Medicine;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.dialogs.LoadingDialog;
import ru.application.homemedkit.graphics.Snackbars;

public class RequestUpdate implements Callback<ResponseBody> {
    private final Activity activity;
    private final LoadingDialog dialog;
    private final long id;

    public RequestUpdate(Activity activity, LoadingDialog dialog, long id) {
        this.activity = activity;
        this.dialog = dialog;
        this.id = id;
    }

    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, Response<ResponseBody> response) {
        if (response.isSuccessful()) {
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    MedicineDatabase database = MedicineDatabase.getInstance(activity);
                    Medicine medicine = parseJSON(body.string());

                    medicine.id = id;
                    database.medicineDAO().update(medicine);

                    Intent intent = new Intent(activity, MedicineActivity.class);
                    intent.putExtra(ID, id);

                    activity.runOnUiThread(dialog::dismissDialog);
                    activity.startActivity(intent);
                }
            } catch (IOException e) {
                showError();
            }
        } else showError();
    }

    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
        activity.runOnUiThread(dialog::dismissDialog);
        new Snackbars(activity).noNetwork();
    }

    private void showError() {
        activity.runOnUiThread(dialog::dismissDialog);
        new Snackbars(activity).fetchError();
    }
}
