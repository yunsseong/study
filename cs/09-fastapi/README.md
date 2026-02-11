# FastAPI Backend 로드맵

> Python + FastAPI 기반 백엔드 개발자 학습 로드맵

## 학습 순서

```
주 1     : Python 핵심 복습 (타입힌트, async)
주 2-3   : FastAPI 기초 (라우팅, 요청/응답)
주 3-4   : Pydantic (데이터 검증, 직렬화)
주 5-6   : 데이터베이스 (SQLAlchemy, Alembic)
주 7     : 인증/인가 (JWT, OAuth2)
주 8     : 비동기 프로그래밍 심화
주 9     : 테스트 (pytest, httpx)
주 10    : 배포 (Docker, CI/CD)
```

---

## Phase 1: Python 기초

### 1. [Python 핵심 복습](./01-python-basics/)

| 주제 | 핵심 |
|------|------|
| 타입 힌트 | str, int, list[str], Optional, Union |
| 데코레이터 | @property, 커스텀 데코레이터 |
| 컨텍스트 매니저 | with문, __enter__/__exit__ |
| 제너레이터 | yield, 지연 평가 |
| async/await 기초 | 코루틴, 이벤트 루프 |
| 가상환경 | venv, pip, requirements.txt |
| 프로젝트 구조 | 패키지, 모듈, __init__.py |

---

## Phase 2: FastAPI 핵심

### 2. [FastAPI Core](./02-fastapi-core/)

| 주제 | 핵심 |
|------|------|
| 프로젝트 설정 | uvicorn, 디렉토리 구조 |
| 라우팅 | @app.get, @app.post, APIRouter |
| Path Parameters | `/users/{user_id}` |
| Query Parameters | `?skip=0&limit=10` |
| Request Body | JSON 요청 처리 |
| Response Model | response_model, status_code |
| 에러 처리 | HTTPException, 커스텀 예외 핸들러 |
| 미들웨어 | CORS, 커스텀 미들웨어 |
| Dependency Injection | Depends(), 의존성 주입 패턴 |
| 자동 API 문서 | Swagger UI (/docs), ReDoc (/redoc) |

```python
# FastAPI 기본 구조
from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel

app = FastAPI()

class UserCreate(BaseModel):
    name: str
    email: str

@app.post("/users", status_code=201)
async def create_user(user: UserCreate):
    return {"id": 1, **user.model_dump()}

@app.get("/users/{user_id}")
async def get_user(user_id: int):
    if user_id <= 0:
        raise HTTPException(status_code=404, detail="User not found")
    return {"id": user_id, "name": "John"}
```

### 3. [Pydantic (데이터 검증)](./03-pydantic/)

| 주제 | 핵심 |
|------|------|
| BaseModel | 스키마 정의, 자동 검증 |
| Field | 제약 조건 (min_length, ge, le) |
| Validator | @field_validator, 커스텀 검증 |
| 중첩 모델 | 복합 데이터 구조 |
| Settings | 환경변수 관리 (BaseSettings) |
| 직렬화 | model_dump(), model_dump_json() |
| 요청/응답 분리 | CreateSchema, ResponseSchema, UpdateSchema |

```python
from pydantic import BaseModel, Field, field_validator

class UserCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=50)
    email: str = Field(..., pattern=r'^[\w\.-]+@[\w\.-]+\.\w+$')
    age: int = Field(..., ge=0, le=150)

    @field_validator('name')
    @classmethod
    def name_must_not_be_empty(cls, v):
        if not v.strip():
            raise ValueError('이름은 공백일 수 없습니다')
        return v.strip()

class UserResponse(BaseModel):
    id: int
    name: str
    email: str

    model_config = {"from_attributes": True}  # ORM 모드
```

---

## Phase 3: 데이터베이스

### 4. [Database (SQLAlchemy + Alembic)](./04-database/)

| 주제 | 핵심 |
|------|------|
| SQLAlchemy 기초 | Engine, Session, 연결 설정 |
| 모델 정의 | DeclarativeBase, Column 타입 |
| CRUD 작업 | session.add(), session.query(), session.delete() |
| 관계 매핑 | relationship(), ForeignKey |
| Alembic | 마이그레이션 생성, 적용, 롤백 |
| 비동기 DB | AsyncSession, create_async_engine |
| N+1 문제 | joinedload, selectinload |
| Repository 패턴 | DB 로직 분리 |

```python
# SQLAlchemy 모델
from sqlalchemy import Column, Integer, String, ForeignKey
from sqlalchemy.orm import relationship, DeclarativeBase

class Base(DeclarativeBase):
    pass

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(50), nullable=False)
    email = Column(String(100), unique=True, nullable=False)

    posts = relationship("Post", back_populates="author")

class Post(Base):
    __tablename__ = "posts"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(200), nullable=False)
    author_id = Column(Integer, ForeignKey("users.id"))

    author = relationship("User", back_populates="posts")
```

```python
# 의존성 주입으로 DB 세션 관리
from sqlalchemy.orm import Session

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/users")
async def list_users(db: Session = Depends(get_db)):
    return db.query(User).all()
```

---

## Phase 4: 인증 & 비동기

### 5. [Authentication (인증/인가)](./05-authentication/)

| 주제 | 핵심 |
|------|------|
| 비밀번호 해싱 | passlib, bcrypt |
| JWT 토큰 | python-jose, 생성/검증 |
| OAuth2 스키마 | OAuth2PasswordBearer, OAuth2PasswordRequestForm |
| 인증 의존성 | get_current_user Depends 패턴 |
| 역할 기반 접근 | admin, user 권한 분리 |
| 소셜 로그인 | OAuth2 플로우 (Google, Kakao) |

```python
from fastapi import Depends
from fastapi.security import OAuth2PasswordBearer
from jose import jwt

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="auth/login")

async def get_current_user(token: str = Depends(oauth2_scheme)):
    payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
    user_id = payload.get("sub")
    if user_id is None:
        raise HTTPException(status_code=401, detail="Invalid token")
    return user_id

@app.get("/users/me")
async def read_me(current_user: int = Depends(get_current_user)):
    return {"user_id": current_user}
```

### 6. [Async 비동기 프로그래밍](./06-async/)

| 주제 | 핵심 |
|------|------|
| async/await 심화 | 코루틴 동작 원리 |
| asyncio | gather, create_task, 동시 실행 |
| 비동기 DB | AsyncSession, async for |
| 비동기 HTTP | httpx.AsyncClient |
| 백그라운드 작업 | BackgroundTasks |
| 동기 vs 비동기 | 언제 어떤 걸 쓰는지 |

```python
import asyncio
from fastapi import BackgroundTasks

# 동시 실행
async def fetch_all():
    async with httpx.AsyncClient() as client:
        results = await asyncio.gather(
            client.get("https://api1.com"),
            client.get("https://api2.com"),
            client.get("https://api3.com"),
        )
    return results

# 백그라운드 작업
def send_email(email: str, message: str):
    # 시간이 오래 걸리는 작업
    pass

@app.post("/notify")
async def notify(background_tasks: BackgroundTasks):
    background_tasks.add_task(send_email, "user@test.com", "Hello")
    return {"message": "알림이 전송됩니다"}
```

---

## Phase 5: 품질 & 배포

### 7. [Testing (테스트)](./07-testing/)

| 주제 | 핵심 |
|------|------|
| pytest 기초 | fixture, parametrize, conftest |
| TestClient | FastAPI 엔드포인트 테스트 |
| DB 테스트 | 테스트용 DB, 트랜잭션 롤백 |
| Mock | unittest.mock, 외부 API 모킹 |
| 비동기 테스트 | pytest-asyncio |

```python
from fastapi.testclient import TestClient

client = TestClient(app)

def test_create_user():
    response = client.post("/users", json={
        "name": "John",
        "email": "john@test.com"
    })
    assert response.status_code == 201
    assert response.json()["name"] == "John"

def test_get_user_not_found():
    response = client.get("/users/999")
    assert response.status_code == 404
```

### 8. [Deployment (배포)](./08-deployment/)

| 주제 | 핵심 |
|------|------|
| 프로젝트 구조화 | app/, routers/, schemas/, models/ |
| Docker | Dockerfile, docker-compose |
| 환경 변수 | .env, Pydantic Settings |
| CI/CD | GitHub Actions |
| 클라우드 | AWS EC2, RDS |
| 로깅 | logging 모듈, 구조화된 로그 |

```
# 권장 프로젝트 구조
project/
├── app/
│   ├── __init__.py
│   ├── main.py              # FastAPI 앱 생성
│   ├── config.py            # 설정 (BaseSettings)
│   ├── database.py          # DB 연결
│   ├── models/              # SQLAlchemy 모델
│   ├── schemas/             # Pydantic 스키마
│   ├── routers/             # API 라우터
│   ├── services/            # 비즈니스 로직
│   └── dependencies.py      # 공통 의존성
├── alembic/                 # 마이그레이션
├── tests/
├── Dockerfile
├── docker-compose.yml
└── requirements.txt
```

---

## 실습 프로젝트 (포트폴리오)

```
1단계: TODO API
   - FastAPI + SQLAlchemy + SQLite
   - CRUD, Pydantic 검증, 에러 처리

2단계: 블로그 API
   - 회원가입/로그인 (JWT)
   - 게시글 CRUD + 댓글
   - 페이징, 검색

3단계: 고도화
   - PostgreSQL + Alembic 마이그레이션
   - Redis 캐싱, 비동기 처리
   - Docker 배포, 테스트 코드
```

---

## Spring vs FastAPI 비교

| 비교 | Spring Boot | FastAPI |
|------|------------|---------|
| 언어 | Java | Python |
| 성능 | 높음 (JVM 최적화) | 높음 (비동기 네이티브) |
| 타입 시스템 | 컴파일 타임 검증 | 런타임 (타입힌트) |
| 생태계 | 매우 넓음 (엔터프라이즈) | 성장 중 (AI/ML 강점) |
| 학습 곡선 | 높음 | 낮음 |
| ORM | JPA/Hibernate | SQLAlchemy |
| 문서화 | Swagger 설정 필요 | 자동 생성 |
| DI | 프레임워크 내장 | Depends() |
| 취업 시장 | 대기업, 금융, SI | 스타트업, AI, 데이터 |

---

## 면접 빈출 질문

### FastAPI 핵심
1. FastAPI가 빠른 이유는? (Starlette + Pydantic + ASGI)
2. 동기 함수와 비동기 함수의 차이는?
3. Depends()를 활용한 의존성 주입 패턴이란?
4. Pydantic의 역할과 장점은?

### 데이터베이스
5. SQLAlchemy의 Session 관리 방법은?
6. Alembic 마이그레이션이란?
7. N+1 문제와 해결법은?

### 설계
8. FastAPI 프로젝트의 추천 디렉토리 구조는?
9. Repository 패턴을 사용하는 이유는?
10. 동기 vs 비동기를 어떤 기준으로 선택하나요?

---

## 진행 상황

- [ ] Python 핵심 복습
- [ ] FastAPI Core
- [ ] Pydantic
- [ ] Database (SQLAlchemy + Alembic)
- [ ] Authentication
- [ ] Async 비동기 프로그래밍
- [ ] Testing
- [ ] Deployment
- [ ] 실습 프로젝트
