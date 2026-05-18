- **Endpoint**: `GET /projects/{projectId}/tasks`
- **Request Parameters**:
  - `projectId` (Path Variable, Long): 프로젝트 식별자 (필수)
  - `tagId` (Query Parameter, Long): 필터링할 태그 식별자 (선택)
- **Response (List<TaskDto>)**:
  ```json
  [
    {
      "taskId": 1,
      "milestoneId": 10,
      "title": "업무 제목",
      "content": "업무 상세 내용",
      "writerId": "user123",
      "createdAt": "2023-10-27T10:00:00",
      "tags": [
        {
          "tagId": 5,
          "name": "Backend"
        }
      ]
    }
  ]
  ```
- **Description**: 프로젝트에 속한 업무 목록을 JSON으로 반환합니다. `tagId`가 제공되면 해당 태그가 포함된 업무만 필터링하여 반환하고, 파라미터가 없으면 프로젝트의 전체 업무 목록을 반환합니다.