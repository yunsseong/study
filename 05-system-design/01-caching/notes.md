# 캐싱 (Caching)

## 개념

- 자주 사용하는 데이터를 **빠른 저장소에 임시 저장**하여 응답 속도를 높이는 기법
- DB 부하를 줄이고, 응답 시간을 획기적으로 개선

```
요청 → 캐시 확인 → 있으면(Cache Hit) → 바로 반환 (빠름)
                 → 없으면(Cache Miss) → DB 조회 → 캐시에 저장 → 반환
```

### 캐시가 필요한 이유

```
DB 조회: ~10ms
Redis 조회: ~0.5ms
→ 약 20배 빠름

DB 커넥션 수: 제한적 (보통 100~300개)
Redis: 수만 개 동시 연결 가능
→ DB 부하 감소
```

---

## 캐시 저장소 종류

| 계층 | 저장소 | 속도 | 용도 |
|------|--------|------|------|
| **브라우저 캐시** | 로컬 스토리지/메모리 | 가장 빠름 | 정적 파일, CSS, JS |
| **CDN 캐시** | 엣지 서버 | 매우 빠름 | 이미지, 동영상, 정적 콘텐츠 |
| **애플리케이션 캐시** | Redis, Memcached | 빠름 | DB 쿼리 결과, 세션 |
| **DB 캐시** | 쿼리 캐시, 버퍼 풀 | 보통 | 반복 쿼리 결과 |

---

## Redis

### Redis 특징

```
- In-Memory 저장소 → 매우 빠름
- Key-Value 구조
- 다양한 자료구조 지원 (String, Hash, List, Set, Sorted Set)
- TTL(만료 시간) 설정 가능
- 싱글 스레드 (명령어 처리) → 원자적 연산 보장
- 영속화 지원 (RDB 스냅샷, AOF 로그)
```

### Redis 자료구조 활용

```
String: 단순 캐시, 세션
  SET user:1 "John"
  GET user:1

Hash: 객체 저장 (필드별 접근)
  HSET user:1 name "John" age 25
  HGET user:1 name

List: 최근 목록, 큐
  LPUSH recent:posts "post:1"
  LRANGE recent:posts 0 9    → 최근 10개

Set: 유니크 집합, 태그
  SADD post:1:tags "java" "spring"
  SMEMBERS post:1:tags

Sorted Set: 랭킹, 실시간 순위
  ZADD ranking 100 "user:1"
  ZADD ranking 200 "user:2"
  ZREVRANGE ranking 0 9      → 상위 10명
```

---

## 캐시 전략 (Cache Strategy)

### 읽기 전략

#### 1. Cache Aside (Look Aside) — 가장 일반적

```
애플리케이션이 캐시를 직접 관리.

읽기:
1. 캐시에서 조회
2. Cache Hit → 바로 반환
3. Cache Miss → DB 조회 → 캐시에 저장 → 반환

쓰기:
1. DB에 쓰기
2. 캐시 삭제 (또는 갱신)
```

```python
# FastAPI + Redis 예시
import redis
import json

r = redis.Redis()

def get_user(user_id: int):
    # 1. 캐시 확인
    cached = r.get(f"user:{user_id}")
    if cached:
        return json.loads(cached)  # Cache Hit

    # 2. Cache Miss → DB 조회
    user = db.query(User).filter(User.id == user_id).first()

    # 3. 캐시에 저장 (TTL 1시간)
    r.setex(f"user:{user_id}", 3600, json.dumps(user.dict()))

    return user
```

```java
// Spring + Redis 예시
@Service
public class UserService {
    @Cacheable(value = "users", key = "#userId")
    public User getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow();
        // Cache Miss 시 DB 조회 → 자동으로 캐시 저장
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateUser(Long userId, UserDto dto) {
        // DB 업데이트 후 캐시 자동 삭제
    }
}
```

#### 2. Read Through

```
캐시가 DB 조회를 대신 수행.

1. 애플리케이션 → 캐시에 요청
2. Cache Miss → 캐시가 직접 DB 조회 → 저장 → 반환

Cache Aside와의 차이:
- Cache Aside: 애플리케이션이 DB를 직접 조회
- Read Through: 캐시가 DB를 조회 (애플리케이션은 캐시만 바라봄)
```

### 쓰기 전략

#### 1. Write Through

```
쓰기 시 캐시와 DB를 동시에 갱신.

1. 데이터 쓰기 요청
2. 캐시에 쓰기
3. DB에 쓰기 (동기)
4. 완료 응답

장점: 캐시와 DB 항상 일관성 유지
단점: 쓰기 느림 (2곳에 쓰기), 안 읽히는 데이터도 캐시에 저장
```

#### 2. Write Behind (Write Back)

```
쓰기 시 캐시에만 쓰고, 나중에 DB에 반영.

1. 데이터 쓰기 요청
2. 캐시에 쓰기 → 바로 응답
3. 일정 시간/조건 후 DB에 비동기 반영

장점: 쓰기 매우 빠름, DB 부하 감소
단점: 캐시 장애 시 데이터 유실 위험
사용: 좋아요 수, 조회수 (유실돼도 치명적이지 않은 데이터)
```

#### 3. Write Around

```
DB에만 쓰고, 캐시는 건드리지 않음.

1. DB에 쓰기
2. 캐시는 그대로 (다음 읽기 시 Cache Miss → 갱신)

장점: 쓰기 빠름, 불필요한 캐시 저장 방지
단점: 쓰기 직후 읽으면 항상 Cache Miss
사용: Cache Aside와 주로 함께 사용
```

### 전략 비교

```
읽기 중심 서비스 (게시판, 상품 조회):
  → Cache Aside + Write Around (가장 일반적)

쓰기 빈번 + 일관성 중요 (결제, 재고):
  → Write Through

쓰기 빈번 + 성능 중요 (좋아요, 조회수):
  → Write Behind
```

---

## 캐시 무효화 (Cache Invalidation)

"컴퓨터 과학에서 어려운 것 두 가지: 캐시 무효화와 이름 짓기" — Phil Karlton

### 무효화 전략

| 전략 | 방식 | 적합한 상황 |
|------|------|------------|
| **TTL (Time To Live)** | 시간 경과 후 자동 삭제 | 대부분의 캐시 |
| **이벤트 기반 삭제** | 데이터 변경 시 삭제 | 실시간 정합성 필요 |
| **버전 기반** | 키에 버전 포함 | API 응답 캐시 |

```python
# TTL 기반
r.setex("user:1", 3600, data)  # 1시간 후 자동 만료

# 이벤트 기반 삭제
def update_user(user_id, data):
    db.update(user_id, data)
    r.delete(f"user:{user_id}")       # 캐시 삭제
    r.delete(f"user_list:page:1")     # 관련 목록 캐시도 삭제

# 버전 기반
r.set(f"user:{user_id}:v2", data)     # 버전 올리면 자동 무효화
```

---

## 캐시 문제와 해결

### 1. Cache Stampede (Thundering Herd)

```
인기 데이터의 TTL 만료 → 수천 요청이 동시에 DB 조회 → DB 과부하

해결:
1. 뮤텍스 락: 1개 요청만 DB 조회, 나머지는 대기
2. TTL 분산: TTL에 랜덤값 추가 (예: 3600 + random(0, 300))
3. 사전 갱신: TTL 만료 전에 백그라운드에서 갱신
```

```python
# 뮤텍스 락 방식
def get_with_lock(key):
    data = r.get(key)
    if data:
        return data

    # 락 획득 시도 (NX: 없을 때만, EX: 10초 타임아웃)
    if r.set(f"lock:{key}", "1", nx=True, ex=10):
        data = db.query(...)
        r.setex(key, 3600, data)
        r.delete(f"lock:{key}")
        return data
    else:
        time.sleep(0.1)  # 잠시 대기 후 재시도
        return get_with_lock(key)
```

### 2. Cache Penetration

```
존재하지 않는 데이터를 반복 요청 → 매번 Cache Miss → DB 부하

예: /user/9999999 (없는 유저를 계속 요청)

해결:
1. Null 캐싱: 없는 데이터도 캐시 (짧은 TTL)
2. 블룸 필터: 존재 여부를 빠르게 확인
```

```python
# Null 캐싱
def get_user(user_id):
    cached = r.get(f"user:{user_id}")
    if cached == "NULL":
        return None  # 없는 데이터
    if cached:
        return json.loads(cached)

    user = db.query(User).filter(User.id == user_id).first()
    if user is None:
        r.setex(f"user:{user_id}", 300, "NULL")  # 5분간 캐시
        return None

    r.setex(f"user:{user_id}", 3600, json.dumps(user.dict()))
    return user
```

### 3. Cache Avalanche

```
대량의 캐시가 동시에 만료 → DB에 요청 폭주

해결:
1. TTL 분산: 만료 시간에 랜덤값 추가
2. 다중 캐시 레이어: L1(로컬) + L2(Redis)
3. 서킷 브레이커: DB 과부하 시 요청 차단
```

---

## 면접 예상 질문

1. **캐시 전략(Cache Aside, Write Through 등)을 설명해주세요**
   - Cache Aside: 앱이 캐시/DB 직접 관리, 가장 일반적
   - Write Through: 캐시와 DB 동시 쓰기, 일관성 보장

2. **캐시 무효화를 어떻게 하나요?**
   - TTL, 이벤트 기반 삭제, 버전 기반

3. **Cache Stampede란? 해결 방법은?**
   - 인기 데이터 만료 시 동시 DB 조회 → 뮤텍스 락, TTL 분산

4. **Redis를 어떤 용도로 사용하나요?**
   - 캐시, 세션, Rate Limiting, 랭킹, 메시지 큐

5. **캐시 일관성 문제를 어떻게 해결하나요?**
   - 이벤트 기반 삭제, TTL, Write Through 전략 활용
