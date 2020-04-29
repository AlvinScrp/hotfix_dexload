package com.a.retrofitsample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.a.library.JSONUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.schedulers.IoScheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_retrofit.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


/**
 * 官网  https://square.github.io/retrofit/
 */
class RetrofitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrofit)

        btnOkHttp.setOnClickListener { okHttpRequestRepos("octocat") }
//        btnRetrofit.setOnClickListener { retrofitRequestRepos("octocat") }
        btnRetrofit.setOnClickListener { retrofitRequestReposRx("octocat") }

    }

    private fun okHttpRequestRepos(userName: String) {
        val request = Request.Builder()
            .url("https://api.github.com/users/$userName/repos")
            .get()
            .build()
        val okHttpClient = OkHttpClient()
        var call=okHttpClient.newCall(request)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

                if (response.code() == 200) {
                    var responseString = response.body()?.string();
                    var repos: List<RepoBean>? =
                        JSONUtils.parseArray(responseString, RepoBean::class.java)
                    var fullName = repos?.get(0)?.full_name ?: ""
                    Handler(Looper.getMainLooper()).post { tvOkHttp.text = fullName }
                }

            }
        })
    }

    private fun retrofitRequestRepos(userName: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(GitHubService::class.java)
        var call = service.listRepos(userName)
        call.enqueue(object : Callback<MutableList<RepoBean>> {
            override fun onFailure(call: Call<MutableList<RepoBean>>, t: Throwable) {
                tvRetrofit.text = t.message
            }

            override fun onResponse(
                call: Call<MutableList<RepoBean>>,
                response: Response<MutableList<RepoBean>>
            ) {
                tvRetrofit.text = response.body()?.get(0)?.full_name ?: ""
            }

        })
    }

    private fun retrofitRequestReposRx(userName: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(GitHubService::class.java)
        service.listReposRx(userName)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { repos -> tvRetrofit.text = repos.get(0)?.full_name ?: "" },
                {
                    tvRetrofit.text = it.message
                    it.printStackTrace()
                }
            )

    }
}