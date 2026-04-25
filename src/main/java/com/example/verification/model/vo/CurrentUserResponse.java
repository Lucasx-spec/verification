package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentUserResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String realName;
    private String email;
    private String phone;
    private String roleCode;
    private Boolean signEnabled;
    private String publicKey;
}
