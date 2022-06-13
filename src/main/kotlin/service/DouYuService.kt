package shiroi.top.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface DouYuService {
    companion object{
        const val base_url = "https://www.douyu.com/";

        val create = Retrofit.Builder()
            .baseUrl(base_url)
            .build().create(DouYuService::class.java)!!
    }

    @GET("{id}")
    fun getId(@Path("id") id:String): Call<ResponseBody>
}