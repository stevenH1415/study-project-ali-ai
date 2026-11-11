package com.springali.auth.web.dto;

import java.util.List;

public record UserInfo(String username, String nickname, String avatar, List<String> roles) {
}
