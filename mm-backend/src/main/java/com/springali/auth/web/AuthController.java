package com.springali.auth.web;

import com.springali.auth.web.dto.LoginRequest;
import com.springali.auth.web.dto.LoginResponse;
import com.springali.auth.web.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录认证：用户来自 PostgreSQL 的 sys_user 表，密码使用 BCrypt 校验。
 * Token 暂时保存在内存中（后续可换成 JWT / Redis）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, UserInfo> sessions = new ConcurrentHashMap<>();

    public AuthController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResult<LoginResponse>> login(@RequestBody LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResult.fail(401, "用户名和密码不能为空"));
        }
        try {
            List<UserRow> rows = jdbcTemplate.query(
                    "select username, password, nickname, avatar, roles "
                            + "from sys_user where username = ? and enabled = true",
                    (rs, rowNum) -> new UserRow(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("nickname"),
                            rs.getString("avatar"),
                            rs.getString("roles")),
                    request.username());

            if (rows.isEmpty() || !passwordEncoder.matches(request.password(), rows.get(0).password())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResult.fail(401, "用户名或密码错误（演示账号：admin / 123456）"));
            }

            UserRow row = rows.get(0);
            UserInfo user = new UserInfo(row.username(), row.nickname(), row.avatar(), splitRoles(row.roles()));
            String token = UUID.randomUUID().toString().replace("-", "");
            sessions.put(token, user);
            return ResponseEntity.ok(ApiResult.ok(new LoginResponse(token, user)));
        } catch (DataAccessException e) {
            log.error("查询用户失败", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResult.fail(503,
                            "数据库不可用：请确认 PostgreSQL 已启动，并已执行 sql/01-schema.sql"));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResult<UserInfo>> info(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserInfo user = sessions.get(extractToken(authorization));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResult.fail(401, "未登录或登录已过期"));
        }
        return ResponseEntity.ok(ApiResult.ok(user));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        sessions.remove(extractToken(authorization));
        return ApiResult.ok(null);
    }

    private String extractToken(String authorization) {
        if (authorization == null) {
            return "";
        }
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }

    private List<String> splitRoles(String roles) {
        if (roles == null) {
            return List.of();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record UserRow(String username, String password, String nickname,
                           String avatar, String roles) {
    }
}
