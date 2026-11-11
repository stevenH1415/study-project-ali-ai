package com.springali.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动时初始化演示账号 admin / 123456（密码使用 BCrypt 存储）。
 * 如果还没建表，只打印警告，不阻断服务启动。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(*) from sys_user where username = ?", Integer.class, "admin");
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.update(
                    "insert into sys_user (username, password, nickname, roles) values (?, ?, ?, ?)",
                    "admin", passwordEncoder.encode("123456"), "管理员", "admin");
            log.info("已初始化演示账号 admin / 123456");
        } catch (Exception e) {
            log.warn("初始化演示账号失败，请先执行 sql/01-schema.sql：{}", e.getMessage());
        }
    }
}
