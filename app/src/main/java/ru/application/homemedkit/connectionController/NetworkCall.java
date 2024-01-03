package ru.application.homemedkit.connectionController;

import static ru.application.homemedkit.helpers.ConstantsHelper.CIS;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ru.application.homemedkit.BuildConfig;
import ru.application.homemedkit.connectionController.models.MainModel;

public interface NetworkCall {

    @FormUrlEncoded
    @POST(BuildConfig.API_URL)
    Call<MainModel> requestInfo(@Field(CIS) String cis);
}
