package ru.application.homemedkit.connectionController;

import static ru.application.homemedkit.helpers.StringHelper.formatCode;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import okhttp3.ResponseBody;
import retrofit2.Call;
import ru.application.homemedkit.dialogs.LoadingDialog;
import ru.application.homemedkit.graphics.Snackbars;

public class RequestAPI implements DecodeCallback {

    private final Activity activity;

    public RequestAPI(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onDecoded(@NonNull Result result) {
        if (result.getBarcodeFormat() == BarcodeFormat.DATA_MATRIX && result.getNumBits() == 688) {
            NetworkCall controller = NetworkClient.getInstance().create(NetworkCall.class);
            Call<ResponseBody> request = controller.requestInfo(formatCode(result.getText()));
            LoadingDialog loading = new LoadingDialog(activity);

            activity.runOnUiThread(loading::showDialog);
            request.enqueue(new RequestNew(activity, formatCode(result.getText()), loading));
        } else new Snackbars(activity).error();
    }

    public void onDecoded(long id, @NonNull String result) {
        if (result.length() == 85) {
            NetworkCall controller = NetworkClient.getInstance().create(NetworkCall.class);
            Call<ResponseBody> request = controller.requestInfo(result);
            LoadingDialog loading = new LoadingDialog(activity);

            activity.runOnUiThread(loading::showDialog);
            request.enqueue(new RequestUpdate(activity, loading, id));
        } else new Snackbars(activity).error();
    }
}
