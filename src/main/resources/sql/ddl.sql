-- Users
CREATE TABLE users (
                       user_id     VARCHAR(50)  NOT NULL,
                       email       VARCHAR(100) NOT NULL,
                       password    VARCHAR(255) NOT NULL,           -- 암호화된 비밀번호
                       status      VARCHAR(20)  NOT NULL DEFAULT 'JOIN', -- JOIN, WITHDRAW, DORMANT

                       PRIMARY KEY (user_id),
                       UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Project
CREATE TABLE projects (
                          project_id  BIGINT AUTO_INCREMENT,
                          name        VARCHAR(100) NOT NULL,
                          status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DORMANT, TERMINATED
                          admin_id    VARCHAR(50)  NOT NULL,

                          PRIMARY KEY (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Project Member
CREATE TABLE project_members (
                                 project_member_id BIGINT AUTO_INCREMENT,
                                 project_id        BIGINT      NOT NULL,
                                 user_id           VARCHAR(50) NOT NULL,

                                 PRIMARY KEY (project_member_id),
                                 UNIQUE KEY uk_project_user (project_id, user_id),
                                 CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Milestone
CREATE TABLE milestones (
                            milestone_id BIGINT AUTO_INCREMENT,
                            project_id   BIGINT       NOT NULL,
                            name         VARCHAR(100) NOT NULL,
                            start_date   DATE         NULL,
                            end_date     DATE         NULL,

                            PRIMARY KEY (milestone_id),
                            CONSTRAINT fk_ms_project FOREIGN KEY (project_id) REFERENCES projects (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tag
CREATE TABLE tags (
                      tag_id      BIGINT AUTO_INCREMENT,
                      project_id  BIGINT      NOT NULL,
                      name        VARCHAR(50) NOT NULL,

                      PRIMARY KEY (tag_id),
                      CONSTRAINT fk_tag_project FOREIGN KEY (project_id) REFERENCES projects (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Task
CREATE TABLE tasks (
                       task_id      BIGINT AUTO_INCREMENT,
                       project_id   BIGINT       NOT NULL,
                       milestone_id BIGINT       NULL,              -- MileStone 삭제 시 NULL 처리
                       title        VARCHAR(200) NOT NULL,
                       content      TEXT         NULL,
                       writer_id    VARCHAR(50)  NOT NULL,
                       created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

                       PRIMARY KEY (task_id),
                       CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES projects (project_id),
                       CONSTRAINT fk_task_milestone FOREIGN KEY (milestone_id) REFERENCES milestones (milestone_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Task_Tag
CREATE TABLE task_tags (
                           task_tag_id BIGINT AUTO_INCREMENT,
                           task_id     BIGINT NOT NULL,
                           tag_id      BIGINT NOT NULL,

                           PRIMARY KEY (task_tag_id),

                           UNIQUE KEY uk_task_tag (task_id, tag_id),
                           CONSTRAINT fk_tt_task FOREIGN KEY (task_id) REFERENCES tasks (task_id) ON DELETE CASCADE,
                           CONSTRAINT fk_tt_tag  FOREIGN KEY (tag_id)  REFERENCES tags (tag_id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Comment
CREATE TABLE comments (
                          comment_id  BIGINT AUTO_INCREMENT,
                          task_id     BIGINT      NOT NULL,
                          writer_id   VARCHAR(50) NOT NULL,
                          content     TEXT        NOT NULL,
                          created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

                          PRIMARY KEY (comment_id),
                          CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES tasks (task_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;