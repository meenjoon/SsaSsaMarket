package com.mbj.ssassamarket.data.source.remote.network

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class ApiCall<T : Any>(private val call: Call<T>) : Call<ApiResponse<T>> {

    override fun enqueue(callback: Callback<ApiResponse<T>>) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val networkResult = try {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        ApiResultSuccess(body)
                    } else {
                        ApiResultError(code = response.code(), message = response.message())
                    }
                } catch (e: HttpException) {
                    ApiResultError(code = e.code(), message = e.message())
                } catch (t: Throwable) {
                    ApiResultException(t)
                }
                callback.onResponse(this@ApiCall, Response.success(networkResult))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                val networkResult = ApiResultException<T>(t)
                callback.onResponse(this@ApiCall, Response.success(networkResult))
            }
        })
    }

    override fun clone(): Call<ApiResponse<T>> {
        return ApiCall(call.clone())
    }

    override fun execute(): Response<ApiResponse<T>> {
        throw NotImplementedError()
    }

    override fun isExecuted(): Boolean = call.isExecuted

    override fun cancel() {
        call.cancel()
    }

    override fun isCanceled(): Boolean = call.isCanceled

    override fun request(): Request = call.request()

    override fun timeout(): Timeout = call.timeout()
}
