package br.com.breno.animallovers.notification

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("send/sendPosts/")
    fun sendNotificationPosts(
        @Query("token") token: String?,
        @Query("title") title: String?,
        @Query("body") body: String?,
        @Query("post") post: String,
        @Query("pet") pet: String,
        @Query("kindOfNotification") kindOfNotification : String,
        @Query("idNotification") idNotification : String
    ): Call<ResponseBody>

    @GET("send/sendFriendship/")
    fun sendNotificationFriendship(
        @Query("token") token: String?,
        @Query("title") title: String?,
        @Query("body") body: String?,
        @Query("petLogged") petLogged: String,
        @Query("pet") pet: String,
        @Query("kindOfNotification") kindOfNotification : String,
        @Query("idNotification") idNotification : String
    ): Call<ResponseBody>

    @GET("send/sendChat/")
    fun sendNotificationChat(
        @Query("token") token: String?,
        @Query("title") title: String?,
        @Query("body") body: String?,
        @Query("userLogged") userLogged: String,
        @Query("user") user: String,
        @Query("kindOfNotification") kindOfNotification : String,
        @Query("idNotification") idNotification : String
    ): Call<ResponseBody>
}