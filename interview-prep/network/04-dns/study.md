# 4. DNS 동작 원리

---

## DNS란?

**DNS (Domain Name System)**: 도메인 이름을 IP 주소로 변환하는 시스템

```
사람: "google.com에 접속하고 싶어"
컴퓨터: "google.com? 모르겠는데... IP 주소가 뭐지?"
DNS: "google.com은 142.250.207.46이야"
컴퓨터: "142.250.207.46로 접속!"
```

사람은 이름(google.com)을 기억하고, 컴퓨터는 숫자(IP)로 통신한다.
DNS는 이 둘을 **연결해주는 전화번호부**다.

---

## 브라우저에 google.com을 치면? (전체 흐름)

이 질문은 면접 초단골이다. DNS는 이 과정의 **첫 번째 단계**다.

```
전체 흐름:
1. DNS 조회 (google.com → 142.250.207.46)  ← 이 문서에서 배우는 것
2. TCP 연결 (3-way handshake)
3. HTTP 요청 전송
4. 서버가 HTML 응답
5. 브라우저가 HTML 렌더링
```

지금은 1번 DNS 조회 과정을 자세히 보자.

---

## DNS 조회 과정 (상세)

### 1단계: 로컬 캐시 확인

브라우저가 google.com의 IP를 찾을 때, 먼저 **이미 알고 있는 곳**을 확인한다.

```
확인 순서:
1. 브라우저 DNS 캐시     → 이전에 접속한 적 있으면 기억하고 있음
2. OS DNS 캐시          → 운영체제도 캐시를 가지고 있음
3. hosts 파일           → /etc/hosts (수동 매핑)
4. 공유기(라우터) 캐시    → 공유기도 캐시 가능

→ 여기서 찾으면 끝! DNS 서버에 물어볼 필요 없음
→ 못 찾으면 다음 단계로
```

### 2단계: DNS Resolver에게 질문

로컬에 없으면 **DNS Resolver(재귀적 해석기)**에게 물어본다.
보통 인터넷 서비스 제공자(ISP - KT, SKT 등)가 운영한다.

```
[내 PC] → "google.com IP 뭐야?" → [DNS Resolver (KT)]
```

DNS Resolver도 캐시가 있다. 캐시에 있으면 바로 응답.
없으면 직접 찾으러 간다 → 3단계

### 3단계: DNS 계층 구조 탐색

DNS는 **계층 구조**로 되어 있다. 위에서부터 내려가며 찾는다.

```
도메인: www.google.com
분해하면: www . google . com . (맨 뒤에 루트)

           [루트 DNS (.)]
                |
           [TLD DNS (.com)]
                |
           [권한 DNS (google.com)]
                |
        IP: 142.250.207.46
```

실제 과정:

```
DNS Resolver → 루트 DNS 서버
  "google.com의 IP 아세요?"
  "나는 모르지만 .com 담당 서버 주소를 알려줄게"

DNS Resolver → TLD DNS 서버 (.com 담당)
  "google.com의 IP 아세요?"
  "나는 모르지만 google.com 담당 서버 주소를 알려줄게"

DNS Resolver → 권한(Authoritative) DNS 서버 (google.com 담당)
  "google.com의 IP 아세요?"
  "142.250.207.46이야!"

DNS Resolver → 내 PC
  "google.com은 142.250.207.46이야"
  (그리고 캐시에 저장해둠)
```

### 전체 과정 그림

```
[내 PC]
   │ ① "google.com IP?"
   ↓
[DNS Resolver (KT)]
   │ ② "google.com?"     → [루트 DNS]      → ".com은 저기"
   │ ③ "google.com?"     → [.com TLD DNS]  → "google.com은 저기"
   │ ④ "google.com?"     → [google.com     → "142.250.207.46"
   │                         권한 DNS]
   │ ⑤ 결과 캐시 저장
   ↓
[내 PC] ← ⑥ "142.250.207.46이야!"
```

---

## 재귀적 질의 vs 반복적 질의

### 재귀적 질의 (Recursive Query)

```
[내 PC] → [DNS Resolver]: "google.com 알아내줘"
DNS Resolver가 알아서 루트 → TLD → 권한 DNS를 다 돌아다님
결과만 내 PC에 돌려줌
```

- 클라이언트(내 PC)는 **한 번만 요청**하면 된다
- DNS Resolver가 대신 다 해준다
- **내 PC → DNS Resolver** 구간이 재귀적 질의

### 반복적 질의 (Iterative Query)

```
[DNS Resolver] → [루트 DNS]: "google.com?"
[루트 DNS] → "나는 모르고, .com은 여기야" (다음 주소 알려줌)
[DNS Resolver] → [.com DNS]: "google.com?"
[.com DNS] → "나는 모르고, google.com은 여기야" (다음 주소 알려줌)
[DNS Resolver] → [google.com DNS]: "google.com?"
[google.com DNS] → "142.250.207.46이야!"
```

- DNS Resolver가 **직접 돌아다니며** 물어본다
- 각 서버는 답을 모르면 다음 서버 주소만 알려준다
- **DNS Resolver → 각 DNS 서버** 구간이 반복적 질의

---

## DNS 캐시와 TTL

### DNS 캐시가 있는 곳

```
[브라우저 캐시] → 가장 먼저 확인
[OS 캐시]      → 그 다음
[공유기 캐시]   → 그 다음
[DNS Resolver]  → ISP 수준 캐시
```

캐시 덕분에 매번 루트 DNS부터 찾지 않아도 된다 → **속도 향상**

### TTL (Time To Live)

DNS 레코드에는 **TTL**이 설정되어 있다.
"이 정보를 얼마 동안 캐시해도 되는지" 시간(초)을 나타낸다.

```
google.com → 142.250.207.46  TTL: 300 (5분)

의미: "이 IP 정보를 5분 동안 캐시해도 좋아. 5분 지나면 다시 물어봐."
```

- TTL이 짧으면: 자주 갱신되어 최신 정보 유지, 하지만 DNS 조회 많아짐
- TTL이 길면: 캐시 활용 높아 빠르지만, IP가 바뀌어도 반영이 느림

---

## DNS 레코드 타입

DNS 서버에는 여러 종류의 정보가 저장되어 있다.

| 레코드 | 의미 | 예시 |
|--------|------|------|
| **A** | 도메인 → IPv4 주소 | google.com → 142.250.207.46 |
| **AAAA** | 도메인 → IPv6 주소 | google.com → 2404:6800::200e |
| **CNAME** | 도메인 → 다른 도메인 (별칭) | www.google.com → google.com |
| **MX** | 메일 서버 주소 | google.com → smtp.google.com |
| **NS** | 네임서버 주소 | google.com → ns1.google.com |
| **TXT** | 텍스트 정보 (인증 등) | SPF, DKIM 등 |

### 자주 쓰는 것만 기억하기

```
A 레코드:     도메인 → IP (가장 기본, 가장 중요)
CNAME 레코드:  별칭 (www.google.com → google.com)
MX 레코드:    이메일 전용
```

---

## 실무에서 DNS

### AWS Route 53

AWS의 DNS 서비스. 도메인을 관리하고 라우팅한다.

```
내가 myapp.com 도메인을 샀다면:

Route 53에서 설정:
  myapp.com    → A 레코드    → EC2 서버 IP (13.125.xxx.xxx)
  api.myapp.com → CNAME 레코드 → 로드 밸런서 주소
```

Spring Boot 서버를 AWS에 배포하면:
1. EC2에 서버 실행 (IP: 13.125.xxx.xxx, 포트: 8080)
2. Route 53에서 myapp.com → 13.125.xxx.xxx 설정
3. 사용자가 myapp.com 접속 → DNS가 IP 반환 → 서버에 연결

### DNS가 성능에 미치는 영향

```
첫 접속: DNS 조회 50~200ms + TCP 연결 + HTTP 요청
재접속: DNS 캐시 히트 0ms + TCP 연결 + HTTP 요청
```

DNS 조회 시간을 줄이는 방법:
- **DNS 캐시 활용** (적절한 TTL 설정)
- **DNS Prefetch** (브라우저가 미리 DNS 조회)
- **CDN 사용** (사용자와 가까운 서버)

---

## 전체 흐름 다시 정리

```
브라우저에 "http://myapp.com:8080/api/users" 입력

[1단계: DNS]
  브라우저 캐시 확인 → 없음
  OS 캐시 확인 → 없음
  DNS Resolver(KT)에 질의
  Resolver: 루트 → .com TLD → myapp.com 권한 DNS
  결과: 13.125.xxx.xxx
  캐시에 저장 (TTL만큼)

[2단계: TCP]
  13.125.xxx.xxx:8080으로 3-way handshake → 연결 완료

[3단계: HTTP]
  GET /api/users HTTP/1.1
  Host: myapp.com

[4단계: 응답]
  Spring Boot가 처리 → JSON 응답

[5단계: 브라우저]
  응답 표시
```

---

## 면접 핵심 정리

**Q: 브라우저에 URL을 입력하면 무슨 일이 일어나나요?**
> 먼저 DNS 조회로 도메인의 IP 주소를 찾습니다. 브라우저 캐시, OS 캐시를 확인하고
> 없으면 DNS Resolver가 루트, TLD, 권한 DNS를 순서대로 조회합니다.
> IP를 얻으면 TCP 3-way handshake로 연결하고, HTTP 요청을 보내고, 응답을 받아 렌더링합니다.

**Q: DNS는 어떻게 동작하나요?**
> DNS는 계층 구조입니다. DNS Resolver가 루트 DNS → TLD DNS → 권한 DNS 순서로
> 반복적 질의를 하여 도메인의 IP를 찾습니다.
> 캐시가 각 단계에 있어서 매번 전체 과정을 거치지 않습니다.

**Q: DNS 캐시는 어디에 있나요?**
> 브라우저, OS, 공유기, DNS Resolver 총 4곳에 있습니다.
> 각 캐시에는 TTL이 있어 설정된 시간이 지나면 다시 조회합니다.
> 캐시 덕분에 DNS 조회 시간을 크게 줄일 수 있습니다.

**Q: A 레코드와 CNAME의 차이는?**
> A 레코드는 도메인을 직접 IP 주소에 매핑하고,
> CNAME은 도메인을 다른 도메인에 매핑합니다(별칭).
> 예를 들어 www.google.com(CNAME) → google.com(A) → 142.250.207.46 입니다.
