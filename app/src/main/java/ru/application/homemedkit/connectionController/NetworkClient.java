package ru.application.homemedkit.connectionController;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.application.homemedkit.BuildConfig;

public class NetworkClient {
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
