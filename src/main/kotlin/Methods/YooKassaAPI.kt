package Methods

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Base64

object YooKassaAPI {

    private const val YOOKASSA_SHOP_ID = "1019095"
    private const val YOOKASSA_SECRET_KEY = "live_CjlL6CRpKzjajEHVOV5-fVKiZh3Csp0ohM0luJMIfq8"
    private const val YOOKASSA_API_URL = "https://api.yookassa.ru/v3/"

    private val client = OkHttpClient()
    private val mediaType = "application/json".toMediaType()

    // Метод для создания платежа
    fun createPayment(amount: Double, currency: String, description: String, chatId: Long): String? {
        val json = """
        {
            "amount": {
                "value": "$amount",
                "currency": "$currency"
            },
            "confirmation": {
                "type": "redirect",
                "return_url": "https://yourwebsite.com/return"
            },
            "capture": true,
            "description": "$description",
            "metadata": {
                "chatId": "$chatId"
            }
        }
    """.trimIndent()

        val requestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${YOOKASSA_API_URL}payments")
            .post(requestBody)
            .addHeader("Authorization", "Basic ${Base64.getEncoder().encodeToString("$YOOKASSA_SHOP_ID:$YOOKASSA_SECRET_KEY".toByteArray())}")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                println("Failed to create payment: ${response.code} - ${response.message}")
                null
            }
        } catch (e: IOException) {
            println("Error creating payment: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Метод для проверки статуса платежа
    fun checkPaymentStatus(paymentId: String): String? {
        val request = Request.Builder()
            .url("${YOOKASSA_API_URL}payments/$paymentId")
            .get()
            .addHeader("Authorization", "Basic ${Base64.getEncoder().encodeToString("$YOOKASSA_SHOP_ID:$YOOKASSA_SECRET_KEY".toByteArray())}")
            .build()

        return try {
            val response = client.newCall(request).execute()
            response.body?.string()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}