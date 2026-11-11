package com.springali.auth.web.dto;

public record LoginResponse(String token, UserInfo user) {
}
