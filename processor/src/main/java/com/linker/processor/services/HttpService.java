package com.linker.processor.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Utils;
import com.linker.common.exceptions.HttpException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HttpService {

    final OkHttpClient client;

    final static String JSON_CONTENT = "application/json";
    final static MediaType JSON_MEDIA = MediaType.parse(JSON_CONTENT + " charset=utf-8");


    @Autowired
    public HttpService() {
        client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();

                    log.info("send {} request to {}", request.method(), request.url());
                    if (log.isDebugEnabled() && request.body() != null) {
                        Buffer requestBuffer = new Buffer();
                        request.body().writeTo(requestBuffer);
                        log.debug("body:{}", requestBuffer.readUtf8());
                    }

                    Response response = chain.proceed(request);
                    log.info("response: code:{}", response.code());
                    if (log.isDebugEnabled() && response.body() != null) {
                        String content = response.body().string();
                        log.debug("body:{}", content);
                        ResponseBody wrappedBody = ResponseBody.create(JSON_MEDIA, content);
                        return response.newBuilder().body(wrappedBody).build();
                    }
                    return response;
                }).build();
    }


    public <T> CompletableFuture<T> get(String url, Map<String, Object> queryMap, Class<T> responseClass) {
        return performAsyncRequest(buildGetRequest(url, queryMap), responseClass);
    }

    public <T> T getSync(String url, Map<String, Object> queryMap, Class<T> responseClass) throws IOException {
        return performSyncRequest(buildGetRequest(url, queryMap), responseClass);
    }

    public <T> CompletableFuture<T> post(String url, Object object, Class<T> responseClass) throws IOException {
        return performAsyncRequest(buildPostRequest(url, object), responseClass);
    }

    String buildUrl(String url, Map<String, Object> queryMap) {
        if (queryMap != null) {
            HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
            queryMap.forEach((key, value) -> builder.addQueryParameter(key, String.valueOf(value)));
            url = builder.build().toString();
        }

        return url;
    }

    Request buildGetRequest(String url, Map<String, Object> queryMap) {
        url = buildUrl(url, queryMap);
        return new Request.Builder().addHeader("Accept", JSON_CONTENT).url(url).get().build();
    }

    Request buildPostRequest(String url, Object object) throws JsonProcessingException {
        String json = Utils.toJson(object);

        return new Request.Builder()
                .addHeader("Content-Type", JSON_CONTENT)
                .addHeader("Accept", JSON_CONTENT)
                .url(url)
                .post(RequestBody.create(JSON_MEDIA, json))
                .build();
    }

    <T> CompletableFuture<T> performAsyncRequest(Request request, Class<T> responseClass) {
        CompletableFuture<T> result = new CompletableFuture<>();
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

    <T> T performSyncRequest(Request request, Class<T> responseClass) throws IOException {
        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            if (response.isSuccessful() && responseClass != null) {
                return Utils.fromJson(response.body().string(), responseClass);
            }

            throw new HttpException(response.code(), response.headers().toMultimap(), response.body() != null ? response.body().string() : null);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
