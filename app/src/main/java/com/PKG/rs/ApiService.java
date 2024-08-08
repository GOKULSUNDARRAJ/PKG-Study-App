package com.PKG.rs;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/users")
    Call<List<Map<String, String>>> postData(@Body User user);

    @POST("get_list")
    Call<List<QuestionPaper>> getList(@Body EmailRequest emailRequest);
    }


