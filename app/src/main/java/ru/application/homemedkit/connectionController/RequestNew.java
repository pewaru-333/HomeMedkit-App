package ru.application.homemedkit.connectionController;

import static ru.application.homemedkit.helpers.ConstantsHelper.CATEGORY;
import static ru.application.homemedkit.helpers.ConstantsHelper.DUPLICATE;
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
import ru.application.homemedkit.databaseController.MedicineDAO;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.databaseController.Technical;
import ru.application.homemedkit.dialogs.AddMedicineDialog;
import ru.application.homemedkit.dialogs.LoadingDialog;
import ru.application.homemedkit.graphics.Snackbars;

public class RequestNew implements Callback<MainModel> {

    private final Activity activity;
    private final LoadingDialog dialog;
    private final String cis;
    private long id = -1;

    public RequestNew(Activity activity, String cis, LoadingDialog dialog) {
        this.activity = activity;
        this.dialog = dialog;
        this.cis = cis;
    }

    @NonNull
    public static Medicine getMedicine(MainModel body) {
        return new Medicine(
                body.cis,
                body.drugsData.prodDescLabel,
                body.drugsData.expireDate,
                body.drugsData.foiv.prodFormNormName,
                body.drugsData.foiv.prodDNormName,
                body.drugsData.vidalData.phKinetics,
                new Technical(Boolean.TRUE, Boolean.TRUE)
        );
    }

    @Override
    public void onResponse(@NonNull Call<MainModel> call, Response<MainModel> response) {
        if (response.isSuccessful()) {
            MainModel body = response.body();
            if (body != null) {
                if (body.category == null) {
                    activity.runOnUiThread(dialog::dismissDialog);
                    new Snackbars(activity).wrongCodeCategory();
                } else if (body.category.equals(CATEGORY)) {
                    if (body.codeFounded && body.checkResult) {
                        MedicineDatabase database = MedicineDatabase.getInstance(activity);
                        MedicineDAO dao = database.medicineDAO();
                        Medicine medicine = getMedicine(body);
                        Intent intent = new Intent(activity, MedicineActivity.class);

                        if (dao.getAllCIS().contains(cis)) {
                            id = dao.getIDbyCis(cis);
                            intent.putExtra(DUPLICATE, true);
                        } else id = database.medicineDAO().add(medicine);

                        intent.putExtra(ID, id);

                        activity.runOnUiThread(dialog::dismissDialog);
                        activity.startActivity(intent);
                    } else {
                        changeDialog(dialog);
                        new Snackbars(activity).codeNotFound();
                    }
                } else {
                    activity.runOnUiThread(dialog::dismissDialog);
                    new Snackbars(activity).wrongCodeCategory();
                }
            }
        } else {
            activity.runOnUiThread(dialog::dismissDialog);
            new Snackbars(activity).error();
        }
    }

    @Override
    public void onFailure(@NonNull Call<MainModel> call, @NonNull Throwable t) {
        MedicineDatabase database = MedicineDatabase.getInstance(activity);
        MedicineDAO dao = database.medicineDAO();
        Intent intent = new Intent(activity, MedicineActivity.class);

        if (dao.getAllCIS().contains(cis)) {
            id = dao.getIDbyCis(cis);
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
