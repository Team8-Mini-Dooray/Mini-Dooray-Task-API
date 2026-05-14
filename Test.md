# Minidooray Team 8 Gateway API Specification

이 문서는 Minidooray Gateway 서비스에서 처리하는 모든 API 엔드포인트와 해당 요청/응답 구조를 상세히 설명합니다.

---

## 1. Account API (계정 관리)

### 1.1 회원가입
- **Endpoint**: `POST /accounts/signup`
- **Request (SignupRequest)**:
  ```json
  {
    "id": "user123",
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response (SignupResponse)**:
  ```json
  {
    "id": "user123",
    "status": "ACTIVE"
  }
  ```

### 1.2 로그인
- **Endpoint**: `POST /accounts/login`
- **Request (LoginRequest)**:
  ```json
  {
    "userId": "user123",
    "password": "password123"
  }
  ```
- **Response (LoginResponse)**:
  ```json
  {
    "userId": "user123"
  }
  ```
  *(성공 시 Redis 세션에 `USER_ID`가 저장됩니다.)*

### 1.3 로그아웃
- **Endpoint**: `POST /accounts/logout`
- **Description**: 현재 세션을 무효화(invalidate)합니다.

### 1.4 사용자 상태 변경
- **Endpoint**: `POST /accounts/users/{userId}/status`
- **Request (UserStatusUpdateRequest)**:
  ```json
  {
    "status": "DORMANT"
  }
  ```

---

## 2. Project API (프로젝트 관리)

### 2.1 프로젝트 목록 조회
- **Endpoint**: `GET /projects`
- **Response**: `List<ProjectDto>`
  ```json
  [
    {
      "projectId": 1,
      "name": "Project A",
      "status": "ACTIVE"
    }
  ]
  ```

### 2.2 프로젝트 상세 조회
- **Endpoint**: `GET /projects/{projectId}`
- **Response (ProjectDetailDto)**:
  ```json
  {
    "projectId": 1,
    "name": "Project A",
    "status": "ACTIVE",
    "members": [ { "userId": "user123" } ],
    "tasks": [
      {
        "taskId": 1,
        "title": "Task 1",
        "content": "Content...",
        "writerId": "user123",
        "createdAt": "2023-10-27T10:00:00"
      }
    ],
    "milestones": [
      {
        "milestoneId": 1,
        "name": "Sprint 1",
        "startDate": "2023-10-01",
        "endDate": "2023-10-15"
      }
    ]
  }
  ```

### 2.3 프로젝트 생성
- **Endpoint**: `POST /projects`
- **Request (ProjectCreateRequest)**:
  ```json
  {
    "name": "New Project"
  }
  ```
- **Response (ProjectDto)**: 생성된 프로젝트 정보 반환

### 2.4 프로젝트 수정
- **Endpoint**: `POST /projects/{projectId}/edit`
- **Request (ProjectUpdateRequest)**:
  ```json
  {
    "name": "Updated Name",
    "status": "DORMANT"
  }
  ```

### 2.5 프로젝트 종료
- **Endpoint**: `POST /projects/{projectId}/close`
- **Description**: 프로젝트 상태를 `CLOSED`로 강제 업데이트합니다.

---

## 3. Task API (업무 관리)

### 3.1 업무 상세 조회
- **Endpoint**: `GET /projects/{projectId}/tasks/{taskId}`
- **Response (TaskDetailDto)**:
  ```json
  {
    "taskId": 1,
    "title": "Task 1",
    "content": "Detailed Content",
    "writerId": "user123",
    "createdAt": "2023-10-27T10:00:00",
    "comments": [
      {
        "commentId": 1,
        "writerId": "user123",
        "content": "First Comment",
        "createdAt": "2023-10-27T11:00:00"
      }
    ]
  }
  ```

### 3.2 업무 생성
- **Endpoint**: `POST /projects/{projectId}/tasks`
- **Request (TaskCreateRequest)**:
  ```json
  {
    "projectId": 1,
    "title": "New Task",
    "content": "Task Content",
    "writerId": "user123"
  }
  ```

### 3.3 업무 수정
- **Endpoint**: `POST /projects/{projectId}/tasks/{taskId}/edit`
- **Request (TaskUpdateRequest)**:
  ```json
  {
    "title": "Updated Title",
    "content": "Updated Content"
  }
  ```

### 3.4 업무 삭제
- **Endpoint**: `POST /projects/{projectId}/tasks/{taskId}/delete`

### 3.5 업무 마일스톤 설정
- **Endpoint**: `POST /projects/{projectId}/tasks/{taskId}/milestones`
- **Request (TaskMilestoneRequest)**:
  ```json
  {
    "milestoneId": 2
  }
  ```

### 3.6 업무 태그 설정
- **Endpoint**: `POST /projects/{projectId}/tasks/{taskId}/tags`
- **Request (TaskTagRequest)**:
  ```json
  {
    "tagIds": [1, 2, 5]
  }
  ```

---

## 4. Milestone API (마일스톤 관리)
### 4.1 마일스톤 상세 조회
- **Endpoint**: `GET /projects/{projectId}/milestones/{milestoneId}`
- **Response (MilestoneDetailDto)**:
  ```json
  {
    "milestoneId": 1,
    "name": "Sprint 1",
    "startDate": "2023-10-01",
    "endDate": "2023-10-15",
    "tasks": [
      {
        "taskId": 1,
        "title": "Task Title",
        "content": "Task Content",
        "writerId": "user123",
        "createdAt": "2023-10-27T10:00:00"
      }
    ]
  }
  ```

### 4.2 마일스톤 생성
- **Endpoint**: `POST /projects/{projectId}/milestones`
- **Request (MilestoneCreateRequest)**:
  ```json
  {
    "name": "Sprint 1",
    "startDate": "2023-10-01",
    "endDate": "2023-10-15"
  }
  ```
- **Response (MilestoneDto)**: 생성된 마일스톤 정보 반환

### 4.3 마일스톤 수정
- **Endpoint**: `POST /projects/{projectId}/milestones/{milestoneId}/edit`
- **Request (MilestoneCreateRequest)**:
  ```json
  {
    "name": "Updated Sprint Name",
    "startDate": "2023-10-02",
    "endDate": "2023-10-16"
  }
  ```

### 4.4 마일스톤 삭제
- **Endpoint**: `POST /projects/{projectId}/milestones/{milestoneId}/delete`
- **Description**: 해당 마일스톤을 삭제합니다.

---

## 5. Tag API (태그 관리)

### 5.1 태그 목록 조회
- **Endpoint**: `GET /projects/{projectId}/tags`
- **Response**: `List<TagDto>`
  ```json
  [
    {
      "tagId": 1,
      "name": "Backend"
    },
    {
      "tagId": 2,
      "name": "UI/UX"
    }
  ]
  ```

### 5.2 태그 생성
- **Endpoint**: `POST /projects/{projectId}/tags`
- **Request (TagCreateRequest)**:
  ```json
  {
    "name": "New Tag"
  }
  ```
- **Response (TagDto)**: 생성된 태그 정보 반환

### 5.3 태그 수정
- **Endpoint**: `POST /projects/{projectId}/tags/{tagId}/edit`
- **Request (TagCreateRequest)**:
  ```json
  {
    "name": "Updated Tag Name"
  }
  ```

### 5.4 태그 삭제
- **Endpoint**: `POST /projects/{projectId}/tags/{tagId}/delete`
- **Description**: 해당 태그를 삭제합니다.

---

## 6. Comment API (댓글 관리)

### 6.1 댓글 생성
- **Endpoint**: `POST /projects/{projectId}/tasks/{taskId}/comments`
- **Request (CommentCreateRequest)**:
  ```json
  {
    "content": "Comment Content"
  }
  ```

### 6.2 댓글 수정
- **Endpoint**: `POST /projects/{projectId}/tasks/{taskId}/comments/{commentId}/edit`
- **Request (CommentCreateRequest)**:
  ```json
  {
    "content": "Updated Comment Content"
  }
  ```

### 6.3 댓글 삭제
- **Endpoint**: `POST /projects/{projectId}/tasks/{taskId}/comments/{commentId}/delete`
