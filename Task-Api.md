# Task-Api API 명세서

## 1. Task-Api (`http://localhost:8082`)

프로젝트, 프로젝트 멤버, 태그, 마일스톤, 태스크, 댓글을 관리합니다.

Gateway가 로그인 사용자를 알고 있으므로 Task-Api는 매 요청마다 사용자 ID를 Header로 전달받는다고 가정합니다.

```http
X-User-Id: user1
```

Task-Api는 세션이나 로그인을 직접 확인하지 않고, 전달받은 `X-User-Id`로 권한을 판단합니다.

- `X-User-Id`가 이 프로젝트 멤버인가?
- `X-User-Id`가 프로젝트 관리자인가?
- `X-User-Id`가 댓글 작성자인가?

### 1.1 공통 규칙

#### Base URL

```http
/api
```

#### 공통 Header

| Header | 값 | 설명 |
| --- | --- | --- |
| `Content-Type` | `application/json` | JSON 요청 본문 |
| `X-User-Id` | `user1` | 모든 API에 필수입니다. Gateway가 전달하는 로그인 사용자 ID입니다. |

`X-User-Id`가 없거나 빈 값이면 `400 Bad Request`로 처리합니다.

#### 공통 Error Response

```json
{
  "status": 404,
  "code": "PROJECT_NOT_FOUND",
  "message": "프로젝트를 찾을 수 없습니다.",
  "path": "/api/projects/1"
}
```

#### 공통 Status Code

| Status Code | 의미 |
| --- | --- |
| 200 | 조회/수정 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 |
| 400 | 요청 값 오류 |
| 403 | 권한 없음 |
| 404 | 데이터 없음 |
| 409 | 중복 데이터 또는 삭제할 수 없는 데이터 |

#### 주요 Error Code

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

#### 프로젝트 상태 공통 규칙

| Project Status | 조회 API | 생성/수정/삭제 API |
| --- | --- | --- |
| `ACTIVE` | 가능 | 가능 |
| `DORMANT` | 가능 | 가능 |
| `TERMINATED` | 가능 | 불가 |

`TERMINATED` 상태의 프로젝트에서는 Project, Project Member, Tag, Milestone, Task, Comment의 생성/수정/삭제 요청을 처리하지 않고 `409 PROJECT_NOT_ACTIVE`를 응답합니다.

---

## 2. Project API

### 2.1 프로젝트 생성

- **[POST]** `/api/projects`
- **설명**: 프로젝트를 생성합니다. 생성자는 자동으로 프로젝트 관리자이며 동시에 프로젝트 멤버로 등록됩니다.
- **권한 규칙**: `X-User-Id` 사용자를 프로젝트 관리자와 멤버로 저장합니다.

#### Request Header

| Header | 값 |
| --- | --- |
| `X-User-Id` | `user1` |

#### Request Body

```json
{
  "name": "미니 두레이"
}
```

#### Response Body (201 Created)

```json
{
  "projectId": 1,
  "name": "미니 두레이",
  "status": "ACTIVE",
  "adminId": "user1",
  "createdAt": "2026-05-14T10:30:00",
  "updatedAt": "2026-05-14T10:30:00"
}
```

#### 서비스 규칙

| 순서 | 내용 |
| --- | --- |
| 1 | Project 생성 |
| 2 | 생성자를 `admin_id`로 저장 |
| 3 | 생성자를 `project_members`에도 자동 저장 |
| 4 | 생성된 프로젝트의 기본 상태는 `ACTIVE`입니다. |

### 2.2 내가 속한 프로젝트 목록 조회

- **[GET]** `/api/projects`
- **설명**: 로그인한 사용자가 멤버로 속한 프로젝트 목록을 조회합니다.
- **권한 규칙**: `X-User-Id`가 멤버인 프로젝트만 조회합니다.

#### Request Header

| Header | 값 |
| --- | --- |
| `X-User-Id` | `user1` |

#### Response Body (200 OK)

```json
[
  {
    "projectId": 1,
    "name": "미니 두레이",
    "status": "ACTIVE",
    "adminId": "user1"
  },
  {
    "projectId": 2,
    "name": "쇼핑몰 프로젝트",
    "status": "DORMANT",
    "adminId": "user2"
  }
]
```

### 2.3 프로젝트 단건 조회

- **[GET]** `/api/projects/{projectId}`
- **예시**: `/api/projects/1`
- **설명**: 프로젝트를 단건 조회합니다.
- **권한 규칙**: 프로젝트 멤버만 조회할 수 있습니다.

#### Response Body (200 OK)

```json
{
  "projectId": 1,
  "name": "미니 두레이",
  "status": "ACTIVE",
  "adminId": "user1",
  "createdAt": "2026-05-14T10:30:00",
  "updatedAt": "2026-05-14T10:30:00"
}
```

### 2.4 프로젝트 이름 수정

- **[PUT]** `/api/projects/{projectId}`
- **설명**: 프로젝트 이름을 수정합니다.
- **권한 규칙**: 프로젝트 관리자만 수정할 수 있습니다.

#### Request Body

```json
{
  "name": "미니 두레이 Final"
}
```

#### Response Body (200 OK)

```json
{
  "projectId": 1,
  "name": "미니 두레이 Final",
  "status": "ACTIVE",
  "adminId": "user1",
  "createdAt": "2026-05-14T10:30:00",
  "updatedAt": "2026-05-14T11:00:00"
}
```

### 2.5 프로젝트 상태 변경

- **[PATCH]** `/api/projects/{projectId}/status`
- **설명**: 프로젝트 상태를 변경합니다.
- **권한 규칙**: 프로젝트 관리자만 변경할 수 있습니다.

#### 가능한 Status

| Status | 설명 |
| --- | --- |
| `ACTIVE` | 활성 |
| `DORMANT` | 휴면 |
| `TERMINATED` | 종료 |

목록에 없는 상태 값이면 `400 INVALID_PROJECT_STATUS`를 응답합니다.

#### Request Body

```json
{
  "status": "TERMINATED"
}
```

#### Response Body (200 OK)

```json
{
  "projectId": 1,
  "name": "미니 두레이",
  "status": "TERMINATED",
  "adminId": "user1"
}
```

---

## 3. Project Member API

### 3.1 프로젝트 멤버 목록 조회

- **[GET]** `/api/projects/{projectId}/members`
- **설명**: 프로젝트 멤버 목록을 조회합니다.
- **권한 규칙**: 프로젝트 멤버만 조회할 수 있습니다.

#### Response Body (200 OK)

```json
[
  {
    "projectMemberId": 1,
    "projectId": 1,
    "userId": "user1"
  },
  {
    "projectMemberId": 2,
    "projectId": 1,
    "userId": "user2"
  }
]
```

### 3.2 프로젝트 멤버 추가

- **[POST]** `/api/projects/{projectId}/members`
- **설명**: 프로젝트에 멤버를 추가합니다.
- **권한 규칙**: 프로젝트 관리자만 추가할 수 있습니다.

#### Request Body

```json
{
  "userId": "user2"
}
```

#### Response Body (201 Created)

```json
{
  "projectMemberId": 2,
  "projectId": 1,
  "userId": "user2"
}
```

#### 주의

| 항목 | 내용 |
| --- | --- |
| 가입자 확인 책임 | Gateway가 Account-Api로 `userId` 존재 여부를 확인합니다. |
| Task-Api 책임 | Task-Api는 Account DB를 직접 조회하지 않습니다. |

#### 서비스 규칙

| 규칙 |
| --- |
| 같은 프로젝트에 이미 등록된 `userId`는 추가할 수 없습니다. |

### 3.3 프로젝트 멤버 삭제

- **[DELETE]** `/api/projects/{projectId}/members/{userId}`
- **예시**: `/api/projects/1/members/user2`
- **설명**: 프로젝트 멤버를 삭제합니다.
- **권한 규칙**: 프로젝트 관리자만 삭제할 수 있습니다.
- **Response**: 204 No Content

#### 서비스 규칙

| 규칙 |
| --- |
| 삭제 대상 `userId`가 해당 `projectId`의 멤버인지 확인합니다. |
| 프로젝트 관리자는 멤버에서 삭제할 수 없습니다. |

---

## 4. Tag API

### 4.1 태그 목록 조회

- **[GET]** `/api/projects/{projectId}/tags`
- **설명**: 프로젝트 태그 목록을 조회합니다.
- **권한 규칙**: 프로젝트 멤버만 조회할 수 있습니다.

#### Response Body (200 OK)

```json
[
  {
    "tagId": 1,
    "projectId": 1,
    "name": "백엔드"
  },
  {
    "tagId": 2,
    "projectId": 1,
    "name": "긴급"
  }
]
```

### 4.2 태그 생성

- **[POST]** `/api/projects/{projectId}/tags`
- **설명**: 프로젝트 태그를 생성합니다.
- **권한 규칙**: 프로젝트 멤버만 생성할 수 있습니다.
- **중복 방지**: 같은 프로젝트 안에서 태그 이름은 중복될 수 없습니다.
  이 중복 여부는 Task-Api의 Service 계층에서 검증합니다.
#### Request Body

```json
{
  "name": "백엔드"
}
```

#### Response Body (201 Created)

```json
{
  "tagId": 1,
  "projectId": 1,
  "name": "백엔드"
}
```

#### 서비스 규칙

| 규칙 |
| --- |
| 같은 프로젝트 안에서 태그 이름은 중복될 수 없습니다. |
| 중복된 태그 이름이면 `409 DUPLICATE_TAG_NAME`을 응답합니다. |

### 4.3 태그 수정

- **[PUT]** `/api/projects/{projectId}/tags/{tagId}`
- **설명**: 태그 이름을 수정합니다.
- **권한 규칙**: 프로젝트 멤버만 수정할 수 있습니다.
- **중복 방지**: 같은 프로젝트 안에서 태그 이름은 중복될 수 없습니다.
  이 중복 여부는 Task-Api의 Service 계층에서 검증합니다.
#### Request Body

```json
{
  "name": "서버"
}
```

#### Response Body (200 OK)

```json
{
  "tagId": 1,
  "projectId": 1,
  "name": "서버"
}
```

#### 서비스 규칙

| 규칙 |
| --- |
| `tagId`가 해당 `projectId` 소속인지 확인합니다. |
| 해당 태그가 요청한 프로젝트 소속이 아니면 `400 TAG_NOT_IN_PROJECT`를 응답합니다. |
| 같은 프로젝트 안에서 태그 이름은 중복될 수 없습니다. |
| 중복된 태그 이름이면 `409 DUPLICATE_TAG_NAME`을 응답합니다. |

### 4.4 태그 삭제

- **[DELETE]** `/api/projects/{projectId}/tags/{tagId}`
- **설명**: 태그를 삭제합니다.
- **권한 규칙**: 프로젝트 멤버만 삭제할 수 있습니다.
- **Response**: 204 No Content

#### 서비스 규칙

| 규칙 |
| --- |
| `tagId`가 해당 `projectId` 소속인지 확인합니다. |
| 해당 태그가 요청한 프로젝트 소속이 아니면 `400 TAG_NOT_IN_PROJECT`를 응답합니다. |
| 태그 삭제 시 해당 태그와 Task의 연결을 제거합니다. |
| `task_tags` 연결은 DB의 `ON DELETE CASCADE`로 자동 삭제됩니다. |

---

## 5. Milestone API

### 5.1 마일스톤 목록 조회

- **[GET]** `/api/projects/{projectId}/milestones`
- **설명**: 프로젝트 마일스톤 목록을 조회합니다.
- **권한 규칙**: 프로젝트 멤버만 조회할 수 있습니다.

#### Response Body (200 OK)

```json
[
  {
    "milestoneId": 1,
    "projectId": 1,
    "name": "1차 구현",
    "startDate": "2026-05-14",
    "endDate": "2026-05-20"
  }
]
```

### 5.2 마일스톤 생성

- **[POST]** `/api/projects/{projectId}/milestones`
- **설명**: 프로젝트 마일스톤을 생성합니다.
- **권한 규칙**: 프로젝트 멤버만 생성할 수 있습니다.

#### Request Body

```json
{
  "name": "1차 구현",
  "startDate": "2026-05-14",
  "endDate": "2026-05-20"
}
```

#### Response Body (201 Created)

```json
{
  "milestoneId": 1,
  "projectId": 1,
  "name": "1차 구현",
  "startDate": "2026-05-14",
  "endDate": "2026-05-20"
}
```

#### 서비스 규칙

| 규칙 |
| --- |
| `startDate`와 `endDate`가 모두 있으면 `startDate`는 `endDate`보다 늦을 수 없습니다. |
| 날짜 범위가 잘못되면 `400 INVALID_DATE_RANGE`를 응답합니다. |
| 같은 프로젝트 안에서 마일스톤 이름은 중복될 수 없습니다. |
| 중복된 마일스톤 이름이면 `409 DUPLICATE_MILESTONE_NAME`을 응답합니다. |

### 5.3 마일스톤 수정

- **[PUT]** `/api/projects/{projectId}/milestones/{milestoneId}`
- **설명**: 마일스톤을 수정합니다.
- **권한 규칙**: 프로젝트 멤버만 수정할 수 있습니다.

#### Request Body

```json
{
  "name": "1차 구현 수정",
  "startDate": "2026-05-15",
  "endDate": "2026-05-22"
}
```

#### Response Body (200 OK)

```json
{
  "milestoneId": 1,
  "projectId": 1,
  "name": "1차 구현 수정",
  "startDate": "2026-05-15",
  "endDate": "2026-05-22"
}
```

#### 서비스 규칙

| 규칙 |
| --- |
| `milestoneId`가 해당 `projectId` 소속인지 확인합니다. |
| 해당 마일스톤이 요청한 프로젝트 소속이 아니면 `400 MILESTONE_NOT_IN_PROJECT`를 응답합니다. |
| `startDate`와 `endDate`가 모두 있으면 `startDate`는 `endDate`보다 늦을 수 없습니다. |
| 날짜 범위가 잘못되면 `400 INVALID_DATE_RANGE`를 응답합니다. |
| 같은 프로젝트 안에서 마일스톤 이름은 중복될 수 없습니다. |
| 중복된 마일스톤 이름이면 `409 DUPLICATE_MILESTONE_NAME`을 응답합니다. |

### 5.4 마일스톤 삭제

- **[DELETE]** `/api/projects/{projectId}/milestones/{milestoneId}`
- **설명**: 마일스톤을 삭제합니다.
- **권한 규칙**: 프로젝트 멤버만 삭제할 수 있습니다.
- **Response**: 204 No Content

#### 서비스 규칙

| 규칙 |
| --- |
| `milestoneId`가 해당 `projectId` 소속인지 확인합니다. |
| 해당 마일스톤이 요청한 프로젝트 소속이 아니면 `400 MILESTONE_NOT_IN_PROJECT`를 응답합니다. |
| 마일스톤 삭제 시 `tasks.milestone_id`는 DB의 `ON DELETE SET NULL`로 자동 NULL 처리됩니다. |

---

## 6. Task API

### 6.1 프로젝트의 Task 목록 조회

- **[GET]** `/api/projects/{projectId}/tasks`
- **설명**: 프로젝트의 Task 목록을 조회합니다.
- **권한 규칙**: 프로젝트 멤버만 조회할 수 있습니다.

#### Response Body (200 OK)

```json
[
  {
    "taskId": 1,
    "projectId": 1,
    "title": "Task API Entity 작성",
    "content": "Project, Task, Comment Entity 작성",
    "writerId": "user1",
    "milestone": {
      "milestoneId": 1,
      "name": "1차 구현"
    },
    "tags": [
      {
        "tagId": 1,
        "name": "백엔드"
      },
      {
        "tagId": 2,
        "name": "긴급"
      }
    ],
    "createdAt": "2026-05-14T10:30:00",
    "updatedAt": "2026-05-14T10:30:00"
  }
]
```

### 6.2 Task 생성

- **[POST]** `/api/projects/{projectId}/tasks`
- **설명**: 프로젝트에 Task를 생성합니다.
- **권한 규칙**: 프로젝트 멤버만 생성할 수 있습니다.

#### Request Body

```json
{
  "title": "Task API Entity 작성",
  "content": "Project, Task, Comment Entity 작성",
  "milestoneId": 1,
  "tagIds": [1, 2]
}
```

#### Response Body (201 Created)

```json
{
  "taskId": 1,
  "projectId": 1,
  "title": "Task API Entity 작성",
  "content": "Project, Task, Comment Entity 작성",
  "writerId": "user1",
  "milestone": {
    "milestoneId": 1,
    "name": "1차 구현"
  },
  "tags": [
    {
      "tagId": 1,
      "name": "백엔드"
    },
    {
      "tagId": 2,
      "name": "긴급"
    }
  ],
  "createdAt": "2026-05-14T10:30:00",
  "updatedAt": "2026-05-14T10:30:00"
}
```

#### 서비스 규칙

| 순서 | 내용 |
| --- | --- |
| 1 | 요청자가 프로젝트 멤버인지 확인 |
| 2 | `title` 필수 |
| 3 | Task 생성 요청의 `tagIds`는 1개 이상 필수 |
| 3-1 | `tagIds`가 비어 있으면 `400 TASK_TAG_REQUIRED`를 응답 |
| 4 | `tagIds`의 태그들이 모두 해당 `projectId` 소속인지 확인 |
| 4-1 | 프로젝트 소속이 아닌 태그가 있으면 `400 TAG_NOT_IN_PROJECT`를 응답 |
| 5 | `milestoneId`가 있으면 해당 `projectId` 소속인지 확인 |
| 5-1 | 프로젝트 소속이 아닌 마일스톤이면 `400 MILESTONE_NOT_IN_PROJECT`를 응답 |
| 6 | Task 저장 |
| 7 | TaskTag 저장 |

### 6.3 Task 단건 조회

- **[GET]** `/api/projects/{projectId}/tasks/{taskId}`
- **설명**: Task를 단건 조회합니다.
- **권한 규칙**: 프로젝트 멤버만 조회할 수 있습니다.

#### Response Body (200 OK)

```json
{
  "taskId": 1,
  "projectId": 1,
  "title": "Task API Entity 작성",
  "content": "Project, Task, Comment Entity 작성",
  "writerId": "user1",
  "milestone": {
    "milestoneId": 1,
    "name": "1차 구현"
  },
  "tags": [
    {
      "tagId": 1,
      "name": "백엔드"
    },
    {
      "tagId": 2,
      "name": "긴급"
    }
  ],
  "comments": [
    {
      "commentId": 1,
      "writerId": "user2",
      "content": "확인했습니다.",
      "createdAt": "2026-05-14T11:00:00",
      "updatedAt": "2026-05-14T11:00:00"
    }
  ],
  "createdAt": "2026-05-14T10:30:00",
  "updatedAt": "2026-05-14T10:30:00"
}
```

#### 서비스 규칙

| 규칙 |
| --- |
| `taskId`가 해당 `projectId` 소속인지 확인합니다. |
| 해당 Task가 요청한 프로젝트 소속이 아니면 `400 TASK_NOT_IN_PROJECT`를 응답합니다. |

### 6.4 Task 수정

- **[PUT]** `/api/projects/{projectId}/tasks/{taskId}`
- **설명**: Task를 수정합니다.
- **권한 규칙**: 프로젝트 멤버만 수정할 수 있습니다.

#### Request Body

```json
{
  "title": "Task API Service 작성",
  "content": "Task 생성/수정/삭제 Service 구현",
  "milestoneId": 1,
  "tagIds": [1]
}
```

#### Response Body (200 OK)

```json
{
  "taskId": 1,
  "projectId": 1,
  "title": "Task API Service 작성",
  "content": "Task 생성/수정/삭제 Service 구현",
  "writerId": "user1",
  "milestone": {
    "milestoneId": 1,
    "name": "1차 구현"
  },
  "tags": [
    {
      "tagId": 1,
      "name": "백엔드"
    }
  ],
  "createdAt": "2026-05-14T10:30:00",
  "updatedAt": "2026-05-14T11:10:00"
}
```

#### 서비스 규칙

| 순서 | 내용 |
| --- | --- |
| 1 | 요청자가 프로젝트 멤버인지 확인 |
| 2 | `taskId`가 `projectId` 소속인지 확인 |
| 2-1 | 해당 Task가 요청한 프로젝트 소속이 아니면 `400 TASK_NOT_IN_PROJECT`를 응답 |
| 3 | Task 수정 요청의 `tagIds`는 1개 이상 필수 |
| 3-1 | `tagIds`가 비어 있으면 `400 TASK_TAG_REQUIRED`를 응답 |
| 4 | `tagIds`의 태그들이 모두 해당 `projectId` 소속인지 확인 |
| 4-1 | 프로젝트 소속이 아닌 태그가 있으면 `400 TAG_NOT_IN_PROJECT`를 응답 |
| 5 | `milestoneId`가 있으면 해당 `projectId` 소속인지 확인 |
| 5-1 | 프로젝트 소속이 아닌 마일스톤이면 `400 MILESTONE_NOT_IN_PROJECT`를 응답 |
| 6 | Task 수정 |
| 7 | 기존 TaskTag 삭제 후 새 TaskTag 저장 |

### 6.5 Task 삭제

- **[DELETE]** `/api/projects/{projectId}/tasks/{taskId}`
- **설명**: Task를 삭제합니다.
- **권한 규칙**: 프로젝트 멤버만 삭제할 수 있습니다.
- **Response**: 204 No Content

#### 서비스 규칙

| 규칙 |
| --- |
| `taskId`가 해당 `projectId` 소속인지 확인합니다. |
| 해당 Task가 요청한 프로젝트 소속이 아니면 `400 TASK_NOT_IN_PROJECT`를 응답합니다. |
| Task 삭제 시 `comments`, `task_tags`는 DB의 `ON DELETE CASCADE`로 자동 삭제됩니다. |

---

## 7. Comment API

### 7.1 댓글 목록 조회

- **[GET]** `/api/projects/{projectId}/tasks/{taskId}/comments`
- **설명**: Task 댓글 목록을 조회합니다.
- **권한 규칙**: 프로젝트 멤버만 조회할 수 있습니다.

#### Response Body (200 OK)

```json
[
  {
    "commentId": 1,
    "taskId": 1,
    "writerId": "user2",
    "content": "확인했습니다.",
    "createdAt": "2026-05-14T11:00:00",
    "updatedAt": "2026-05-14T11:00:00"
  }
]
```

#### 서비스 규칙

| 규칙 |
| --- |
| `taskId`가 해당 `projectId` 소속인지 확인합니다. |
| 해당 Task가 요청한 프로젝트 소속이 아니면 `400 TASK_NOT_IN_PROJECT`를 응답합니다. |

### 7.2 댓글 생성

- **[POST]** `/api/projects/{projectId}/tasks/{taskId}/comments`
- **설명**: Task에 댓글을 생성합니다.
- **권한 규칙**: 프로젝트 멤버만 생성할 수 있습니다.

#### Request Body

```json
{
  "content": "확인했습니다."
}
```

#### Response Body (201 Created)

```json
{
  "commentId": 1,
  "taskId": 1,
  "writerId": "user2",
  "content": "확인했습니다.",
  "createdAt": "2026-05-14T11:00:00",
  "updatedAt": "2026-05-14T11:00:00"
}
```

#### 서비스 규칙

| 규칙 |
| --- |
| `taskId`가 해당 `projectId` 소속인지 확인합니다. |
| 해당 Task가 요청한 프로젝트 소속이 아니면 `400 TASK_NOT_IN_PROJECT`를 응답합니다. |
| `content`는 필수입니다. |

### 7.3 댓글 수정

- **[PUT]** `/api/projects/{projectId}/tasks/{taskId}/comments/{commentId}`
- **설명**: 댓글 내용을 수정합니다.
- **권한 규칙**: 프로젝트 멤버이면서 댓글 작성자인 사용자만 수정할 수 있습니다.

#### Request Body

```json
{
  "content": "내용 수정했습니다."
}
```

#### Response Body (200 OK)

```json
{
  "commentId": 1,
  "taskId": 1,
  "writerId": "user2",
  "content": "내용 수정했습니다.",
  "createdAt": "2026-05-14T11:00:00",
  "updatedAt": "2026-05-14T11:10:00"
}
```

#### 서비스 규칙

| 순서 | 내용 |
| --- | --- |
| 1 | `taskId`가 해당 `projectId` 소속인지 확인 |
| 1-1 | 해당 Task가 요청한 프로젝트 소속이 아니면 `400 TASK_NOT_IN_PROJECT`를 응답 |
| 2 | `commentId`가 해당 `taskId` 소속인지 확인 |
| 2-1 | 해당 댓글이 요청한 Task 소속이 아니면 `400 COMMENT_NOT_IN_TASK`를 응답 |
| 3 | 요청자가 댓글 작성자인지 확인 |
| 4 | `content`는 필수 |
| 5 | 댓글 수정 |

### 7.4 댓글 삭제

- **[DELETE]** `/api/projects/{projectId}/tasks/{taskId}/comments/{commentId}`
- **설명**: 댓글을 삭제합니다.
- **권한 규칙**: 프로젝트 멤버이면서 댓글 작성자인 사용자만 삭제할 수 있습니다.
- **Response**: 204 No Content

#### 서비스 규칙

| 순서 | 내용 |
| --- | --- |
| 1 | `taskId`가 해당 `projectId` 소속인지 확인 |
| 1-1 | 해당 Task가 요청한 프로젝트 소속이 아니면 `400 TASK_NOT_IN_PROJECT`를 응답 |
| 2 | `commentId`가 해당 `taskId` 소속인지 확인 |
| 2-1 | 해당 댓글이 요청한 Task 소속이 아니면 `400 COMMENT_NOT_IN_TASK`를 응답 |
| 3 | 요청자가 댓글 작성자인지 확인 |
| 4 | 댓글 삭제 |
