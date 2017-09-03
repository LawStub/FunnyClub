package com.lawstub.funnyclub.utils;

import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by 廖婵001 on 2017/8/28 0028.
 */

public class GetOkHttpClient {
    private static OkHttpClient sClient;

    public static OkHttpClient get(){
        if(sClient == null){
            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/FunnyClubCache");
            if(!file.exists()) file.mkdir();
            Cache cache = new Cache(file,1024*1024*100);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            sClient = builder.addInterceptor(new MyInterptor())
                    .cache(cache)
                    .build();
        }
        return sClient;
    }

    public static class MyInterptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response newResponse = chain.proceed(request)
                    .newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .addHeader("Cache-Control","public,only-if-cached")
                    .build();
            return newResponse;
        }
    }

}
