# Task-Api API 명세서

## 1. 개요

Task-Api는 Project, Project Member, Task, Tag, Milestone, Comment 정보를 관리하는 REST API입니다.

Gateway가 인증을 담당하므로 Task-Api는 로그인 세션을 직접 확인하지 않습니다. Gateway는 모든 Task-Api 요청에 로그인 사용자 ID를 Header로 전달합니다.

```http
X-User-Id: user1
```

Task-Api는 `X-User-Id`를 기준으로 프로젝트 멤버 여부, 프로젝트 관리자 여부, 댓글 작성자 여부를 판단합니다.

---

## 2. 공통 규칙

### 2.1 Base URL

Gateway 요청 경로 기준으로 작성합니다.

### 2.2 공통 Header

| Header | 필수 | 설명 |
| --- | --- | --- |
| `Content-Type: application/json` | 요청 Body가 있는 경우 필수 | JSON 요청 |
| `X-User-Id` | 필수 | Gateway가 전달하는 로그인 사용자 ID |

### 2.3 공통 Error Response

```json
{
  "status": 404,
  "code": "PROJECT_NOT_FOUND",
  "message": "프로젝트를 찾을 수 없습니다.",
  "path": "/projects/1"
}
```

### 2.4 공통 Status Code

| Status Code | 의미 |
| --- | --- |
| 200 | 조회/수정 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 |
| 400 | 요청 값 오류 |
| 403 | 권한 없음 |
| 404 | 데이터 없음 |
| 409 | 중복 데이터 또는 현재 상태와 충돌하는 요청 |

### 2.5 Error Code

| Error Code | Status | 의미 |
| --- | --- | --- |
| `MISSING_USER_ID` | 400 | `X-User-Id` Header가 없거나 빈 값입니다. |
| `INVALID_REQUEST` | 400 | 요청 본문 값이 유효하지 않습니다. |
| `TASK_TAG_REQUIRED` | 400 | Task는 1개 이상의 Tag가 필요합니다. |
| `INVALID_PROJECT_STATUS` | 400 | 사용할 수 없는 프로젝트 상태입니다. |
| `INVALID_DATE_RANGE` | 400 | 시작일은 종료일보다 늦을 수 없습니다. |
| `TAG_NOT_IN_PROJECT` | 400 | 해당 태그가 요청한 프로젝트 소속이 아닙니다. |
| `MILESTONE_NOT_IN_PROJECT` | 400 | 해당 마일스톤이 요청한 프로젝트 소속이 아닙니다. |
| `TASK_NOT_IN_PROJECT` | 400 | 해당 Task가 요청한 프로젝트 소속이 아닙니다. |
| `COMMENT_NOT_IN_TASK` | 400 | 해당 댓글이 요청한 Task 소속이 아닙니다. |
| `PROJECT_NOT_FOUND` | 404 | 프로젝트를 찾을 수 없습니다. |
| `PROJECT_MEMBER_NOT_FOUND` | 404 | 프로젝트 멤버를 찾을 수 없습니다. |
| `TAG_NOT_FOUND` | 404 | 태그를 찾을 수 없습니다. |
| `MILESTONE_NOT_FOUND` | 404 | 마일스톤을 찾을 수 없습니다. |
| `TASK_NOT_FOUND` | 404 | Task를 찾을 수 없습니다. |
| `COMMENT_NOT_FOUND` | 404 | 댓글을 찾을 수 없습니다. |
| `NOT_PROJECT_MEMBER` | 403 | 요청자가 프로젝트 멤버가 아닙니다. |
| `NOT_PROJECT_ADMIN` | 403 | 요청자가 프로젝트 관리자가 아닙니다. |
| `NOT_COMMENT_WRITER` | 403 | 요청자가 댓글 작성자가 아닙니다. |
| `DUPLICATE_PROJECT_MEMBER` | 409 | 이미 등록된 프로젝트 멤버입니다. |
| `DUPLICATE_TAG_NAME` | 409 | 같은 프로젝트에 동일한 태그 이름이 존재합니다. |
| `DUPLICATE_MILESTONE_NAME` | 409 | 같은 프로젝트에 동일한 마일스톤 이름이 존재합니다. |
| `PROJECT_NOT_ACTIVE` | 409 | 종료 상태의 프로젝트에서는 생성/수정/삭제 작업을 할 수 없습니다. |
| `ADMIN_MEMBER_CANNOT_BE_REMOVED` | 409 | 프로젝트 관리자는 멤버에서 삭제할 수 없습니다. |

### 2.6 프로젝트 상태 규칙

| Project Status | 조회 | 생성/수정/삭제 |
| --- | --- | --- |
| `ACTIVE` | 가능 | 가능 |
| `DORMANT` | 가능 | 가능 |
| `TERMINATED` | 가능 | 불가 |

`TERMINATED` 상태의 프로젝트에서는 조회만 가능합니다. Project, Project Member, Tag, Milestone, Task, Comment의 생성/수정/삭제 요청은 `409 PROJECT_NOT_ACTIVE`로 응답합니다.

### 2.7 삭제 요청 규칙

Gateway의 HTML form 요청을 고려하여 삭제 요청은 `POST /delete` 형태로 통일합니다.

삭제 성공 시 `204 No Content`를 반환합니다.

---

## 3. DTO 목록

### 3.1 Request DTO

| DTO | 필드 | 사용 API |
| --- | --- | --- |
| `ProjectCreateRequest` | `name` | 프로젝트 생성 |
| `ProjectUpdateRequest` | `name`, `status` | 프로젝트 수정, 상태 변경 |
| `ProjectMemberRequest` | `userId` | 프로젝트 멤버 추가 |
| `TagCreateRequest` | `name` | 태그 생성, 태그 수정 |
| `MilestoneCreateRequest` | `name`, `startDate`, `endDate` | 마일스톤 생성, 마일스톤 수정 |
| `TaskCreateRequest` | `taskId`, `projectId`, `title`, `content`, `writerId`, `createdAt` | Task 생성 |
| `TaskUpdateRequest` | `title`, `content` | Task 수정 |
| `TaskMilestoneRequest` | `milestoneId` | Task 마일스톤 설정 |
| `TaskTagRequest` | `tagIds` | Task 태그 설정 |
| `CommentCreateRequest` | `content` | 댓글 생성, 댓글 수정 |

### 3.2 Response DTO

| DTO | 필드 | 사용 API |
| --- | --- | --- |
| `ProjectDto` | `projectId`, `name`, `status` | 프로젝트 목록, 생성, 수정, 상태 변경 |
| `ProjectDetailDto` | `projectId`, `name`, `status`, `members`, `tasks`, `milestones` | 프로젝트 상세 |
| `ProjectMemberDto` | `userId` | 프로젝트 멤버 목록 |
| `TaskDto` | `taskId`, `title`, `content`, `writerId`, `createdAt` | Task 생성, 수정, 목록 구성 |
| `TaskDetailDto` | `taskId`, `title`, `content`, `writerId`, `createdAt`, `comments` | Task 상세 |
| `MilestoneDto` | `milestoneId`, `name`, `startDate`, `endDate` | 마일스톤 조회, 생성, 수정 |
| `MilestoneDetailDto` | `milestoneId`, `name`, `startDate`, `endDate`, `tasks` | 마일스톤 상세 |
| `TagDto` | `tagId`, `name` | 태그 목록, 생성, 수정 |

> Task 상세 응답의 `comments`는 댓글 조회용 DTO가 필요합니다. 별도 `CommentDto`를 만들거나 `TaskDetailDto` 내부 응답 타입으로 정의해야 합니다.

---

## 4. Project API

### 4.1 프로젝트 목록 조회

- **Method URL**: `GET /projects`
- **설명**: 로그인 사용자가 멤버로 속한 프로젝트 목록을 조회합니다.
- **Request DTO**: 없음
- **Response DTO**: `List<ProjectDto>`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Request</strong></summary>

- 없음

</details>

<details>
<summary><strong>Response</strong></summary>

```json
[
  {
    "projectId": 1,
    "name": "Project A",
    "status": "ACTIVE"
  }
]
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `MISSING_USER_ID` (400): `X-User-Id` Header가 없습니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. `X-User-Id`가 멤버로 등록된 프로젝트만 응답합니다.

</details>

### 4.2 프로젝트 상세 조회

- **Method URL**: `GET /projects/{projectId}`
- **설명**: 프로젝트 상세 정보, 멤버, Task 목록, 마일스톤 목록을 조회합니다.
- **Request DTO**: 없음
- **Response DTO**: `ProjectDetailDto`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Request</strong></summary>

- Path Variable
  - `projectId` (long): 프로젝트 ID

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "projectId": 1,
  "name": "Project A",
  "status": "ACTIVE",
  "members": [
    {
      "userId": "user123"
    }
  ],
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

</details>

<details>
<summary><strong>Error</strong></summary>

- `PROJECT_NOT_FOUND` (404): 프로젝트를 찾을 수 없습니다.
- `NOT_PROJECT_MEMBER` (403): 요청자가 프로젝트 멤버가 아닙니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. 프로젝트 멤버만 조회할 수 있습니다.

</details>

### 4.3 프로젝트 생성

- **Method URL**: `POST /projects`
- **설명**: 프로젝트를 생성합니다. 생성자는 프로젝트 관리자이며 동시에 프로젝트 멤버로 등록됩니다.
- **Request DTO**: `ProjectCreateRequest`
- **Response DTO**: `ProjectDto`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Request</strong></summary>

```json
{
  "name": "New Project"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "projectId": 1,
  "name": "New Project",
  "status": "ACTIVE"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `INVALID_REQUEST` (400): 프로젝트 이름이 유효하지 않습니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. 생성자를 프로젝트 관리자(`admin_id`)로 저장합니다.
2. 생성자를 `project_members`에 자동 등록합니다.
3. 기본 상태는 `ACTIVE`입니다.

</details>

### 4.4 프로젝트 수정

- **Method URL**: `POST /projects/{projectId}/edit`
- **설명**: 프로젝트 이름과 상태를 수정합니다.
- **Request DTO**: `ProjectUpdateRequest`
- **Response DTO**: `ProjectDto`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Request</strong></summary>

```json
{
  "name": "Updated Name",
  "status": "DORMANT"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "projectId": 1,
  "name": "Updated Name",
  "status": "DORMANT"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `PROJECT_NOT_FOUND` (404): 프로젝트를 찾을 수 없습니다.
- `NOT_PROJECT_ADMIN` (403): 요청자가 프로젝트 관리자가 아닙니다.
- `INVALID_PROJECT_STATUS` (400): 사용할 수 없는 프로젝트 상태입니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트는 수정할 수 없습니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. 프로젝트 관리자만 수정할 수 있습니다.

</details>

### 4.5 프로젝트 종료

- **Method URL**: `POST /projects/{projectId}/close`
- **설명**: 프로젝트 상태를 변경합니다. 프로젝트 종료는 `status=TERMINATED`로 처리합니다.
- **Request DTO**: `ProjectUpdateRequest`
- **Response DTO**: `ProjectDto`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Request</strong></summary>

```json
{
  "name": "Project A",
  "status": "TERMINATED"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "projectId": 1,
  "name": "Project A",
  "status": "TERMINATED"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `PROJECT_NOT_FOUND` (404): 프로젝트를 찾을 수 없습니다.
- `NOT_PROJECT_ADMIN` (403): 요청자가 프로젝트 관리자가 아닙니다.
- `INVALID_PROJECT_STATUS` (400): 사용할 수 없는 프로젝트 상태입니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. 프로젝트 관리자만 상태를 변경할 수 있습니다.
2. `ProjectUpdateRequest.status` 주석에는 `CLOSED`가 남아 있지만 종료 상태는 `TERMINATED`로 통일합니다.

</details>

---

## 5. Project Member API

### 5.1 프로젝트 멤버 목록 조회

- **Method URL**: `GET /projects/{projectId}/members`
- **설명**: 프로젝트 멤버 목록을 조회합니다.
- **Request DTO**: 없음
- **Response DTO**: `List<ProjectMemberDto>`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Response</strong></summary>

```json
[
  {
    "userId": "user1"
  },
  {
    "userId": "user2"
  }
]
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `PROJECT_NOT_FOUND` (404): 프로젝트를 찾을 수 없습니다.
- `NOT_PROJECT_MEMBER` (403): 요청자가 프로젝트 멤버가 아닙니다.

</details>

### 5.2 프로젝트 멤버 추가

- **Method URL**: `POST /projects/{projectId}/members`
- **설명**: 프로젝트에 멤버를 추가합니다.
- **Request DTO**: `ProjectMemberRequest`
- **Response DTO**: `ProjectMemberDto`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Request</strong></summary>

```json
{
  "userId": "user2"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "userId": "user2"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `NOT_PROJECT_ADMIN` (403): 요청자가 프로젝트 관리자가 아닙니다.
- `DUPLICATE_PROJECT_MEMBER` (409): 이미 등록된 프로젝트 멤버입니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 멤버를 추가할 수 없습니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. 프로젝트 관리자만 추가할 수 있습니다.
2. 추가하려는 `userId`가 실제 가입자인지 확인하는 책임은 Gateway에 있습니다.
3. Task-Api는 Account DB를 직접 조회하지 않습니다.

</details>

### 5.3 프로젝트 멤버 삭제

- **Method URL**: `DELETE /projects/{projectId}/members/{userId}`
- **설명**: 프로젝트 멤버를 삭제합니다.
- **Request DTO**: 없음
- **Response DTO**: 없음

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Response</strong></summary>

`204 No Content`

</details>

<details>
<summary><strong>Error</strong></summary>

- `PROJECT_MEMBER_NOT_FOUND` (404): 프로젝트 멤버를 찾을 수 없습니다.
- `NOT_PROJECT_ADMIN` (403): 요청자가 프로젝트 관리자가 아닙니다.
- `ADMIN_MEMBER_CANNOT_BE_REMOVED` (409): 프로젝트 관리자는 멤버에서 삭제할 수 없습니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 멤버를 삭제할 수 없습니다.

</details>

---

## 6. Task API

Task 전용 목록 API는 별도로 제공하지 않습니다. 프로젝트 상세 조회(`ProjectDetailDto`)에 포함된 `tasks`로 프로젝트의 Task 목록을 제공합니다.

구현 시 Project 상세 DTO 조립을 위해 `TaskRepository.findByProject_ProjectId(projectId)` 같은 조회 메서드가 필요할 수 있습니다.

### 6.1 Task 상세 조회

- **Method URL**: `GET /projects/{projectId}/tasks/{taskId}`
- **설명**: Task 상세 정보를 조회합니다.
- **Request DTO**: 없음
- **Response DTO**: `TaskDetailDto`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user1`

</details>

<details>
<summary><strong>Response</strong></summary>

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

</details>

<details>
<summary><strong>Error</strong></summary>

- `TASK_NOT_FOUND` (404): Task를 찾을 수 없습니다.
- `TASK_NOT_IN_PROJECT` (400): 해당 Task가 요청한 프로젝트 소속이 아닙니다.
- `NOT_PROJECT_MEMBER` (403): 요청자가 프로젝트 멤버가 아닙니다.

</details>

### 6.2 Task 생성

- **Method URL**: `POST /projects/{projectId}/tasks`
- **설명**: 프로젝트에 Task를 생성합니다.
- **Request DTO**: `TaskCreateRequest`
- **Response DTO**: `TaskDto`

<details>
<summary><strong>Header</strong></summary>

- `X-User-Id` (string): `user123`

</details>

<details>
<summary><strong>Request</strong></summary>

```json
{
  "projectId": 1,
  "title": "New Task",
  "content": "Task Content",
  "writerId": "user123"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "taskId": 1,
  "title": "New Task",
  "content": "Task Content",
  "writerId": "user123",
  "createdAt": "2023-10-27T10:00:00"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `NOT_PROJECT_MEMBER` (403): 요청자가 프로젝트 멤버가 아닙니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 Task를 생성할 수 없습니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. Task 생성 시 저장되는 `writerId`는 `request.writerId()`가 아니라 Header의 `X-User-Id`입니다.
2. `TaskCreateRequest`의 `taskId`, `createdAt`은 생성 시 서버에서 결정되는 값입니다.
3. Tag는 `TaskTagRequest` API로 별도 설정합니다.
4. Milestone은 `TaskMilestoneRequest` API로 별도 설정합니다.

</details>

### 6.3 Task 수정

- **Method URL**: `POST /projects/{projectId}/tasks/{taskId}/edit`
- **설명**: Task 제목과 내용을 수정합니다.
- **Request DTO**: `TaskUpdateRequest`
- **Response DTO**: `TaskDto`

<details>
<summary><strong>Request</strong></summary>

```json
{
  "title": "Updated Title",
  "content": "Updated Content"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "taskId": 1,
  "title": "Updated Title",
  "content": "Updated Content",
  "writerId": "user123",
  "createdAt": "2023-10-27T10:00:00"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `TASK_NOT_FOUND` (404): Task를 찾을 수 없습니다.
- `TASK_NOT_IN_PROJECT` (400): 해당 Task가 요청한 프로젝트 소속이 아닙니다.
- `NOT_PROJECT_MEMBER` (403): 요청자가 프로젝트 멤버가 아닙니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 Task를 수정할 수 없습니다.

</details>

### 6.4 Task 삭제

- **Method URL**: `POST /projects/{projectId}/tasks/{taskId}/delete`
- **설명**: Task를 삭제합니다.
- **Request DTO**: 없음
- **Response DTO**: 없음

<details>
<summary><strong>Response</strong></summary>

`204 No Content`

</details>

<details>
<summary><strong>예외</strong></summary>

1. Task 삭제 시 `comments`, `task_tags`는 DB의 `ON DELETE CASCADE`로 자동 삭제됩니다.
2. 삭제 성공 시 `204 No Content`를 반환합니다.

</details>

### 6.5 Task 마일스톤 설정

- **Method URL**: `POST /projects/{projectId}/tasks/{taskId}/milestones`
- **설명**: Task에 마일스톤을 설정합니다.
- **Request DTO**: `TaskMilestoneRequest`
- **Response DTO**: `TaskDto`

<details>
<summary><strong>Request</strong></summary>

```json
{
  "milestoneId": 2
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `MILESTONE_NOT_IN_PROJECT` (400): 해당 마일스톤이 요청한 프로젝트 소속이 아닙니다.
- `TASK_NOT_IN_PROJECT` (400): 해당 Task가 요청한 프로젝트 소속이 아닙니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 Task를 수정할 수 없습니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. Task 마일스톤 설정은 기존 `milestoneId` 값을 요청한 `milestoneId`로 교체합니다.
2. `milestoneId`가 `null`이면 Task의 마일스톤을 제거하는 방식으로 처리할 수 있습니다.

</details>

### 6.6 Task 태그 설정

- **Method URL**: `POST /projects/{projectId}/tasks/{taskId}/tags`
- **설명**: Task에 Tag를 설정합니다.
- **Request DTO**: `TaskTagRequest`
- **Response DTO**: `TaskDto`

<details>
<summary><strong>Request</strong></summary>

```json
{
  "tagIds": [1, 2, 5]
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `TASK_TAG_REQUIRED` (400): Task는 1개 이상의 Tag가 필요합니다.
- `TAG_NOT_IN_PROJECT` (400): 해당 태그가 요청한 프로젝트 소속이 아닙니다.
- `TASK_NOT_IN_PROJECT` (400): 해당 Task가 요청한 프로젝트 소속이 아닙니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 Task를 수정할 수 없습니다.

</details>

<details>
<summary><strong>예외</strong></summary>

1. Task 태그 설정은 기존 TaskTag 목록을 삭제한 뒤 요청한 `tagIds` 기준으로 다시 저장하는 전체 교체 방식입니다.
2. `tagIds`는 1개 이상이어야 합니다.

</details>

---

## 7. Milestone API

### 7.1 마일스톤 목록 조회

- **Method URL**: `GET /projects/{projectId}/milestones`
- **설명**: 프로젝트 마일스톤 목록을 조회합니다.
- **Request DTO**: 없음
- **Response DTO**: `List<MilestoneDto>`

<details>
<summary><strong>Response</strong></summary>

```json
[
  {
    "milestoneId": 1,
    "name": "Sprint 1",
    "startDate": "2023-10-01",
    "endDate": "2023-10-15"
  }
]
```

</details>

### 7.2 마일스톤 상세 조회

- **Method URL**: `GET /projects/{projectId}/milestones/{milestoneId}`
- **설명**: 마일스톤 상세 정보와 해당 마일스톤에 연결된 Task 목록을 조회합니다.
- **Request DTO**: 없음
- **Response DTO**: `MilestoneDetailDto`

<details>
<summary><strong>Response</strong></summary>

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

</details>

<details>
<summary><strong>Error</strong></summary>

- `MILESTONE_NOT_FOUND` (404): 마일스톤을 찾을 수 없습니다.
- `MILESTONE_NOT_IN_PROJECT` (400): 해당 마일스톤이 요청한 프로젝트 소속이 아닙니다.
- `NOT_PROJECT_MEMBER` (403): 요청자가 프로젝트 멤버가 아닙니다.

</details>

### 7.3 마일스톤 생성

- **Method URL**: `POST /projects/{projectId}/milestones`
- **설명**: 프로젝트 마일스톤을 생성합니다.
- **Request DTO**: `MilestoneCreateRequest`
- **Response DTO**: `MilestoneDto`

<details>
<summary><strong>Request</strong></summary>

```json
{
  "name": "Sprint 1",
  "startDate": "2023-10-01",
  "endDate": "2023-10-15"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "milestoneId": 1,
  "name": "Sprint 1",
  "startDate": "2023-10-01",
  "endDate": "2023-10-15"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `INVALID_DATE_RANGE` (400): 시작일은 종료일보다 늦을 수 없습니다.
- `DUPLICATE_MILESTONE_NAME` (409): 같은 프로젝트에 동일한 마일스톤 이름이 존재합니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 마일스톤을 생성할 수 없습니다.

</details>

### 7.4 마일스톤 수정

- **Method URL**: `POST /projects/{projectId}/milestones/{milestoneId}/edit`
- **설명**: 마일스톤을 수정합니다.
- **Request DTO**: `MilestoneCreateRequest`
- **Response DTO**: `MilestoneDto`

<details>
<summary><strong>Request</strong></summary>

```json
{
  "name": "Updated Sprint Name",
  "startDate": "2023-10-02",
  "endDate": "2023-10-16"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "milestoneId": 1,
  "name": "Updated Sprint Name",
  "startDate": "2023-10-02",
  "endDate": "2023-10-16"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `MILESTONE_NOT_FOUND` (404): 마일스톤을 찾을 수 없습니다.
- `MILESTONE_NOT_IN_PROJECT` (400): 해당 마일스톤이 요청한 프로젝트 소속이 아닙니다.
- `INVALID_DATE_RANGE` (400): 시작일은 종료일보다 늦을 수 없습니다.
- `DUPLICATE_MILESTONE_NAME` (409): 같은 프로젝트에 동일한 마일스톤 이름이 존재합니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 마일스톤을 수정할 수 없습니다.

</details>

### 7.5 마일스톤 삭제

- **Method URL**: `POST /projects/{projectId}/milestones/{milestoneId}/delete`
- **설명**: 해당 마일스톤을 삭제합니다.
- **Request DTO**: 없음
- **Response DTO**: 없음

<details>
<summary><strong>예외</strong></summary>

1. 마일스톤 삭제 시 `tasks.milestone_id`는 DB의 `ON DELETE SET NULL`로 자동 NULL 처리됩니다.

</details>

---

## 8. Tag API

### 8.1 태그 목록 조회

- **Method URL**: `GET /projects/{projectId}/tags`
- **설명**: 프로젝트 태그 목록을 조회합니다.
- **Request DTO**: 없음
- **Response DTO**: `List<TagDto>`

<details>
<summary><strong>Response</strong></summary>

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

</details>

### 8.2 태그 생성

- **Method URL**: `POST /projects/{projectId}/tags`
- **설명**: 프로젝트 태그를 생성합니다.
- **Request DTO**: `TagCreateRequest`
- **Response DTO**: `TagDto`

<details>
<summary><strong>Request</strong></summary>

```json
{
  "name": "New Tag"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "tagId": 1,
  "name": "New Tag"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `DUPLICATE_TAG_NAME` (409): 같은 프로젝트에 동일한 태그 이름이 존재합니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 태그를 생성할 수 없습니다.

</details>

### 8.3 태그 수정

- **Method URL**: `POST /projects/{projectId}/tags/{tagId}/edit`
- **설명**: 태그 이름을 수정합니다.
- **Request DTO**: `TagCreateRequest`
- **Response DTO**: `TagDto`

<details>
<summary><strong>Request</strong></summary>

```json
{
  "name": "Updated Tag Name"
}
```

</details>

<details>
<summary><strong>Response</strong></summary>

```json
{
  "tagId": 1,
  "name": "Updated Tag Name"
}
```

</details>

<details>
<summary><strong>Error</strong></summary>

- `TAG_NOT_FOUND` (404): 태그를 찾을 수 없습니다.
- `TAG_NOT_IN_PROJECT` (400): 해당 태그가 요청한 프로젝트 소속이 아닙니다.
- `DUPLICATE_TAG_NAME` (409): 같은 프로젝트에 동일한 태그 이름이 존재합니다.
- `PROJECT_NOT_ACTIVE` (409): 종료 상태의 프로젝트에서는 태그를 수정할 수 없습니다.

</details>

### 8.4 태그 삭제

- **Method URL**: `POST /projects/{projectId}/tags/{tagId}/delete`
- **설명**: 해당 태그를 삭제합니다.
- **Request DTO**: 없음
- **Response DTO**: 없음

<details>
<summary><strong>예외</strong></summary>

1. 태그 삭제 시 해당 태그와 Task의 연결을 제거합니다.
2. `task_tags` 연결은 DB의 `ON DELETE CASCADE`로 자동 삭제됩니다.

</details>

---

## 9. Comment API

### 9.1 댓글 생성

- **Method URL**: `POST /projects/{projectId}/tasks/{taskId}/comments`
- **설명**: Task에 댓글을 생성합니다.
- **Request DTO**: `CommentCreateRequest`
- **Response DTO**: 없음

<details>
<summary><strong>Request</strong></summary>

```json
{
  "content": "Comment Content"
}
```

</details>

### 9.2 댓글 수정

- **Method URL**: `POST /projects/{projectId}/tasks/{taskId}/comments/{commentId}/edit`
- **설명**: 댓글 내용을 수정합니다.
- **Request DTO**: `CommentCreateRequest`
- **Response DTO**: 없음

<details>
<summary><strong>Request</strong></summary>

```json
{
  "content": "Updated Comment Content"
}
```

</details>

<details>
<summary><strong>예외</strong></summary>

1. 댓글 작성자만 수정할 수 있습니다.
2. `commentId`가 해당 `taskId` 소속인지 확인합니다.

</details>

### 9.3 댓글 삭제

- **Method URL**: `POST /projects/{projectId}/tasks/{taskId}/comments/{commentId}/delete`
- **설명**: 댓글을 삭제합니다.
- **Request DTO**: 없음
- **Response DTO**: 없음

<details>
<summary><strong>예외</strong></summary>

1. 댓글 작성자만 삭제할 수 있습니다.
2. `commentId`가 해당 `taskId` 소속인지 확인합니다.

</details>
