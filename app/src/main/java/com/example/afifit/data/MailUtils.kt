import android.util.Base64
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

object MailUtils {
    private const val API_BASE_URL = "https://api.mailgun.net/v3/sandbox3b502df685974ebea6bb224177458b2c.mailgun.org/"
    private const val API_KEY = "3487bbd55b476ac8d4e64855d2b86c59-51356527-328015f6"

    fun sendEmail(to: String?, subject: String?, text: String?) {
        val client = OkHttpClient()
        val auth = "Basic " + Base64.encodeToString(("api:$API_KEY").toByteArray(), Base64.NO_WRAP)

        val body: RequestBody = FormBody.Builder()
            .add("from", "maxwell.opondo@strathmore.edu")
            .add("to", to ?: "")
            .add("subject", subject ?: "")
            .add("text", text ?: "")
            .build()

        val request = Request.Builder()
            .url(API_BASE_URL + "messages")
            .post(body)
            .header("Authorization", auth)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
