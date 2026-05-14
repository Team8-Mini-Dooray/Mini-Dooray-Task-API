# Minidooray Team 8

이 저장소는 Minidooray 프로젝트의 Gateway 서비스입니다. Gateway는 UI 렌더링(Thymeleaf), 인증(Spring Security, Form Login, Redis Session), 그리고 백엔드 서비스로의 라우팅을 담당합니다.

## API 명세서 (API Specification)

Gateway 서비스는 `RestTemplate`을 사용하여 다음 백엔드 API와 통신합니다.

### 1. Account API (`http://localhost:8081`)
사용자 계정 정보, 인증 데이터 및 사용자 상태를 관리합니다.

#### 1.1 회원 가입 (Sign Up)
- **[POST]** `/accounts/signup`
- **Request Body**:
  ```json
  {
    "id": "user_id",
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response** (201 Created):
  ```json
  {
    "id": "user_id",
    "status": "ACTIVE"
  }
  ```

#### 1.2 사용자 정보 조회 (인증 검증용)
- **[GET]** `/accounts/{id}`
- **Response** (200 OK):
  ```json
  {
    "id": "user_id",
    "email": "user@example.com",
    "password": "{bcrypt}encoded_password",
    "status": "ACTIVE" 
  }
  ```

#### 1.3 사용자 상태 변경
- **[PUT]** `/accounts/{id}/status`
- **Request Body**:
  ```json
  {
    "status": "DELETED"
  }
  ```

### 2. Task API (`http://localhost:8082`)
프로젝트, 업무(Task), 댓글, 태그, 마일스톤을 관리합니다.
*(인증된 사용자의 ID를 `X-User-Id` 헤더에 담아 전송하여 권한을 확인합니다.)*

#### 2.1 프로젝트 (Projects)
- **[POST]** `/projects` (프로젝트 생성)
    - **Request Body**: `{"name": "New Project"}`
    - **Response**: `{"projectId": 1, "name": "New Project", "status": "ACTIVE"}`
- **[GET]** `/projects?memberId={user_id}` (사용자별 참여 프로젝트 목록 조회)
- **[POST]** `/projects/{projectId}/members` (프로젝트 멤버 추가)
    - **Request Body**: `{"memberId": "another_user"}`

#### 2.2 업무 (Tasks)
- **[POST]** `/projects/{projectId}/tasks` (업무 생성)
    - **Request Body**: `{"title": "로그인 구현", "content": "Spring Security 사용"}`
- **[GET]** `/projects/{projectId}/tasks` (프로젝트 내 업무 목록 조회)
- **[GET]** `/projects/{projectId}/tasks/{taskId}` (업무 상세 조회)
- **[PUT]** `/projects/{projectId}/tasks/{taskId}` (업무 수정)
- **[DELETE]** `/projects/{projectId}/tasks/{taskId}` (업무 삭제)

#### 2.3 댓글 (Comments)
- **[POST]** `/projects/{projectId}/tasks/{taskId}/comments` (댓글 추가)
    - **Request Body**: `{"content": "수정이 필요합니다."}`
- **[PUT]** `/projects/{projectId}/tasks/{taskId}/comments/{commentId}` (댓글 수정)
- **[DELETE]** `/projects/{projectId}/tasks/{taskId}/comments/{commentId}` (댓글 삭제)

#### 2.4 태그 & 마일스톤 (Tags & Milestones)
- **[POST]** `/projects/{projectId}/tags` (프로젝트 내 태그 생성)
    - **Request Body**: `{"name": "Backend"}`
- **[POST]** `/projects/{projectId}/milestones` (프로젝트 내 마일스톤 생성)
    - **Request Body**: `{"name": "Release 1.0", "startDate": "...", "endDate": "..."}`
- **[PUT]** `/projects/{projectId}/tasks/{taskId}/tags` (업무에 태그 설정)
    - **Request Body**: `{"tagIds": [1, 2]}`
- **[PUT]** `/projects/{projectId}/tasks/{taskId}/milestone` (업무에 마일스톤 설정)
    - **Request Body**: `{"milestoneId": 1}`