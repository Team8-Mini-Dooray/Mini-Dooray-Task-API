## 4. Milestone API (마일스톤 관리)

### 4.1 마일스톤 생성
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

### 4.2 마일스톤 수정
- **Endpoint**: `POST /projects/{projectId}/milestones/{milestoneId}/edit`
- **Request (MilestoneCreateRequest)**:
  ```json
  {
    "name": "Updated Sprint Name",
    "startDate": "2023-10-02",
    "endDate": "2023-10-16"
  }
  ```

### 4.3 마일스톤 삭제
- **Endpoint**: `POST /projects/{projectId}/milestones/{milestoneId}/delete`
- **Description**: 해당 마일스톤을 삭제합니다.