# REST API

## REST란?

**RE**presentational **S**tate **T**ransfer

- 자원(Resource)을 **URI로 식별**하고, HTTP 메서드로 **행위를 표현**하는 아키텍처 스타일
- Roy Fielding의 2000년 박사 논문에서 제안

---

## REST 6가지 원칙

| 원칙 | 설명 |
|------|------|
| Client-Server | 클라이언트와 서버의 역할 분리 |
| Stateless | 서버가 클라이언트 상태를 저장하지 않음 |
| Cacheable | 응답은 캐시 가능 여부를 명시해야 함 |
| Uniform Interface | 일관된 인터페이스 (URI, HTTP 메서드) |
| Layered System | 클라이언트는 서버의 내부 구조를 모름 |
| Code on Demand | (선택) 서버가 클라이언트에 코드를 전송할 수 있음 |

---

## RESTful API 설계 규칙

### 1. URI는 명사, 복수형

```
✅ GET    /users          - 유저 목록 조회
✅ GET    /users/1        - 유저 1번 조회
✅ POST   /users          - 유저 생성
✅ PUT    /users/1        - 유저 1번 수정
✅ DELETE /users/1        - 유저 1번 삭제

❌ GET    /getUser        - 동사 사용 금지
❌ GET    /user           - 단수 사용 지양
❌ POST   /createUser     - 행위를 URI에 넣지 않음
```

### 2. 계층 관계는 슬래시로 표현

```
✅ GET /users/1/posts         - 유저 1의 게시글 목록
✅ GET /users/1/posts/5       - 유저 1의 게시글 5번
✅ GET /posts/5/comments      - 게시글 5의 댓글 목록

❌ GET /getUserPosts?userId=1 - 행위를 URI에 넣지 않음
```

### 3. 필터링은 쿼리 파라미터

```
GET /users?page=1&size=20           - 페이징
GET /users?sort=name&order=asc      - 정렬
GET /posts?category=tech&status=published  - 필터링
GET /users?search=john              - 검색
```

### 4. HTTP 메서드 + 상태 코드를 올바르게 사용

| 동작 | 메서드 | 성공 코드 | 예시 |
|------|--------|----------|------|
| 목록 조회 | GET | 200 | GET /users |
| 단건 조회 | GET | 200 / 404 | GET /users/1 |
| 생성 | POST | **201** | POST /users |
| 전체 수정 | PUT | 200 | PUT /users/1 |
| 부분 수정 | PATCH | 200 | PATCH /users/1 |
| 삭제 | DELETE | **204** | DELETE /users/1 |

### 5. URI 규칙

```
✅ 소문자 사용          /users (O)  /Users (X)
✅ 하이픈 사용          /blog-posts (O)
❌ 언더스코어 사용 금지   /blog_posts (X)
❌ 마지막 슬래시 없음    /users (O)  /users/ (X)
❌ 파일 확장자 없음      /users (O)  /users.json (X)
```

---

## 실전 API 설계 예시

### 블로그 API

```
# 게시글
GET    /posts                 - 게시글 목록
GET    /posts/:id             - 게시글 상세
POST   /posts                 - 게시글 작성
PUT    /posts/:id             - 게시글 수정
DELETE /posts/:id             - 게시글 삭제

# 댓글 (게시글 하위 리소스)
GET    /posts/:id/comments    - 댓글 목록
POST   /posts/:id/comments    - 댓글 작성
DELETE /comments/:id          - 댓글 삭제

# 인증
POST   /auth/login            - 로그인
POST   /auth/register         - 회원가입
POST   /auth/refresh          - 토큰 갱신

# 유저
GET    /users/me              - 내 정보 조회
PATCH  /users/me              - 내 정보 수정
```

### 응답 형식 통일

```json
// 성공 (단건)
{
  "id": 1,
  "title": "Hello",
  "content": "World",
  "createdAt": "2025-01-15T09:00:00Z"
}

// 성공 (목록 + 페이징)
{
  "data": [
    {"id": 1, "title": "Hello"},
    {"id": 2, "title": "World"}
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}

// 에러
{
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "해당 유저를 찾을 수 없습니다.",
    "status": 404
  }
}
```

---

## 페이지네이션 방식

### 1. Offset 기반 (전통적)

```
GET /posts?page=2&size=10

장점: 구현 간단, 특정 페이지 이동 가능
단점: 데이터 추가/삭제 시 중복/누락 발생, 대량 데이터 시 느림 (OFFSET)
```

### 2. Cursor 기반 (권장)

```
GET /posts?cursor=eyJpZCI6MTB9&size=10

장점: 대량 데이터에서도 일정한 성능, 실시간 피드에 적합
단점: 특정 페이지로 직접 이동 불가
```

---

## REST vs GraphQL vs gRPC

| 비교 | REST | GraphQL | gRPC |
|------|------|---------|------|
| 프로토콜 | HTTP | HTTP | HTTP/2 |
| 데이터 형식 | JSON | JSON | Protocol Buffers |
| Over-fetching | 발생 가능 | 클라이언트가 필요한 것만 요청 | 스키마 정의 |
| 학습 곡선 | 낮음 | 중간 | 높음 |
| 사용 사례 | 일반 웹 API | 복잡한 프론트엔드 | 마이크로서비스 간 통신 |

---

## 면접 예상 질문

1. **REST API란 무엇인가요?**
   - 자원을 URI로 식별, HTTP 메서드로 행위 표현, 무상태 아키텍처

2. **RESTful한 API 설계 원칙은?**
   - 명사+복수형 URI, 적절한 HTTP 메서드/상태코드, 계층 관계

3. **PUT과 PATCH의 차이는?**
   - PUT: 전체 리소스 교체 / PATCH: 일부 필드만 수정

4. **멱등성이란?**
   - 같은 요청을 여러 번 보내도 결과가 동일. GET, PUT, DELETE는 멱등

5. **Stateless란?**
   - 서버가 클라이언트 상태를 저장하지 않음. 매 요청에 필요한 정보 포함

6. **페이지네이션 방식의 차이는?**
   - Offset: 구현 간단하지만 대량 데이터 시 느림 / Cursor: 성능 일정
