package ru.application.homemedkit.connectionController;

import static ru.application.homemedkit.helpers.ConstantsHelper.DUPLICATE;
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
import ru.application.homemedkit.databaseController.MedicineDAO;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.dialogs.AddMedicineDialog;
import ru.application.homemedkit.dialogs.LoadingDialog;
import ru.application.homemedkit.graphics.Snackbars;

public class RequestNew implements Callback<ResponseBody> {
    private final Activity activity;
    private final LoadingDialog dialog;
    private final String cis;
    private long id = -1;

    public RequestNew(Activity activity, String cis, LoadingDialog dialog) {
        this.activity = activity;
        this.cis = cis;
        this.dialog = dialog;
    }

    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, Response<ResponseBody> response) {
        if (response.isSuccessful()) {
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    MedicineDatabase database = MedicineDatabase.getInstance(activity);
                    MedicineDAO dao = database.medicineDAO();
                    Medicine medicine = parseJSON(body.string());
                    Intent intent = new Intent(activity, MedicineActivity.class);

                    if (medicine.expDate == -1L) {
                        dialog.dismissDialog();
                        new Snackbars(activity).error();
                    } else {
                        if (dao.getAllCIS().contains(cis)) {
                            id = dao.getAllCIS().indexOf(cis) + 1;
                            intent.putExtra(DUPLICATE, true);
                        } else id = database.medicineDAO().add(medicine);

                        intent.putExtra(ID, id);

                        activity.runOnUiThread(dialog::dismissDialog);
                        activity.startActivity(intent);
                    }
                }
            } catch (IOException e) {
                changeDialog(dialog);
            }
        } else changeDialog(dialog);
    }


    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
        MedicineDatabase database = MedicineDatabase.getInstance(activity);
        Intent intent = new Intent(activity, MedicineActivity.class);

        if (database.medicineDAO().getAllCIS().contains(cis)) {
            id = database.medicineDAO().getAllCIS().indexOf(cis) + 1;
            intent.putExtra(DUPLICATE, true);
            intent.putExtra(ID, id);
            activity.runOnUiThread(dialog::dismissDialog);
            activity.startActivity(intent);
        } else {
            changeDialog(dialog);
        }
    }

    private void changeDialog(@NonNull LoadingDialog loading) {
        activity.runOnUiThread(loading::dismissDialog);
        activity.runOnUiThread(new AddMedicineDialog(activity, cis)::showDialog);
    }
}
