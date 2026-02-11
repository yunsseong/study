# 6. Next.js & SSR

Next.js는 React 기반의 프레임워크로, SSR, SSG, ISR 등 다양한 렌더링 전략을 제공합니다.

---

## 렌더링 방식 비교

### CSR (Client-Side Rendering)

**개념**: 브라우저에서 JavaScript를 실행하여 UI를 렌더링

**동작 과정**
```
1. 브라우저 → 서버: HTML 요청
2. 서버 → 브라우저: 거의 빈 HTML + JS 번들
3. 브라우저: JS 다운로드 및 실행
4. 브라우저: API 호출하여 데이터 가져오기
5. 브라우저: React가 UI 렌더링
6. 사용자: 인터랙티브한 페이지 사용

┌─────────────┐
│   브라우저   │
│  (빈 HTML)  │ ← 초기 HTML (매우 빠름)
└─────────────┘
      ↓
┌─────────────┐
│ JS 다운로드  │ ← 번들 크기에 따라 시간 소요
└─────────────┘
      ↓
┌─────────────┐
│ 데이터 페칭 │ ← API 호출
└─────────────┘
      ↓
┌─────────────┐
│ UI 렌더링   │ ← 사용자가 콘텐츠 볼 수 있음
└─────────────┘
```

**장점**
- 빠른 페이지 전환 (SPA)
- 풍부한 인터랙션
- 서버 부하 적음

**단점**
- 초기 로딩 느림 (FCP 늦음)
- SEO 불리
- JavaScript 비활성화 시 작동 안 함

**적합한 경우**
- 관리자 대시보드
- 내부 도구
- SEO가 중요하지 않은 애플리케이션

---

### SSR (Server-Side Rendering)

**개념**: 서버에서 매 요청마다 HTML을 생성하여 전송

**동작 과정**
```
1. 브라우저 → 서버: 페이지 요청
2. 서버: 데이터 페칭 (DB, API 등)
3. 서버: React 컴포넌트 → HTML 문자열 생성
4. 서버 → 브라우저: 완성된 HTML 전송
5. 브라우저: HTML 즉시 표시 (아직 인터랙티브 X)
6. 브라우저: JS 다운로드 및 실행
7. 브라우저: Hydration (이벤트 리스너 연결)
8. 사용자: 인터랙티브한 페이지 사용

┌─────────────┐
│   서버      │
│ 데이터 페칭 │ ← DB/API 호출
│ HTML 생성   │
└─────────────┘
      ↓
┌─────────────┐
│  브라우저    │
│ 완성된 HTML │ ← 콘텐츠 즉시 표시
└─────────────┘
      ↓
┌─────────────┐
│  Hydration  │ ← JavaScript 활성화
└─────────────┘
```

**장점**
- 빠른 FCP (First Contentful Paint)
- SEO 최적화
- 소셜 미디어 공유 최적화
- 최신 데이터 보장

**단점**
- 서버 부하 증가
- TTFB 느림
- 복잡한 캐싱 전략 필요

**적합한 경우**
- 뉴스 사이트
- 소셜 미디어 피드
- 실시간 데이터가 중요한 페이지

---

### SSG (Static Site Generation)

**개념**: 빌드 타임에 HTML을 미리 생성

**동작 과정**
```
[빌드 시]
1. 빌드 스크립트 실행
2. 모든 페이지에 대해 데이터 페칭
3. React 컴포넌트 → HTML 파일 생성
4. 정적 파일로 저장 (public/ 또는 .next/)

[요청 시]
1. 브라우저 → CDN: HTML 요청
2. CDN → 브라우저: 미리 생성된 HTML 즉시 전송
3. 브라우저: HTML 표시 + JS 로드
4. Hydration

┌─────────────┐
│  빌드 타임   │
│ HTML 생성   │ ← 모든 페이지 미리 생성
└─────────────┘
      ↓
┌─────────────┐
│    CDN      │ ← 정적 파일 호스팅
└─────────────┘
      ↓
┌─────────────┐
│  브라우저    │
│ 즉시 표시   │ ← 매우 빠름
└─────────────┘
```

**장점**
- 매우 빠른 로딩 속도
- CDN 캐싱 가능
- 서버 부하 없음
- 높은 가용성

**단점**
- 데이터 변경 시 재빌드 필요
- 빌드 시간 증가 (페이지 많을 경우)
- 동적 콘텐츠 어려움

**적합한 경우**
- 블로그
- 문서 사이트
- 마케팅 랜딩 페이지
- 포트폴리오

---

### ISR (Incremental Static Regeneration)

**개념**: SSG + 주기적 재생성

**동작 과정**
```
[빌드 시]
1. 일부 페이지만 미리 생성 (인기 페이지 등)

[첫 요청 시] (빌드 시 생성 안 된 페이지)
1. 브라우저 → 서버: 페이지 요청
2. 서버: 온디맨드 생성 (SSR처럼)
3. 생성된 HTML 캐시에 저장
4. 서버 → 브라우저: HTML 전송

[이후 요청 시] (revalidate 시간 전)
1. 캐시된 HTML 즉시 반환 (SSG처럼)

[revalidate 시간 경과 후 첫 요청]
1. 캐시된 HTML 즉시 반환 (사용자는 기다리지 않음)
2. 백그라운드에서 재생성 시작
3. 재생성 완료 후 캐시 업데이트

[재생성 완료 후 요청]
1. 새로 생성된 HTML 반환

┌──────────────────────────────────────┐
│ T=0: 빌드 시 HTML 생성                │
├──────────────────────────────────────┤
│ T=10s: 요청 → 캐시 반환              │
├──────────────────────────────────────┤
│ T=65s: 요청 → 캐시 반환 +            │
│        백그라운드 재생성 시작 (60s)   │
├──────────────────────────────────────┤
│ T=70s: 재생성 완료 → 캐시 업데이트   │
├──────────────────────────────────────┤
│ T=75s: 요청 → 새 캐시 반환           │
└──────────────────────────────────────┘
```

**장점**
- SSG의 속도 + SSR의 최신성
- 빌드 시간 단축
- 서버 부하 적음

**단점**
- 약간의 데이터 지연 가능 (revalidate 시간 내)
- 복잡도 증가

**적합한 경우**
- 전자상거래 제품 페이지
- 뉴스 사이트 (주기적 갱신)
- 자주 변경되는 콘텐츠

---

### 렌더링 방식 비교표

| 구분 | CSR | SSR | SSG | ISR |
|------|-----|-----|-----|-----|
| **렌더링 위치** | 브라우저 | 서버 (매 요청) | 빌드 타임 | 빌드 + 런타임 |
| **초기 로딩** | 느림 | 빠름 | 매우 빠름 | 매우 빠름 |
| **SEO** | 불리 | 유리 | 유리 | 유리 |
| **데이터 최신성** | 실시간 | 실시간 | 빌드 시점 | 주기적 갱신 |
| **서버 부하** | 낮음 | 높음 | 없음 | 낮음 |
| **CDN 캐싱** | 어려움 | 어려움 | 쉬움 | 쉬움 |
| **빌드 시간** | 짧음 | 짧음 | 긺 (많은 페이지) | 보통 |
| **사용 사례** | 대시보드 | 뉴스 피드 | 블로그 | 제품 페이지 |

---

## Next.js App Router

### 디렉토리 구조

```
app/
├── layout.tsx          # 루트 레이아웃 (필수)
├── page.tsx            # / 경로
├── loading.tsx         # 로딩 UI
├── error.tsx           # 에러 UI
├── not-found.tsx       # 404 UI
├── about/
│   └── page.tsx        # /about
├── blog/
│   ├── layout.tsx      # 블로그 레이아웃 (중첩)
│   ├── page.tsx        # /blog
│   └── [slug]/
│       ├── page.tsx    # /blog/:slug
│       └── loading.tsx # 블로그 글 로딩 UI
└── dashboard/
    ├── @analytics/     # 병렬 라우트
    │   └── page.tsx
    ├── @team/          # 병렬 라우트
    │   └── page.tsx
    └── layout.tsx      # 병렬 라우트 레이아웃
```

**특수 파일**
- `layout.tsx`: 공유 레이아웃 (중첩 가능)
- `page.tsx`: 라우트의 UI
- `loading.tsx`: Suspense 기반 로딩 UI
- `error.tsx`: Error Boundary
- `not-found.tsx`: 404 페이지
- `route.ts`: API 엔드포인트
- `template.tsx`: 재렌더링되는 레이아웃
- `default.tsx`: 병렬 라우트 폴백

---

### 파일 기반 라우팅

```tsx
// app/layout.tsx (루트 레이아웃 - 필수)
export default function RootLayout({ children }) {
  return (
    <html lang="ko">
      <body>
        <header>공통 헤더</header>
        {children} {/* 자식 페이지 */}
        <footer>공통 푸터</footer>
      </body>
    </html>
  );
}

// app/page.tsx
export default function HomePage() {
  return <h1>홈 페이지</h1>;
}

// app/blog/layout.tsx (중첩 레이아웃)
export default function BlogLayout({ children }) {
  return (
    <div>
      <nav>블로그 네비게이션</nav>
      {children}
    </div>
  );
}

// app/blog/page.tsx
export default function BlogPage() {
  return <h1>블로그 목록</h1>;
}

// app/blog/[slug]/page.tsx (동적 라우트)
export default function BlogPost({ params }) {
  return <article>블로그 글: {params.slug}</article>;
}
```

**라우팅 패턴**
- `/app/page.tsx` → `/`
- `/app/about/page.tsx` → `/about`
- `/app/blog/[slug]/page.tsx` → `/blog/:slug`
- `/app/shop/[...slug]/page.tsx` → `/shop/*` (catch-all)
- `/app/docs/[[...slug]]/page.tsx` → `/docs`, `/docs/*` (optional catch-all)

---

### Server Components vs Client Components

**기본: Server Component**
```tsx
// app/posts/page.tsx
// 'use client' 없음 → Server Component

async function PostsPage() {
  // 서버에서 직접 데이터 페칭
  const posts = await db.post.findMany();

  return (
    <ul>
      {posts.map(post => <li key={post.id}>{post.title}</li>)}
    </ul>
  );
}
```

**Client Component**
```tsx
// app/components/counter.tsx
'use client'; // 명시적으로 선언

import { useState } from 'react';

export default function Counter() {
  const [count, setCount] = useState(0);

  return (
    <button onClick={() => setCount(count + 1)}>
      {count}
    </button>
  );
}
```

**비교표**

| 기능 | Server Component | Client Component |
|------|------------------|------------------|
| **데이터 페칭** | ✅ async/await | ❌ useEffect 등 |
| **백엔드 접근** | ✅ DB, 파일 시스템 | ❌ API만 |
| **민감한 정보** | ✅ 서버에만 유지 | ❌ 노출 위험 |
| **번들 크기** | ✅ 포함 안 됨 | ❌ 증가 |
| **useState** | ❌ | ✅ |
| **useEffect** | ❌ | ✅ |
| **이벤트 리스너** | ❌ | ✅ onClick, onChange |
| **브라우저 API** | ❌ | ✅ window, localStorage |
| **Context** | ❌ | ✅ createContext |

**조합 패턴**
```tsx
// Server Component (부모)
async function ProductPage({ id }) {
  const product = await fetchProduct(id);

  return (
    <div>
      <h1>{product.name}</h1>
      <p>{product.description}</p>
      {/* Client Component 임베딩 */}
      <AddToCartButton product={product} />
    </div>
  );
}

// Client Component (자식)
'use client';

function AddToCartButton({ product }) {
  const [loading, setLoading] = useState(false);

  const handleClick = async () => {
    setLoading(true);
    await addToCart(product);
    setLoading(false);
  };

  return (
    <button onClick={handleClick} disabled={loading}>
      {loading ? '추가 중...' : '장바구니 담기'}
    </button>
  );
}
```

---

### Data Fetching (App Router)

**Server Component에서 fetch**
```tsx
// 기본: 캐시됨 (force-cache)
async function Page() {
  const data = await fetch('https://api.example.com/posts');
  return <div>{/* ... */}</div>;
}

// 캐시 안 함 (no-store) - 매 요청마다 최신 데이터
async function Page() {
  const data = await fetch('https://api.example.com/posts', {
    cache: 'no-store'
  });
  return <div>{/* ... */}</div>;
}

// 재검증 (revalidate) - ISR
async function Page() {
  const data = await fetch('https://api.example.com/posts', {
    next: { revalidate: 60 } // 60초마다 재검증
  });
  return <div>{/* ... */}</div>;
}

// 태그 기반 재검증
async function Page() {
  const data = await fetch('https://api.example.com/posts', {
    next: { tags: ['posts'] }
  });
  return <div>{/* ... */}</div>;
}
```

**라우트 세그먼트 설정**
```tsx
// app/posts/page.tsx

// 동적 렌더링 (매 요청마다)
export const dynamic = 'force-dynamic';

// 정적 렌더링 (빌드 시)
export const dynamic = 'force-static';

// 재검증 시간
export const revalidate = 60; // 60초
```

**병렬 데이터 페칭**
```tsx
async function Page() {
  // Promise.all로 병렬 실행
  const [user, posts, comments] = await Promise.all([
    fetchUser(),
    fetchPosts(),
    fetchComments(),
  ]);

  return <div>{/* ... */}</div>;
}
```

---

## Hydration

### 개념

**Hydration**: 서버에서 렌더링된 정적 HTML에 JavaScript를 "주입"하여 인터랙티브하게 만드는 과정

```
[서버]
React 컴포넌트 → ReactDOMServer.renderToString()
                ↓
              HTML 문자열

[브라우저]
1. HTML 수신 → 화면 표시 (아직 인터랙티브 X)
2. JS 다운로드
3. React 로드
4. React가 서버 HTML과 클라이언트 컴포넌트 트리 비교
5. 일치하면 이벤트 리스너만 추가 (빠름)
6. 불일치하면 경고 + 재렌더링 (느림)
```

**코드 예시**
```tsx
// 서버에서 실행
function Counter() {
  return <button>0</button>; // HTML: <button>0</button>
}

// 브라우저에서 Hydration
function Counter() {
  const [count, setCount] = useState(0);
  // React: "아, 이미 <button>0</button>이 있네?"
  // → DOM 재생성 안 함, onClick만 연결
  return <button onClick={() => setCount(count + 1)}>{count}</button>;
}
```

---

### Hydration Mismatch

**원인 1: 서버/클라이언트 환경 차이**
```tsx
// ❌ 잘못된 예
function Component() {
  return <div>{new Date().toLocaleString()}</div>;
  // 서버: 2024-01-01 10:00:00
  // 클라이언트: 2024-01-01 10:00:01 (불일치!)
}

// ✅ 올바른 예
function Component() {
  const [time, setTime] = useState('');

  useEffect(() => {
    setTime(new Date().toLocaleString());
  }, []);

  return <div>{time || '로딩 중...'}</div>;
}
```

**원인 2: 브라우저 전용 API**
```tsx
// ❌ 잘못된 예
function Component() {
  return <div>{window.innerWidth}px</div>;
  // 서버: window is not defined (에러)
}

// ✅ 올바른 예
function Component() {
  const [width, setWidth] = useState(0);

  useEffect(() => {
    setWidth(window.innerWidth);
  }, []);

  return <div>{width}px</div>;
}
```

**원인 3: 잘못된 HTML 중첩**
```tsx
// ❌ 잘못된 예
<p><div>내용</div></p> // <p> 안에 <div> 불가

// ✅ 올바른 예
<div><p>내용</p></div>
```

**suppressHydrationWarning**
```tsx
// 의도적으로 서버/클라이언트 다를 때
<div suppressHydrationWarning>
  {new Date().toLocaleString()}
</div>
```

---

## Streaming SSR & Suspense

### 개념

**Streaming SSR**: HTML을 청크 단위로 점진적으로 전송

```
[전통적인 SSR]
서버: 모든 데이터 완료 대기 → 전체 HTML 생성 → 전송
브라우저: 대기... → 전체 수신 → 표시

[Streaming SSR]
서버: 준비된 부분 즉시 전송 → 나머지 계속 전송
브라우저: 부분 수신 → 즉시 표시 → 추가 수신 → 추가 표시
```

---

### loading.tsx (자동 Suspense)

```tsx
// app/dashboard/loading.tsx
export default function Loading() {
  return <div className="spinner">로딩 중...</div>;
}

// app/dashboard/page.tsx
async function Dashboard() {
  const data = await fetchDashboardData(); // 느린 데이터
  return <div>{data}</div>;
}
```

Next.js가 자동으로 Suspense로 감쌈:
```tsx
<Suspense fallback={<Loading />}>
  <Dashboard />
</Suspense>
```

---

### 수동 Suspense

```tsx
import { Suspense } from 'react';

async function SlowComponent() {
  const data = await fetchSlowData(); // 3초 소요
  return <div>{data}</div>;
}

async function FastComponent() {
  const data = await fetchFastData(); // 0.5초 소요
  return <div>{data}</div>;
}

export default function Page() {
  return (
    <div>
      <h1>페이지 제목</h1> {/* 즉시 표시 */}

      {/* 빠른 컴포넌트 먼저 표시 */}
      <Suspense fallback={<div>로딩 중...</div>}>
        <FastComponent />
      </Suspense>

      {/* 느린 컴포넌트는 나중에 표시 */}
      <Suspense fallback={<div>데이터 불러오는 중...</div>}>
        <SlowComponent />
      </Suspense>
    </div>
  );
}
```

**동작 흐름**
```
T=0ms:   <h1> 즉시 표시
T=0ms:   FastComponent Suspense fallback 표시
T=0ms:   SlowComponent Suspense fallback 표시
T=500ms: FastComponent 데이터 로드 완료 → 실제 콘텐츠 표시
T=3000ms: SlowComponent 데이터 로드 완료 → 실제 콘텐츠 표시
```

---

### 중첩 Suspense

```tsx
export default function ProductPage() {
  return (
    <div>
      <Suspense fallback={<ProductSkeleton />}>
        <ProductInfo /> {/* 빠름: 0.5초 */}

        <Suspense fallback={<ReviewsSkeleton />}>
          <Reviews /> {/* 보통: 2초 */}
        </Suspense>

        <Suspense fallback={<RecommendationsSkeleton />}>
          <Recommendations /> {/* 느림: 5초 */}
        </Suspense>
      </ProductInfo>
    </div>
  );
}
```

**타임라인**
```
T=0s:    ProductSkeleton 표시
T=0.5s:  ProductInfo 표시
         ReviewsSkeleton 표시
         RecommendationsSkeleton 표시
T=2s:    Reviews 표시
         RecommendationsSkeleton 여전히 표시
T=5s:    Recommendations 표시 (완료)
```

---

## SEO 최적화

### Metadata API (App Router)

**정적 메타데이터**
```tsx
// app/layout.tsx
export const metadata = {
  title: '사이트 제목',
  description: '사이트 설명',
  keywords: ['Next.js', 'React', 'SEO'],
  openGraph: {
    title: 'OG 제목',
    description: 'OG 설명',
    images: ['/og-image.jpg'],
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Twitter 제목',
    description: 'Twitter 설명',
    images: ['/twitter-image.jpg'],
  },
};
```

**동적 메타데이터**
```tsx
// app/products/[id]/page.tsx
export async function generateMetadata({ params }) {
  const product = await fetchProduct(params.id);

  return {
    title: product.name,
    description: product.description,
    openGraph: {
      title: product.name,
      description: product.description,
      images: [product.image],
    },
  };
}
```

---

### Sitemap & Robots

**Sitemap**
```tsx
// app/sitemap.ts
export default function sitemap() {
  return [
    {
      url: 'https://example.com',
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 1,
    },
    {
      url: 'https://example.com/about',
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.8,
    },
  ];
}
```

**Robots.txt**
```tsx
// app/robots.ts
export default function robots() {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: '/private/',
    },
    sitemap: 'https://example.com/sitemap.xml',
  };
}
```

---

### 구조화된 데이터 (JSON-LD)

```tsx
function ProductPage({ product }) {
  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: product.name,
    image: product.image,
    description: product.description,
    offers: {
      '@type': 'Offer',
      price: product.price,
      priceCurrency: 'KRW',
      availability: 'https://schema.org/InStock',
    },
  };

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <div>{product.name}</div>
    </>
  );
}
```

---

## Image & Font 최적화

### next/image

**기본 사용**
```tsx
import Image from 'next/image';

// 로컬 이미지 (자동 크기 감지)
import profilePic from '../public/me.png';

<Image src={profilePic} alt="프로필" />

// 외부 이미지 (width, height 필수)
<Image
  src="https://example.com/photo.jpg"
  alt="사진"
  width={500}
  height={300}
/>
```

**반응형 이미지**
```tsx
<div style={{ position: 'relative', width: '100%', height: '400px' }}>
  <Image
    src="/photo.jpg"
    alt="반응형"
    fill
    sizes="(max-width: 768px) 100vw, 50vw"
    style={{ objectFit: 'cover' }}
  />
</div>
```

**우선순위 설정**
```tsx
// LCP 이미지는 priority 설정
<Image
  src="/hero.jpg"
  alt="히어로"
  width={1200}
  height={600}
  priority // Lazy loading 비활성화
/>
```

**블러 플레이스홀더**
```tsx
<Image
  src={profilePic}
  alt="프로필"
  placeholder="blur" // 자동 블러
/>
```

---

### next/font

```tsx
// app/layout.tsx
import { Inter, Noto_Sans_KR } from 'next/font/google';

const inter = Inter({ subsets: ['latin'] });
const notoSansKR = Noto_Sans_KR({ weight: ['400', '700'], subsets: ['korean'] });

export default function RootLayout({ children }) {
  return (
    <html lang="ko" className={notoSansKR.className}>
      <body>{children}</body>
    </html>
  );
}
```

**로컬 폰트**
```tsx
import localFont from 'next/font/local';

const myFont = localFont({
  src: './my-font.woff2',
  display: 'swap',
});
```

---

## 미들웨어

### 기본 구조

```tsx
// middleware.ts (프로젝트 루트)
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  // 요청 처리 로직
  return NextResponse.next();
}

export const config = {
  matcher: '/dashboard/:path*', // 적용 경로
};
```

---

### 인증

```tsx
export function middleware(request: NextRequest) {
  const token = request.cookies.get('token')?.value;

  // 로그인 페이지는 건너뛰기
  if (request.nextUrl.pathname.startsWith('/login')) {
    return NextResponse.next();
  }

  // 토큰 없으면 로그인 페이지로
  if (!token) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  return NextResponse.next();
}
```

---

### 리다이렉션

```tsx
export function middleware(request: NextRequest) {
  // 구 URL → 새 URL
  if (request.nextUrl.pathname === '/old-blog') {
    return NextResponse.redirect(new URL('/blog', request.url));
  }

  return NextResponse.next();
}
```

---

## 캐싱 전략

### 4가지 캐싱 메커니즘

```
┌─────────────────────────────────────────┐
│         Request Memoization             │ ← 렌더링 중 (React)
│  같은 fetch 요청 중복 제거               │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│            Data Cache                   │ ← 서버 (영구)
│  fetch 결과 캐싱                         │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│        Full Route Cache                 │ ← 서버 (빌드 시)
│  정적 라우트 HTML + RSC Payload         │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│          Router Cache                   │ ← 클라이언트 (메모리)
│  RSC Payload 클라이언트 캐싱            │
└─────────────────────────────────────────┘
```

---

### 1. Request Memoization

**자동으로 동작**
```tsx
async function Component1() {
  const data = await fetch('/api/data'); // 실제 요청
}

async function Component2() {
  const data = await fetch('/api/data'); // 메모이제이션 (중복 제거)
}
```

---

### 2. Data Cache

```tsx
// 무기한 캐시 (기본값)
fetch('https://api.example.com/posts');

// 캐시 안 함
fetch('https://api.example.com/posts', { cache: 'no-store' });

// 10초마다 재검증
fetch('https://api.example.com/posts', { next: { revalidate: 10 } });

// 태그 기반 재검증
fetch('https://api.example.com/posts', { next: { tags: ['posts'] } });
```

**온디맨드 재검증**
```tsx
import { revalidatePath, revalidateTag } from 'next/cache';

// 경로 재검증
revalidatePath('/posts');

// 태그 재검증
revalidateTag('posts');
```

---

### 3. Full Route Cache

```tsx
// 정적 렌더링 (캐시됨)
export const dynamic = 'auto'; // 기본값

// 동적 렌더링 (캐시 안 됨)
export const dynamic = 'force-dynamic';

// ISR
export const revalidate = 60;
```

---

### 4. Router Cache

**클라이언트 메모리에 자동 캐싱**
- 정적 라우트: 5분
- 동적 라우트: 30초

**무효화**
```tsx
router.refresh(); // 현재 라우트 재검증
```

---

## 면접 핵심 정리

### 렌더링 방식 선택

```
정적 콘텐츠 (블로그)
  → SSG (revalidate: false)

주기적 갱신 (제품 페이지)
  → ISR (revalidate: 60)

실시간 데이터 (뉴스 피드)
  → SSR (cache: 'no-store')

사용자별 데이터 (대시보드)
  → SSR + Client Component

SEO 불필요 (관리자 페이지)
  → CSR
```

### Server vs Client Component

```
기본은 Server Component
  ↓
인터랙션 필요?
  → Yes: Client Component ('use client')
  → No: Server Component 유지
```

### Hydration Mismatch 해결

```tsx
// ❌ 서버/클라이언트 불일치
<div>{new Date()}</div>

// ✅ useEffect로 클라이언트에서만 실행
useEffect(() => {
  setTime(new Date());
}, []);
```

### 캐싱 전략

```
데이터 특성에 따라 선택:
- 정적: revalidate: false
- 주기적: revalidate: 60
- 실시간: cache: 'no-store'
- 온디맨드: revalidateTag('posts')
```

### 성능 최적화

```
1. next/image: 자동 최적화, lazy loading
2. Streaming SSR: Suspense로 점진적 렌더링
3. Server Components: 번들 크기 감소
4. ISR: 정적 속도 + 동적 최신성
```
