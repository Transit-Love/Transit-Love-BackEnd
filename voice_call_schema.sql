-- 음성 통화 기록 테이블 생성
CREATE TABLE IF NOT EXISTS voice_call_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id BIGINT NOT NULL,
    caller_profile_id BIGINT NOT NULL,
    receiver_profile_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration INT NOT NULL COMMENT '통화 시간(초)',
    call_status VARCHAR(20) NOT NULL COMMENT 'COMPLETED, REJECTED, FAILED, MISSED',
    voice_modulation_used BOOLEAN DEFAULT FALSE COMMENT '음성 변조 사용 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_chat_room_id (chat_room_id),
    INDEX idx_caller_profile_id (caller_profile_id),
    INDEX idx_receiver_profile_id (receiver_profile_id),
    INDEX idx_start_time (start_time),
    INDEX idx_call_status (call_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='음성 통화 기록';

-- 음소거 상태 테이블 생성
CREATE TABLE IF NOT EXISTS voice_mute_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_chat_room_profile (chat_room_id, profile_id),
    INDEX idx_chat_room_id (chat_room_id),
    INDEX idx_profile_id (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='음소거 상태';

