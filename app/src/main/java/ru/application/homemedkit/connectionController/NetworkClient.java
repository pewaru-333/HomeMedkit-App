package ru.application.homemedkit.connectionController;

import retrofit2.Retrofit;
import ru.application.homemedkit.BuildConfig;

public class NetworkClient {
    public static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(BuildConfig.BASE_URL).build();
        }
        return retrofit;
    }
}
