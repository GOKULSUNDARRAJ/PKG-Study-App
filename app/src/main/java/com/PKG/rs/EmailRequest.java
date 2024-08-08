package com.PKG.rs;

import com.google.gson.annotations.SerializedName;

public class EmailRequest {
    @SerializedName("email")
    private String email;

    public EmailRequest(String email) {
        this.email = email;
    }
}
