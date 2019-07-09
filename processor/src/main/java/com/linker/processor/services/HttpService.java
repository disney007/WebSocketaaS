package com.linker.processor.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Utils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HttpService {

    OkHttpClient client = new OkHttpClient.Builder().build();

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public <T> CompletableFuture<T> post(String url, Object object, Class<T> responseClass) throws JsonProcessingException {
        String json = Utils.toJson(object);
        log.info("send post request to {} with {}", url, json);
        CompletableFuture<T> result = new CompletableFuture<>();
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(url)
                .post(RequestBody.create(JSON, json))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                result.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    T resObject = Utils.fromJson(response.body().string(), responseClass);
                    result.complete(resObject);
                } catch (IOException e) {
                    result.completeExceptionally(e);
                }
            }
        });

        return result;
    }
}
