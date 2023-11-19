package ru.application.homemedkit.connectionController;

import static ru.application.homemedkit.helpers.ConstantsHelper.CIS;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import ru.application.homemedkit.BuildConfig;

public interface NetworkCall {

    @FormUrlEncoded
    @POST(BuildConfig.API_URL)
    Call<ResponseBody> requestInfo(@Field(CIS) String cis);
}
