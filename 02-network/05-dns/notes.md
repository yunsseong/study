# DNS (Domain Name System)

## 개념

- **도메인 이름을 IP 주소로 변환**하는 시스템
- 인터넷의 전화번호부 역할
- `www.google.com` → `142.250.196.110`

---

## DNS 조회 과정 (면접 핵심)

"브라우저에 www.example.com 입력하면 일어나는 일" 중 DNS 부분.

```
① 브라우저 캐시 확인
   → 있으면 바로 사용

② OS 캐시 확인 (/etc/hosts)
   → 있으면 바로 사용

③ 로컬 DNS 서버 (ISP의 리졸버) 에게 질의
   → 캐시에 있으면 바로 응답

④ 로컬 DNS → Root DNS 서버
   "www.example.com의 IP가 뭐야?"
   Root: ".com 담당하는 TLD 서버 주소는 이거야"

⑤ 로컬 DNS → TLD DNS 서버 (.com 담당)
   "www.example.com의 IP가 뭐야?"
   TLD: "example.com 담당하는 권한 서버 주소는 이거야"

⑥ 로컬 DNS → 권한(Authoritative) DNS 서버
   "www.example.com의 IP가 뭐야?"
   권한 서버: "142.250.196.110이야!"

⑦ 로컬 DNS → 브라우저에게 IP 주소 전달 (캐싱)
⑧ 브라우저 → 해당 IP로 TCP 연결 시작
```

### 순서 요약

```
브라우저 캐시 → OS 캐시 → 로컬 DNS → Root → TLD → Authoritative
```

---

## DNS 서버 종류

| 서버 | 역할 | 예시 |
|------|------|------|
| **Root DNS** | 최상위, TLD 서버 위치 안내 | 전 세계 13개 그룹 |
| **TLD DNS** | 도메인 확장자별 관리 | .com, .kr, .org |
| **Authoritative DNS** | 실제 도메인의 IP를 저장 | example.com의 네임서버 |
| **Recursive Resolver** | 위 과정을 대행하는 로컬 DNS | ISP 리졸버, 8.8.8.8 (Google) |

---

## DNS 레코드 타입

| 타입 | 용도 | 예시 |
|------|------|------|
| **A** | 도메인 → IPv4 | example.com → 93.184.216.34 |
| **AAAA** | 도메인 → IPv6 | example.com → 2001:db8::1 |
| **CNAME** | 도메인 → 다른 도메인 (별칭) | www.example.com → example.com |
| **MX** | 메일 서버 지정 | example.com → mail.example.com |
| **NS** | 네임서버 지정 | example.com → ns1.example.com |
| **TXT** | 텍스트 정보 (인증 등) | SPF, DKIM (메일 인증) |

### CNAME vs A 레코드

```
A 레코드:     example.com  →  93.184.216.34 (직접 IP)
CNAME 레코드: www.example.com  →  example.com (별칭 → 다시 A 레코드 조회)
```

---

## DNS 캐싱과 TTL

### TTL (Time To Live)
- DNS 레코드의 **캐시 유효 시간** (초 단위)
- TTL=3600 → 1시간 동안 캐싱, 이후 다시 조회

```
TTL이 짧으면: 변경 반영이 빠름, 하지만 DNS 조회 빈번
TTL이 길면:   DNS 조회 줄어듦, 하지만 변경 반영이 느림
```

### 캐싱 계층

```
1. 브라우저 DNS 캐시 (가장 먼저 확인)
2. OS DNS 캐시
3. 로컬 DNS 서버(리졸버) 캐시
4. 각 DNS 서버의 캐시
```

---

## 실무 관련 DNS 지식

### Round Robin DNS (로드밸런싱)

하나의 도메인에 여러 IP를 등록하여 분산.

```
example.com → 1.1.1.1
example.com → 2.2.2.2
example.com → 3.3.3.3

첫 번째 요청: 1.1.1.1
두 번째 요청: 2.2.2.2
세 번째 요청: 3.3.3.3
```

### /etc/hosts 파일

OS 레벨에서 도메인-IP 매핑을 직접 지정.

```
# /etc/hosts
127.0.0.1   localhost
192.168.0.10  my-dev-server.local
```

- DNS 서버보다 먼저 확인됨
- 로컬 개발 환경에서 유용

### 공개 DNS 서버

| 제공자 | 주소 |
|--------|------|
| Google | 8.8.8.8, 8.8.4.4 |
| Cloudflare | 1.1.1.1 |
| KT | 168.126.63.1 |

---

## 브라우저에 URL 입력 시 전체 흐름 (총정리)

```
1. URL 파싱    : https://www.example.com/page 분석
2. DNS 조회    : www.example.com → IP 주소
3. TCP 연결    : 3-way handshake
4. TLS 연결    : (HTTPS인 경우) TLS handshake
5. HTTP 요청   : GET /page HTTP/1.1
6. 서버 처리   : 요청 처리, DB 조회 등
7. HTTP 응답   : 200 OK + HTML
8. 브라우저 렌더링 : HTML 파싱 → DOM 트리 → 화면 표시
9. 연결 종료   : 4-way handshake (또는 Keep-Alive 유지)
```

---

## 면접 예상 질문

1. **DNS의 동작 과정을 설명해주세요**
   - 브라우저 캐시 → OS → 로컬 DNS → Root → TLD → Authoritative

2. **DNS를 사용하는 이유는?**
   - 사람이 IP 주소 대신 도메인 이름으로 접속, 서버 IP 변경에 유연

3. **DNS 캐싱은 어디서 일어나나요?**
   - 브라우저, OS, 로컬 DNS 서버, 각 DNS 서버

4. **A 레코드와 CNAME의 차이는?**
   - A: 도메인→IP 직접 매핑 / CNAME: 도메인→도메인 별칭

5. **브라우저에 URL을 입력하면 일어나는 일을 설명해주세요** (종합)
   - DNS → TCP → TLS → HTTP 요청 → 응답 → 렌더링
