package com.example.ilya.mytestnetworkapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    final OkHttpClient client = new OkHttpClient();
    private String mUrlEndpoint = "https://api.forismatic.com";
    private String endpoint = "https://api.forismatic.com";
    CopyOnWriteArrayList<Call> requestList = new CopyOnWriteArrayList<>();

    //  https://api.forismatic.com/api/1.0/get?method=getQuote&format=json

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Observable.just("Hello, world!")
                .map(s -> s + " -Dan")
                .map(s -> s.hashCode())
                .map(i -> Integer.toString(i))
                .subscribe(s -> Log.d("RXTEST", s));

        Observable.just(Arrays.asList("aaa", "bbb"))
                .flatMap(urls -> Observable.from(urls))
                .subscribe(url -> Log.d("RXTEST", url));

        Observable.from(Arrays.asList("url1", "url2", "url3"))
                .subscribe(url -> Log.d("RXTEST", url));



        Observable.just(Arrays.asList("Hello", "AAAA"))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
        .flatMap(stringList-> Observable.from(stringList))
        .subscribe(stringItem -> Log.d("RXTEST", stringItem));// from




        mImageView = (ImageView) findViewById(R.id.image_view);
        HttpUrl.Builder urlBuilder
                = HttpUrl.parse(mUrlEndpoint + "/api/1.0/get").newBuilder();
        urlBuilder.addQueryParameter("method", "getQuote").
                addQueryParameter("format", "json");
        String url = urlBuilder.build().toString();

        Request okHttp3Request = new Request.Builder()
                .tag("mytestrequest")
                .url(url)
                .build();

        final Call call = client.newCall(okHttp3Request);
        requestList.add(call);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled())
                    new Handler(Looper.getMainLooper()).post(() ->
                            handleFailure()); // UI THREAD
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                requestList.remove(call);
                if (call.isCanceled()) {
                    Timber.d("okHttp3 call cancelled");
                    return;
                }
                Observable.create((Observable.OnSubscribe<JsonObject>) subscriber -> {
                    String responseString = null;
                    JsonObject responseJson;
                    try {
                        responseString = response.body().string();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    } finally {
                        response.close();
                    }
                    try {
                        responseJson = (new JsonParser()).parse(responseString).getAsJsonObject();
                        Quote quote;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        if (responseJson != null) {
                            Type type = new TypeToken<Quote>() {
                            }.getType();
                            quote = gson.fromJson(responseJson, type);
                        }
                        subscriber.onNext(responseJson);
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }).map(jsonObject -> {
                    if (!response.isSuccessful()) {
                        showError("network error");
                    }
                    return jsonObject;
                }).map(jsonObject -> {
                    showError(jsonObject.toString());
//                    if (jsonObject == null || !jsonObject.has(SUCCESS_PARAM))
//                        throw new CatalitException("no response", LTCatalitClient.ERROR_CODE_NO_RESPONSE);
                    return jsonObject;
                }).subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                        .subscribe(result -> {

                        }, throwable -> {

                        });
            }
        });
    }

    private void showError(String erroText) {
        Toast.makeText(this, erroText, Toast.LENGTH_LONG).show();
    }

    private void handleFailure() {
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {

            final OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(urls[0])
                    .build();

            Response response = null;
            Bitmap mIcon11 = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response.isSuccessful()) {
                try {
                    mIcon11 = BitmapFactory.decodeStream(response.body().byteStream());
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}
