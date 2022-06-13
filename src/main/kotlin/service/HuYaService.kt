package shiroi.top.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface HuYaService {
    companion object{
        const val base_url = "https://www.huya.com/";

        val create = Retrofit.Builder()
            .baseUrl(base_url)
            .build().create(HuYaService::class.java)!!
    }

    @GET("{id}")
    fun getId(@Path("id") id:String): Call<ResponseBody>
}