package data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NutritionService {
    @GET("search")
    suspend fun getBreakfastDiet(
        @Query("q") query: String,
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String
    ): Response<List<NutritionResponse>>
}
