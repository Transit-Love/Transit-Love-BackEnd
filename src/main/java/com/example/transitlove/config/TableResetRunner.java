package com.example.transitlove.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TableResetRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            log.info("heart_messages 테이블 재생성 시작...");

            // 테이블 삭제
            jdbcTemplate.execute("DROP TABLE IF EXISTS heart_messages");
            log.info("heart_messages 테이블 삭제 완료");

            // 테이블 생성
            jdbcTemplate.execute(
                "CREATE TABLE heart_messages (" +
                "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "    sender_profile_id BIGINT NOT NULL," +
                "    receiver_profile_id BIGINT NOT NULL," +
                "    content TEXT NOT NULL," +
                "    sent_at DATETIME(6) NOT NULL," +
                "    CONSTRAINT fk_heart_message_sender_profile " +
                "        FOREIGN KEY (sender_profile_id) REFERENCES profiles(id)," +
                "    CONSTRAINT fk_heart_message_receiver_profile " +
                "        FOREIGN KEY (receiver_profile_id) REFERENCES profiles(id)" +
                ")"
            );
            log.info("heart_messages 테이블 생성 완료");

        } catch (Exception e) {
            log.error("테이블 재생성 중 오류 발생: ", e);
        }
    }
}