package com.linker.processor.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Utils;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpService {

    OkHttpClient client = new OkHttpClient.Builder().build();

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public void post(String url, Object object) throws JsonProcessingException {
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(url)
                .post(
                        RequestBody.create(JSON, Utils.toJson(object))
                )
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
}
