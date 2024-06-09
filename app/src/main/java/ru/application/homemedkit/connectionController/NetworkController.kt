package ru.application.homemedkit.connectionController

import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url
import ru.application.homemedkit.connectionController.models.MainModel
import ru.application.homemedkit.helpers.CIS

private const val BASE_URL = "https://mobile.api.crpt.ru/"
private const val API_URL = "mobile/check"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
    .build()

interface NetworkCall {
    @FormUrlEncoded
    @POST(API_URL)
    suspend fun requestData(@Field(CIS) cis: String): Response<MainModel>

    @GET
    @Streaming
    suspend fun getImage(@Url url: String): Response<ResponseBody>
}

object NetworkAPI {
    val client: NetworkCall by lazy { retrofit.create(NetworkCall::class.java) }
}