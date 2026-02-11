# Next.js & SSR 면접 질문 + 답변

---

**Q1.** CSR, SSR, SSG, ISR의 차이점을 설명하고, 각각 어떤 상황에서 사용하는 것이 적합한지 말씀해주세요.

> **CSR (Client-Side Rendering)**
> - 브라우저에서 JavaScript를 실행하여 렌더링
> - 초기 HTML은 거의 비어있고, JS가 다운로드된 후 UI 생성
> - 장점: 빠른 페이지 전환, 풍부한 인터랙션
> - 단점: 초기 로딩 느림, SEO 불리
> - 적합한 경우: SEO가 중요하지 않은 대시보드, 관리자 페이지
>
> **SSR (Server-Side Rendering)**
> - 서버에서 요청 시마다 HTML을 생성하여 전송
> - 장점: 빠른 초기 렌더링, SEO 유리, 최신 데이터 보장
> - 단점: 서버 부하 증가, TTFB(Time To First Byte) 증가 가능
> - 적합한 경우: 실시간 데이터가 중요한 뉴스, 소셜 미디어 피드
>
> **SSG (Static Site Generation)**
> - 빌드 타임에 HTML을 미리 생성
> - 장점: 매우 빠른 로딩 속도, CDN 캐싱 가능, 서버 부하 없음
> - 단점: 데이터 변경 시 재빌드 필요, 동적 콘텐츠 어려움
> - 적합한 경우: 블로그, 문서, 마케팅 페이지
>
> **ISR (Incremental Static Regeneration)**
> - SSG + 주기적 재생성
> - 정적 페이지를 유지하면서 백그라운드에서 재생성
> - 장점: SSG의 속도 + 최신 데이터 반영
> - 단점: 약간의 데이터 지연 가능
> - 적합한 경우: 전자상거래 제품 페이지, 뉴스 사이트

---

**Q2.** SSR(Server-Side Rendering)의 장점과 단점을 설명해주세요.

> **장점**
> 1. **빠른 초기 렌더링 (FCP, First Contentful Paint)**
>    - 서버에서 완성된 HTML을 받아 즉시 화면 표시
>    - 사용자가 콘텐츠를 빠르게 볼 수 있음
>
> 2. **SEO 최적화**
>    - 검색 엔진 크롤러가 완성된 HTML을 받아 색인 가능
>    - meta 태그, 구조화된 데이터 제공 용이
>
> 3. **소셜 미디어 공유 최적화**
>    - Open Graph, Twitter Card 등 메타 정보를 동적으로 생성
>    - 각 페이지마다 다른 미리보기 이미지/제목 설정 가능
>
> 4. **최신 데이터 보장**
>    - 매 요청마다 서버에서 데이터를 가져와 렌더링
>    - 실시간성이 중요한 애플리케이션에 적합
>
> **단점**
> 1. **서버 부하 증가**
>    - 매 요청마다 서버에서 렌더링 수행
>    - 트래픽 증가 시 서버 확장 필요
>
> 2. **TTFB (Time To First Byte) 증가**
>    - 서버에서 HTML 생성 후 전송하므로 응답 시간 증가
>    - 데이터베이스 쿼리, API 호출 시간 포함
>
> 3. **복잡한 캐싱 전략 필요**
>    - 동적 콘텐츠 캐싱이 어려움
>    - CDN 캐싱 효과 제한적
>
> 4. **서버 환경 제약**
>    - window, document 등 브라우저 API 사용 불가
>    - 서버와 클라이언트 환경 차이 고려 필요

---

**Q3.** Next.js의 핵심 기능들을 설명해주세요.

> 1. **하이브리드 렌더링**
>    - SSR, SSG, ISR, CSR을 페이지별로 선택 가능
>    - 최적의 렌더링 전략 조합 사용
>
> 2. **파일 기반 라우팅**
>    - 파일 시스템이 곧 라우팅 구조
>    - app/ 또는 pages/ 디렉토리 기반 자동 라우팅
>
> 3. **자동 코드 분할 (Code Splitting)**
>    - 페이지별로 자동으로 번들 분리
>    - 필요한 코드만 로드하여 성능 최적화
>
> 4. **이미지 최적화 (next/image)**
>    - 자동 이미지 최적화, lazy loading
>    - WebP, AVIF 등 최신 포맷 자동 변환
>
> 5. **API Routes**
>    - 백엔드 API를 Next.js 내에서 구현 가능
>    - Serverless Functions로 배포
>
> 6. **빌트인 CSS/Sass 지원**
>    - CSS Modules, Sass, CSS-in-JS 등 다양한 스타일링 방식 지원
>
> 7. **Fast Refresh**
>    - 상태를 유지하면서 빠른 Hot Module Replacement
>
> 8. **TypeScript 지원**
>    - 기본적으로 TypeScript 완벽 지원
>
> 9. **국제화 (i18n) 라우팅**
>    - 다국어 지원을 위한 빌트인 기능
>
> 10. **미들웨어**
>     - 요청 처리 전 로직 실행 (인증, 리다이렉션 등)

---

**Q4.** Hydration이 무엇인지, 그리고 Hydration Mismatch는 왜 발생하는지 설명해주세요.

> **Hydration이란**
> - SSR로 생성된 정적 HTML에 JavaScript를 "주입"하여 인터랙티브하게 만드는 과정
> - 서버에서 렌더링된 HTML을 클라이언트에서 React가 "재활용"
> - DOM 노드에 이벤트 리스너를 연결하고 상태를 초기화
>
> **동작 과정**
> ```
> 1. 서버: React 컴포넌트 → HTML 문자열 생성
> 2. 브라우저: HTML 받아서 즉시 화면 표시 (아직 인터랙티브 X)
> 3. 브라우저: JavaScript 다운로드 및 실행
> 4. 브라우저: React가 서버 HTML과 클라이언트 컴포넌트 트리 비교
> 5. 브라우저: 일치하면 이벤트 리스너만 추가 (빠름)
> 6. 완료: 인터랙티브한 페이지
> ```
>
> **Hydration Mismatch 발생 원인**
> 1. **서버와 클라이언트 환경 차이**
>    ```jsx
>    // 잘못된 예: 서버와 클라이언트에서 다른 값
>    function Component() {
>      return <div>{new Date().toLocaleString()}</div>
>    }
>    ```
>
> 2. **브라우저 전용 API 사용**
>    ```jsx
>    // 잘못된 예: window는 서버에 없음
>    function Component() {
>      return <div>{window.innerWidth}</div>
>    }
>    ```
>
> 3. **잘못된 HTML 중첩**
>    ```jsx
>    // 잘못된 예: <p> 안에 <div> 중첩 불가
>    <p><div>내용</div></p>
>    ```
>
> 4. **조건부 렌더링 불일치**
>    ```jsx
>    // 잘못된 예
>    function Component() {
>      const [mounted, setMounted] = useState(false);
>      useEffect(() => setMounted(true), []);
>      if (!mounted) return null; // 서버: null, 클라이언트: 실제 콘텐츠
>      return <div>콘텐츠</div>;
>    }
>    ```
>
> **해결 방법**
> ```jsx
> // 1. useEffect로 클라이언트 전용 로직 처리
> function Component() {
>   const [time, setTime] = useState('');
>   useEffect(() => {
>     setTime(new Date().toLocaleString());
>   }, []);
>   return <div>{time || '로딩 중...'}</div>;
> }
>
> // 2. suppressHydrationWarning 사용 (신중하게)
> <div suppressHydrationWarning>{new Date().toLocaleString()}</div>
>
> // 3. dynamic import with ssr: false
> const ClientOnlyComponent = dynamic(
>   () => import('./ClientOnly'),
>   { ssr: false }
> );
> ```

---

**Q5.** Next.js에서 SEO 최적화를 위해 어떤 기능들을 사용할 수 있나요?

> **1. Metadata API (App Router)**
> ```tsx
> // app/layout.tsx
> export const metadata = {
>   title: '사이트 제목',
>   description: '사이트 설명',
>   openGraph: {
>     title: 'OG 제목',
>     description: 'OG 설명',
>     images: ['/og-image.jpg'],
>   },
> };
>
> // 동적 메타데이터
> export async function generateMetadata({ params }) {
>   const product = await fetchProduct(params.id);
>   return {
>     title: product.name,
>     description: product.description,
>   };
> }
> ```
>
> **2. Head 컴포넌트 (Pages Router)**
> ```tsx
> import Head from 'next/head';
>
> function Page() {
>   return (
>     <>
>       <Head>
>         <title>페이지 제목</title>
>         <meta name="description" content="페이지 설명" />
>         <meta property="og:title" content="OG 제목" />
>       </Head>
>       <main>콘텐츠</main>
>     </>
>   );
> }
> ```
>
> **3. Sitemap 생성**
> ```tsx
> // app/sitemap.ts
> export default function sitemap() {
>   return [
>     {
>       url: 'https://example.com',
>       lastModified: new Date(),
>       changeFrequency: 'daily',
>       priority: 1,
>     },
>     {
>       url: 'https://example.com/about',
>       lastModified: new Date(),
>       changeFrequency: 'monthly',
>       priority: 0.8,
>     },
>   ];
> }
> ```
>
> **4. robots.txt**
> ```tsx
> // app/robots.ts
> export default function robots() {
>   return {
>     rules: {
>       userAgent: '*',
>       allow: '/',
>       disallow: '/private/',
>     },
>     sitemap: 'https://example.com/sitemap.xml',
>   };
> }
> ```
>
> **5. 구조화된 데이터 (JSON-LD)**
> ```tsx
> function ProductPage({ product }) {
>   const jsonLd = {
>     '@context': 'https://schema.org',
>     '@type': 'Product',
>     name: product.name,
>     image: product.image,
>     description: product.description,
>     offers: {
>       '@type': 'Offer',
>       price: product.price,
>       priceCurrency: 'KRW',
>     },
>   };
>
>   return (
>     <>
>       <script
>         type="application/ld+json"
>         dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
>       />
>       <div>{product.name}</div>
>     </>
>   );
> }
> ```
>
> **6. 시맨틱 HTML 사용**
> ```tsx
> <article>
>   <header>
>     <h1>제목</h1>
>   </header>
>   <section>
>     <h2>소제목</h2>
>     <p>내용</p>
>   </section>
> </article>
> ```

---

**Q6.** Next.js의 App Router와 Pages Router의 차이점을 설명해주세요.

> | 구분 | Pages Router | App Router |
> |------|--------------|------------|
> | **디렉토리** | `pages/` | `app/` |
> | **라우팅 파일** | `index.js`, `[id].js` | `page.js`, `layout.js` |
> | **기본 컴포넌트** | Client Component | Server Component |
> | **레이아웃** | `_app.js`, `_document.js` | `layout.js` (중첩 가능) |
> | **로딩 상태** | 수동 구현 | `loading.js` |
> | **에러 처리** | `_error.js` | `error.js` (세밀한 제어) |
> | **데이터 페칭** | `getServerSideProps`, `getStaticProps` | `fetch` with cache options |
> | **스트리밍** | 지원 안 함 | Suspense 기반 스트리밍 지원 |
> | **병렬 라우팅** | 지원 안 함 | `@folder` 문법 지원 |
> | **인터셉팅 라우팅** | 지원 안 함 | `(.)folder` 문법 지원 |
>
> **Pages Router 예시**
> ```
> pages/
>   _app.js           # 전역 레이아웃
>   _document.js      # HTML 문서 구조
>   index.js          # /
>   about.js          # /about
>   blog/
>     [slug].js       # /blog/:slug
> ```
>
> **App Router 예시**
> ```
> app/
>   layout.js         # 루트 레이아웃
>   page.js           # /
>   loading.js        # 로딩 UI
>   error.js          # 에러 UI
>   about/
>     page.js         # /about
>   blog/
>     [slug]/
>       page.js       # /blog/:slug
> ```
>
> **App Router 장점**
> 1. Server Components 기본 지원 → 번들 크기 감소
> 2. 중첩 레이아웃으로 코드 재사용 증가
> 3. 스트리밍 SSR로 더 빠른 초기 렌더링
> 4. 파일 기반 로딩/에러 처리 → 보일러플레이트 감소
> 5. 병렬/인터셉팅 라우팅으로 복잡한 UI 패턴 구현 가능

---

**Q7.** Server Components와 Client Components의 차이점은 무엇이며, 언제 각각을 사용해야 하나요?

> **Server Components (기본값)**
> - 서버에서만 렌더링, JavaScript가 클라이언트로 전송되지 않음
> - 데이터베이스, 파일 시스템 직접 접근 가능
> - 백엔드 리소스에 직접 접근 (API 토큰, 민감한 정보)
> - 번들 크기 감소
> - 자동으로 코드 분할
>
> **Client Components ('use client' 선언)**
> - 브라우저에서 실행
> - 상태, 이벤트 리스너, 브라우저 API 사용 가능
> - useEffect, useState 등 React 훅 사용
>
> **차이점 비교**
> | 기능 | Server Component | Client Component |
> |------|------------------|------------------|
> | **데이터 페칭** | ✅ 직접 가능 (await) | ❌ useEffect 등 사용 |
> | **백엔드 리소스** | ✅ 직접 접근 | ❌ API 통해서만 |
> | **민감한 정보** | ✅ 서버에만 유지 | ❌ 노출 위험 |
> | **대용량 라이브러리** | ✅ 번들에 포함 안 됨 | ❌ 번들 크기 증가 |
> | **상태 관리** | ❌ 불가능 | ✅ useState, useReducer |
> | **이벤트 리스너** | ❌ 불가능 | ✅ onClick, onChange |
> | **useEffect** | ❌ 불가능 | ✅ 사용 가능 |
> | **브라우저 API** | ❌ 불가능 | ✅ window, document |
>
> **Server Component 사용 시기**
> ```tsx
> // 데이터 페칭
> async function ProductList() {
>   const products = await db.product.findMany(); // 직접 DB 접근
>   return (
>     <ul>
>       {products.map(p => <li key={p.id}>{p.name}</li>)}
>     </ul>
>   );
> }
>
> // 대용량 라이브러리 사용
> import { Markdown } from 'huge-markdown-lib'; // 번들에 포함 안 됨
>
> function Article({ content }) {
>   return <Markdown>{content}</Markdown>;
> }
> ```
>
> **Client Component 사용 시기**
> ```tsx
> 'use client';
>
> // 상태 관리
> function Counter() {
>   const [count, setCount] = useState(0);
>   return <button onClick={() => setCount(count + 1)}>{count}</button>;
> }
>
> // 이벤트 리스너
> function SearchBox() {
>   return <input onChange={(e) => console.log(e.target.value)} />;
> }
>
> // 브라우저 API
> function WindowSize() {
>   const [size, setSize] = useState(0);
>   useEffect(() => {
>     setSize(window.innerWidth);
>   }, []);
>   return <div>{size}px</div>;
> }
> ```
>
> **조합 패턴**
> ```tsx
> // Server Component (부모)
> async function ProductPage({ id }) {
>   const product = await fetchProduct(id); // 서버에서 데이터 페칭
>
>   return (
>     <div>
>       <h1>{product.name}</h1>
>       <AddToCartButton product={product} /> {/* Client Component */}
>     </div>
>   );
> }
>
> // Client Component (자식)
> 'use client';
> function AddToCartButton({ product }) {
>   return (
>     <button onClick={() => addToCart(product)}>
>       장바구니 담기
>     </button>
>   );
> }
> ```
>
> **권장 패턴**
> - 기본은 Server Component 사용
> - 인터랙션 필요한 부분만 Client Component로 분리
> - Client Component는 리프 노드(말단)에 배치
> - 서버 데이터를 props로 클라이언트에 전달

---

**Q8.** Pages Router의 getServerSideProps, getStaticProps, getStaticPaths의 차이점을 설명해주세요.

> **getServerSideProps (SSR)**
> - 매 요청마다 서버에서 실행
> - 실시간 데이터 필요할 때 사용
> - 서버 부하 높음, TTFB 느림
>
> ```tsx
> // 페이지가 요청될 때마다 실행
> export async function getServerSideProps(context) {
>   const { params, req, res, query } = context;
>
>   // API 호출, DB 쿼리 등
>   const data = await fetch(`https://api.example.com/data`);
>   const result = await data.json();
>
>   return {
>     props: {
>       data: result, // 컴포넌트에 props로 전달
>     },
>   };
> }
>
> function Page({ data }) {
>   return <div>{data.title}</div>;
> }
> ```
>
> **getStaticProps (SSG)**
> - 빌드 타임에 한 번만 실행
> - 정적 HTML 생성
> - 매우 빠름, CDN 캐싱 가능
>
> ```tsx
> // 빌드 시에만 실행
> export async function getStaticProps() {
>   const posts = await fetchPosts();
>
>   return {
>     props: {
>       posts,
>     },
>     revalidate: 60, // ISR: 60초마다 재생성
>   };
> }
>
> function BlogList({ posts }) {
>   return (
>     <ul>
>       {posts.map(post => <li key={post.id}>{post.title}</li>)}
>     </ul>
>   );
> }
> ```
>
> **getStaticPaths (SSG with Dynamic Routes)**
> - 동적 라우트의 경로를 미리 지정
> - getStaticProps와 함께 사용
>
> ```tsx
> // 어떤 경로들을 미리 생성할지 지정
> export async function getStaticPaths() {
>   const posts = await fetchAllPosts();
>
>   const paths = posts.map(post => ({
>     params: { id: post.id.toString() },
>   }));
>
>   return {
>     paths, // [{ params: { id: '1' }}, { params: { id: '2' }}]
>     fallback: 'blocking', // 또는 true, false
>   };
> }
>
> export async function getStaticProps({ params }) {
>   const post = await fetchPost(params.id);
>
>   return {
>     props: { post },
>   };
> }
>
> function Post({ post }) {
>   return <article>{post.content}</article>;
> }
> ```
>
> **fallback 옵션**
> - `false`: paths에 없는 경로는 404
> - `true`: paths에 없는 경로는 첫 요청 시 생성, 로딩 상태 필요
> - `'blocking'`: paths에 없는 경로는 첫 요청 시 생성, SSR처럼 대기
>
> ```tsx
> function Post({ post }) {
>   const router = useRouter();
>
>   // fallback: true일 때 필요
>   if (router.isFallback) {
>     return <div>로딩 중...</div>;
>   }
>
>   return <article>{post.content}</article>;
> }
> ```
>
> **비교 표**
> | 메서드 | 실행 시점 | 사용 시기 | 캐싱 |
> |--------|----------|-----------|------|
> | **getServerSideProps** | 매 요청 | 실시간 데이터 | 어려움 |
> | **getStaticProps** | 빌드 시 | 정적 콘텐츠 | CDN 가능 |
> | **getStaticPaths** | 빌드 시 | 동적 라우트 경로 지정 | - |
>
> **ISR (Incremental Static Regeneration)**
> ```tsx
> export async function getStaticProps() {
>   return {
>     props: { data },
>     revalidate: 10, // 10초마다 백그라운드에서 재생성
>   };
> }
> ```
> - SSG의 속도 + 데이터 최신성
> - revalidate 초 후 첫 요청 시 백그라운드 재생성
> - 재생성 중에도 기존 캐시 페이지 제공

---

**Q9.** Edge Runtime과 Node.js Runtime의 차이점은 무엇인가요?

> **Node.js Runtime (기본값)**
> - 전통적인 Node.js 환경
> - 모든 Node.js API 사용 가능
> - 파일 시스템, 네이티브 모듈 접근
> - Cold Start 느림 (수백 ms)
> - 리전별 배포
>
> **Edge Runtime**
> - 경량 JavaScript 런타임
> - Vercel Edge Network에서 실행
> - 사용자에게 가장 가까운 엣지 서버에서 실행
> - Cold Start 매우 빠름 (수십 ms)
> - 제한된 API (Web API 표준)
>
> **차이점 비교**
> | 구분 | Node.js Runtime | Edge Runtime |
> |------|----------------|--------------|
> | **실행 위치** | 특정 리전 서버 | 전세계 엣지 네트워크 |
> | **Cold Start** | 느림 (100-500ms) | 빠름 (10-50ms) |
> | **메모리** | 최대 3GB | 제한적 |
> | **실행 시간** | 최대 60초 | 최대 30초 |
> | **Node.js API** | ✅ 모두 사용 가능 | ❌ 불가능 |
> | **파일 시스템** | ✅ fs 모듈 | ❌ 불가능 |
> | **네이티브 모듈** | ✅ 가능 | ❌ 불가능 |
> | **Web API** | 일부 | ✅ 전체 |
> | **사용 사례** | 복잡한 로직, DB 연결 | 인증, 리다이렉션, A/B 테스트 |
>
> **Edge Runtime 설정**
> ```tsx
> // app/api/hello/route.ts
> export const runtime = 'edge'; // Edge Runtime 사용
>
> export async function GET(request: Request) {
>   return new Response('Hello from Edge!');
> }
>
> // middleware.ts (자동으로 Edge)
> export function middleware(request: NextRequest) {
>   return NextResponse.redirect(new URL('/login', request.url));
> }
> ```
>
> **Edge Runtime에서 사용 가능한 API**
> ```tsx
> // ✅ Web API 사용 가능
> fetch('https://api.example.com')
> new Request(), new Response()
> URL, URLSearchParams
> crypto.randomUUID()
> btoa(), atob()
> TextEncoder, TextDecoder
>
> // ❌ Node.js API 사용 불가
> fs.readFile() // 에러
> path.join()   // 에러
> Buffer        // 에러
> process.env   // 제한적 (환경변수는 가능)
> ```
>
> **Edge Runtime 사용 사례**
> ```tsx
> // 1. 미들웨어 (인증, 리다이렉션)
> export function middleware(request: NextRequest) {
>   const token = request.cookies.get('token');
>   if (!token) {
>     return NextResponse.redirect(new URL('/login', request.url));
>   }
> }
>
> // 2. 간단한 API (지역 기반 콘텐츠)
> export const runtime = 'edge';
>
> export async function GET(request: Request) {
>   const geo = request.headers.get('x-vercel-ip-country');
>   return Response.json({ country: geo });
> }
>
> // 3. A/B 테스트
> export function middleware(request: NextRequest) {
>   const bucket = Math.random() < 0.5 ? 'a' : 'b';
>   const response = NextResponse.next();
>   response.cookies.set('bucket', bucket);
>   return response;
> }
> ```
>
> **선택 기준**
> - **Edge Runtime 사용**: 빠른 응답 필요, 간단한 로직, 전세계 사용자 대상
> - **Node.js Runtime 사용**: 복잡한 로직, DB 연결, 파일 시스템 접근, 네이티브 모듈 필요

---

**Q10.** Next.js App Router에서 데이터를 가져오는 방법과 캐싱 전략에 대해 설명해주세요.

> **데이터 페칭 방법**
>
> **1. Server Component에서 직접 fetch**
> ```tsx
> // app/posts/page.tsx
> async function PostList() {
>   // 서버에서 직접 실행, await 가능
>   const res = await fetch('https://api.example.com/posts');
>   const posts = await res.json();
>
>   return (
>     <ul>
>       {posts.map(post => <li key={post.id}>{post.title}</li>)}
>     </ul>
>   );
> }
> ```
>
> **2. fetch 캐싱 옵션**
> ```tsx
> // 기본: 'force-cache' (무기한 캐시)
> fetch('https://api.example.com/posts');
>
> // 캐시 안 함 (항상 최신 데이터)
> fetch('https://api.example.com/posts', { cache: 'no-store' });
>
> // 10초마다 재검증
> fetch('https://api.example.com/posts', { next: { revalidate: 10 } });
>
> // 특정 태그로 재검증
> fetch('https://api.example.com/posts', { next: { tags: ['posts'] } });
> ```
>
> **3. 라우트 세그먼트 설정**
> ```tsx
> // app/posts/page.tsx
>
> // 정적 렌더링 (기본값)
> export const dynamic = 'auto';
>
> // 동적 렌더링 (매 요청마다)
> export const dynamic = 'force-dynamic';
>
> // 정적 렌더링 강제
> export const dynamic = 'force-static';
>
> // 에러 발생 시 정적 렌더링
> export const dynamic = 'error';
>
> // 재검증 시간 설정 (ISR)
> export const revalidate = 60; // 60초
>
> // 재검증 안 함
> export const revalidate = false;
>
> // 매 요청마다 재검증
> export const revalidate = 0;
> ```
>
> **4. 데이터베이스 직접 접근**
> ```tsx
> import { db } from '@/lib/db';
>
> async function UserProfile({ id }) {
>   // ORM 사용
>   const user = await db.user.findUnique({
>     where: { id },
>   });
>
>   return <div>{user.name}</div>;
> }
> ```
>
> **5. 병렬 데이터 페칭**
> ```tsx
> async function Dashboard() {
>   // 병렬로 실행
>   const [user, posts, comments] = await Promise.all([
>     fetchUser(),
>     fetchPosts(),
>     fetchComments(),
>   ]);
>
>   return (
>     <div>
>       <User data={user} />
>       <Posts data={posts} />
>       <Comments data={comments} />
>     </div>
>   );
> }
> ```
>
> **6. 순차 데이터 페칭**
> ```tsx
> async function Page() {
>   // user를 먼저 가져온 후
>   const user = await fetchUser();
>
>   // user.id를 사용해 posts 가져오기
>   const posts = await fetchPosts(user.id);
>
>   return <div>...</div>;
> }
> ```
>
> **캐싱 전략**
>
> **1. Request Memoization (자동)**
> - 같은 렌더 사이클에서 동일한 fetch 요청은 자동으로 중복 제거
> ```tsx
> async function Component1() {
>   const data = await fetch('/api/data'); // 실제 요청
> }
>
> async function Component2() {
>   const data = await fetch('/api/data'); // 메모이제이션된 결과 재사용
> }
> ```
>
> **2. Data Cache**
> - fetch 결과를 서버에 영구 저장
> ```tsx
> // 캐시됨 (기본값)
> fetch('https://api.example.com/posts');
>
> // 10초 후 재검증
> fetch('https://api.example.com/posts', { next: { revalidate: 10 } });
> ```
>
> **3. 온디맨드 재검증**
> ```tsx
> // app/actions.ts
> 'use server';
>
> import { revalidatePath, revalidateTag } from 'next/cache';
>
> export async function createPost(data) {
>   await db.post.create(data);
>
>   // 경로 재검증
>   revalidatePath('/posts');
>
>   // 태그 재검증
>   revalidateTag('posts');
> }
>
> // 사용
> fetch('https://api.example.com/posts', {
>   next: { tags: ['posts'] }
> });
> ```
>
> **4. Client Component 데이터 페칭**
> ```tsx
> 'use client';
>
> import useSWR from 'swr';
>
> function Posts() {
>   const { data, error, isLoading } = useSWR('/api/posts', fetcher);
>
>   if (isLoading) return <div>로딩 중...</div>;
>   if (error) return <div>에러 발생</div>;
>
>   return <div>{data.map(...)}</div>;
> }
> ```
>
> **캐싱 전략 선택 가이드**
> | 데이터 특성 | 권장 방식 | 설정 |
> |-----------|----------|------|
> | 정적 콘텐츠 | SSG | `revalidate: false` |
> | 자주 변경 | ISR | `revalidate: 60` |
> | 실시간 | SSR | `cache: 'no-store'` |
> | 사용자별 | SSR + Client | `dynamic: 'force-dynamic'` |
> | 온디맨드 갱신 | ISR + revalidate | `revalidatePath()` |

---

**Q11.** Streaming SSR이 무엇이며, 어떻게 구현하나요?

> **Streaming SSR이란**
> - 서버에서 HTML을 청크 단위로 점진적으로 전송
> - 전체 페이지가 준비될 때까지 기다리지 않고, 준비된 부분부터 전송
> - 사용자는 빠르게 콘텐츠를 볼 수 있음
> - React 18의 Suspense와 결합하여 동작
>
> **전통적인 SSR vs Streaming SSR**
> ```
> [전통적인 SSR]
> 서버: 데이터 페칭 → 전체 HTML 생성 → 전송
> 클라이언트: 대기... → HTML 수신 → 화면 표시
> (느린 데이터 하나가 전체 페이지를 막음)
>
> [Streaming SSR]
> 서버: HTML 헤더 전송 → 준비된 부분 전송 → 나머지 전송
> 클라이언트: HTML 수신 → 점진적 화면 표시 → 추가 콘텐츠 표시
> (빠른 부분은 즉시 표시, 느린 부분은 나중에 표시)
> ```
>
> **구현 방법**
>
> **1. loading.tsx (자동 스트리밍)**
> ```tsx
> // app/dashboard/loading.tsx
> export default function Loading() {
>   return <div>대시보드 로딩 중...</div>;
> }
>
> // app/dashboard/page.tsx
> async function Dashboard() {
>   const data = await fetchDashboardData(); // 느린 데이터 페칭
>   return <div>{data}</div>;
> }
> ```
> - Next.js가 자동으로 Suspense로 감싸서 스트리밍
> - 페이지가 로딩되는 동안 loading.tsx 표시
>
> **2. Suspense 수동 사용**
> ```tsx
> // app/page.tsx
> import { Suspense } from 'react';
>
> async function SlowComponent() {
>   // 느린 데이터 페칭 (3초 소요)
>   const data = await fetchSlowData();
>   return <div>{data}</div>;
> }
>
> async function FastComponent() {
>   // 빠른 데이터 페칭 (0.5초 소요)
>   const data = await fetchFastData();
>   return <div>{data}</div>;
> }
>
> export default function Page() {
>   return (
>     <div>
>       <h1>페이지 제목 (즉시 표시)</h1>
>
>       {/* FastComponent는 빠르게 표시 */}
>       <Suspense fallback={<div>빠른 콘텐츠 로딩 중...</div>}>
>         <FastComponent />
>       </Suspense>
>
>       {/* SlowComponent는 나중에 표시 */}
>       <Suspense fallback={<div>느린 콘텐츠 로딩 중...</div>}>
>         <SlowComponent />
>       </Suspense>
>     </div>
>   );
> }
> ```
>
> **3. 중첩된 Suspense (세밀한 제어)**
> ```tsx
> export default function Page() {
>   return (
>     <div>
>       <Header /> {/* 즉시 표시 */}
>
>       <Suspense fallback={<DashboardSkeleton />}>
>         <Dashboard>
>           <Suspense fallback={<ChartSkeleton />}>
>             <Chart /> {/* 차트 데이터 로딩 */}
>           </Suspense>
>
>           <Suspense fallback={<TableSkeleton />}>
>             <Table /> {/* 테이블 데이터 로딩 */}
>           </Suspense>
>         </Dashboard>
>       </Suspense>
>     </div>
>   );
> }
> ```
>
> **4. 실전 예시: 제품 상세 페이지**
> ```tsx
> // app/products/[id]/page.tsx
> async function ProductInfo({ id }) {
>   const product = await fetchProduct(id); // 빠름
>   return (
>     <div>
>       <h1>{product.name}</h1>
>       <p>{product.price}</p>
>     </div>
>   );
> }
>
> async function Reviews({ productId }) {
>   const reviews = await fetchReviews(productId); // 느림
>   return (
>     <div>
>       {reviews.map(review => <Review key={review.id} {...review} />)}
>     </div>
>   );
> }
>
> async function Recommendations({ productId }) {
>   const recs = await fetchRecommendations(productId); // 매우 느림
>   return (
>     <div>
>       {recs.map(product => <ProductCard key={product.id} {...product} />)}
>     </div>
>   );
> }
>
> export default function ProductPage({ params }) {
>   return (
>     <div>
>       {/* 제품 정보는 즉시 표시 */}
>       <Suspense fallback={<ProductSkeleton />}>
>         <ProductInfo id={params.id} />
>       </Suspense>
>
>       {/* 리뷰는 조금 후에 표시 */}
>       <Suspense fallback={<ReviewsSkeleton />}>
>         <Reviews productId={params.id} />
>       </Suspense>
>
>       {/* 추천 상품은 가장 나중에 표시 */}
>       <Suspense fallback={<RecommendationsSkeleton />}>
>         <Recommendations productId={params.id} />
>       </Suspense>
>     </div>
>   );
> }
> ```
>
> **장점**
> 1. **빠른 TTFB (Time To First Byte)**: 일부 HTML을 즉시 전송
> 2. **개선된 FCP (First Contentful Paint)**: 사용자가 콘텐츠를 빠르게 봄
> 3. **병렬 데이터 페칭**: 느린 데이터가 빠른 데이터를 막지 않음
> 4. **우선순위 제어**: 중요한 콘텐츠를 먼저 표시
> 5. **더 나은 사용자 경험**: 빈 화면 대신 로딩 스켈레톤 표시
>
> **주의사항**
> - Suspense는 Server Component에서만 데이터 페칭에 사용 가능
> - Client Component에서는 다른 라이브러리(SWR, React Query) 필요
> - 너무 많은 Suspense 경계는 복잡도 증가

---

**Q12.** next/image의 이점과 주요 기능을 설명해주세요.

> **주요 이점**
> 1. **자동 이미지 최적화**: WebP, AVIF 등 최신 포맷으로 자동 변환
> 2. **반응형 이미지**: 디바이스 크기에 맞는 이미지 제공
> 3. **Lazy Loading**: 뷰포트에 들어올 때만 로드
> 4. **레이아웃 시프트 방지**: CLS (Cumulative Layout Shift) 개선
> 5. **자동 크기 조정**: 원본 이미지를 여러 크기로 생성
>
> **기본 사용법**
> ```tsx
> import Image from 'next/image';
>
> // 1. 로컬 이미지 (빌드 시 크기 자동 감지)
> import profilePic from '../public/me.png';
>
> export default function Page() {
>   return (
>     <Image
>       src={profilePic}
>       alt="프로필 사진"
>       // width, height 자동 설정됨
>     />
>   );
> }
>
> // 2. 외부 이미지 (width, height 필수)
> <Image
>   src="https://example.com/photo.jpg"
>   alt="설명"
>   width={500}
>   height={300}
> />
> ```
>
> **레이아웃 모드**
> ```tsx
> // 1. 고정 크기
> <Image
>   src="/photo.jpg"
>   width={500}
>   height={300}
>   alt="고정 크기"
> />
>
> // 2. 반응형 (컨테이너에 맞춤)
> <div style={{ position: 'relative', width: '100%', height: '400px' }}>
>   <Image
>     src="/photo.jpg"
>     alt="반응형"
>     fill
>     style={{ objectFit: 'cover' }}
>   />
> </div>
>
> // 3. sizes 속성으로 최적화
> <Image
>   src="/photo.jpg"
>   alt="반응형 최적화"
>   fill
>   sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
> />
> ```
>
> **우선순위 설정**
> ```tsx
> // LCP(Largest Contentful Paint) 이미지는 priority 설정
> <Image
>   src="/hero.jpg"
>   alt="히어로 이미지"
>   width={1200}
>   height={600}
>   priority // Lazy loading 비활성화, 미리 로드
> />
> ```
>
> **로딩 상태**
> ```tsx
> // 1. 블러 플레이스홀더 (로컬 이미지)
> import profilePic from '../public/me.png';
>
> <Image
>   src={profilePic}
>   alt="프로필"
>   placeholder="blur" // 자동으로 블러 생성
> />
>
> // 2. 커스텀 블러 플레이스홀더 (외부 이미지)
> <Image
>   src="https://example.com/photo.jpg"
>   alt="사진"
>   width={500}
>   height={300}
>   placeholder="blur"
>   blurDataURL="data:image/jpeg;base64,/9j/4AAQSkZJRg..." // Base64 블러 이미지
> />
>
> // 3. 로딩 이벤트
> <Image
>   src="/photo.jpg"
>   alt="사진"
>   width={500}
>   height={300}
>   onLoadingComplete={(img) => console.log('로딩 완료', img.naturalWidth)}
> />
> ```
>
> **외부 이미지 도메인 설정**
> ```tsx
> // next.config.js
> module.exports = {
>   images: {
>     // 허용할 외부 도메인
>     remotePatterns: [
>       {
>         protocol: 'https',
>         hostname: 'example.com',
>         port: '',
>         pathname: '/images/**',
>       },
>     ],
>
>     // 이미지 크기 설정
>     deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
>     imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
>
>     // 포맷 설정
>     formats: ['image/webp', 'image/avif'],
>
>     // 품질 설정 (1-100, 기본값 75)
>     quality: 80,
>   },
> };
> ```
>
> **스타일링**
> ```tsx
> // 1. className 사용
> <Image
>   src="/photo.jpg"
>   alt="사진"
>   width={500}
>   height={300}
>   className="rounded-lg shadow-md"
> />
>
> // 2. style 사용
> <Image
>   src="/photo.jpg"
>   alt="사진"
>   width={500}
>   height={300}
>   style={{ borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
> />
> ```
>
> **실전 예시: 이미지 갤러리**
> ```tsx
> 'use client';
>
> import Image from 'next/image';
> import { useState } from 'react';
>
> export default function Gallery({ images }) {
>   const [loaded, setLoaded] = useState({});
>
>   return (
>     <div className="grid grid-cols-3 gap-4">
>       {images.map((img, index) => (
>         <div key={img.id} className="relative aspect-square">
>           <Image
>             src={img.url}
>             alt={img.alt}
>             fill
>             sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
>             className={`object-cover transition-opacity duration-300 ${
>               loaded[img.id] ? 'opacity-100' : 'opacity-0'
>             }`}
>             onLoadingComplete={() => setLoaded({ ...loaded, [img.id]: true })}
>             placeholder="blur"
>             blurDataURL={img.blurDataURL}
>           />
>         </div>
>       ))}
>     </div>
>   );
> }
> ```
>
> **성능 측정**
> - LCP 개선: priority 속성 사용
> - CLS 방지: width/height 또는 fill 사용
> - 대역폭 절약: sizes 속성으로 적절한 크기만 로드

---

**Q13.** Next.js의 미들웨어(middleware.ts)는 어떤 역할을 하며, 어떤 경우에 사용하나요?

> **미들웨어란**
> - 요청이 완료되기 전에 실행되는 코드
> - Edge Runtime에서 실행 (빠른 응답)
> - 요청/응답을 수정하거나, 리다이렉트, 리라이트 수행
>
> **기본 구조**
> ```tsx
> // middleware.ts (프로젝트 루트 또는 app/ 디렉토리)
> import { NextResponse } from 'next/server';
> import type { NextRequest } from 'next/server';
>
> export function middleware(request: NextRequest) {
>   // 요청 처리 로직
>   return NextResponse.next();
> }
>
> // 미들웨어가 실행될 경로 설정
> export const config = {
>   matcher: '/about/:path*', // /about으로 시작하는 모든 경로
> };
> ```
>
> **주요 사용 사례**
>
> **1. 인증 및 권한 확인**
> ```tsx
> import { NextResponse } from 'next/server';
> import type { NextRequest } from 'next/server';
>
> export function middleware(request: NextRequest) {
>   const token = request.cookies.get('token')?.value;
>
>   // 로그인 페이지는 건너뛰기
>   if (request.nextUrl.pathname.startsWith('/login')) {
>     return NextResponse.next();
>   }
>
>   // 토큰 없으면 로그인 페이지로 리다이렉트
>   if (!token) {
>     return NextResponse.redirect(new URL('/login', request.url));
>   }
>
>   // 토큰 검증 (간단한 예시)
>   try {
>     // JWT 검증 로직
>     const isValid = verifyToken(token);
>     if (!isValid) {
>       return NextResponse.redirect(new URL('/login', request.url));
>     }
>   } catch (error) {
>     return NextResponse.redirect(new URL('/login', request.url));
>   }
>
>   return NextResponse.next();
> }
>
> export const config = {
>   matcher: ['/dashboard/:path*', '/profile/:path*'],
> };
> ```
>
> **2. 지역 기반 리다이렉션**
> ```tsx
> export function middleware(request: NextRequest) {
>   const country = request.geo?.country || 'US';
>   const locale = request.cookies.get('locale')?.value;
>
>   // 쿠키에 지역 설정이 없으면 자동 설정
>   if (!locale) {
>     const response = NextResponse.next();
>     response.cookies.set('locale', country);
>     return response;
>   }
>
>   // 한국 사용자는 /ko로 리다이렉트
>   if (country === 'KR' && !request.nextUrl.pathname.startsWith('/ko')) {
>     return NextResponse.redirect(new URL(`/ko${request.nextUrl.pathname}`, request.url));
>   }
>
>   return NextResponse.next();
> }
> ```
>
> **3. A/B 테스트**
> ```tsx
> export function middleware(request: NextRequest) {
>   // 기존 버킷 확인
>   let bucket = request.cookies.get('bucket')?.value;
>
>   if (!bucket) {
>     // 50:50으로 분배
>     bucket = Math.random() < 0.5 ? 'a' : 'b';
>     const response = NextResponse.next();
>     response.cookies.set('bucket', bucket);
>     return response;
>   }
>
>   // 버킷에 따라 다른 페이지로 리라이트
>   if (bucket === 'b') {
>     return NextResponse.rewrite(new URL('/experimental', request.url));
>   }
>
>   return NextResponse.next();
> }
> ```
>
> **4. 봇 감지 및 차단**
> ```tsx
> export function middleware(request: NextRequest) {
>   const userAgent = request.headers.get('user-agent') || '';
>
>   // 악성 봇 차단
>   const badBots = ['BadBot', 'Scraper', 'EvilBot'];
>   if (badBots.some(bot => userAgent.includes(bot))) {
>     return new NextResponse('Access Denied', { status: 403 });
>   }
>
>   // 검색 엔진 봇에게 다른 콘텐츠 제공
>   const isBot = /bot|crawler|spider/i.test(userAgent);
>   if (isBot) {
>     const response = NextResponse.next();
>     response.headers.set('X-Is-Bot', 'true');
>     return response;
>   }
>
>   return NextResponse.next();
> }
> ```
>
> **5. Rate Limiting**
> ```tsx
> import { Redis } from '@upstash/redis';
>
> const redis = new Redis({ /* 설정 */ });
>
> export async function middleware(request: NextRequest) {
>   const ip = request.ip || 'unknown';
>   const key = `rate-limit:${ip}`;
>
>   // 현재 요청 수 확인
>   const count = await redis.incr(key);
>
>   // 첫 요청이면 만료 시간 설정 (1분)
>   if (count === 1) {
>     await redis.expire(key, 60);
>   }
>
>   // 분당 100 요청 제한
>   if (count > 100) {
>     return new NextResponse('Too Many Requests', { status: 429 });
>   }
>
>   return NextResponse.next();
> }
> ```
>
> **6. 요청/응답 헤더 수정**
> ```tsx
> export function middleware(request: NextRequest) {
>   const response = NextResponse.next();
>
>   // 보안 헤더 추가
>   response.headers.set('X-Frame-Options', 'DENY');
>   response.headers.set('X-Content-Type-Options', 'nosniff');
>   response.headers.set('Referrer-Policy', 'origin-when-cross-origin');
>
>   // CORS 헤더 추가
>   response.headers.set('Access-Control-Allow-Origin', '*');
>
>   return response;
> }
> ```
>
> **7. URL 리라이트**
> ```tsx
> export function middleware(request: NextRequest) {
>   // /blog/123 → /posts/123으로 내부 리라이트
>   if (request.nextUrl.pathname.startsWith('/blog')) {
>     const newPath = request.nextUrl.pathname.replace('/blog', '/posts');
>     return NextResponse.rewrite(new URL(newPath, request.url));
>   }
>
>   return NextResponse.next();
> }
> ```
>
> **Matcher 패턴**
> ```tsx
> export const config = {
>   matcher: [
>     // 단일 경로
>     '/about',
>
>     // 와일드카드
>     '/dashboard/:path*',
>
>     // 여러 경로
>     '/api/:path*',
>     '/admin/:path*',
>
>     // 부정 패턴 (제외)
>     '/((?!api|_next/static|_next/image|favicon.ico).*)',
>   ],
> };
> ```
>
> **주의사항**
> - Edge Runtime에서 실행되므로 Node.js API 사용 불가
> - 빠른 응답을 위해 복잡한 로직은 피하기
> - 데이터베이스 쿼리는 최소화
> - 쿠키/헤더 조작은 신중하게

---

**Q14.** Next.js의 4가지 캐싱 메커니즘(Full Route Cache, Data Cache, Router Cache, Request Memoization)을 설명해주세요.

> Next.js는 성능 최적화를 위해 4가지 레벨의 캐싱을 제공합니다.
>
> **1. Request Memoization (요청 메모이제이션)**
> - **위치**: React 렌더링 중
> - **범위**: 단일 렌더 사이클
> - **대상**: 동일한 URL + 옵션을 가진 fetch 요청
>
> ```tsx
> // 같은 렌더링 사이클에서
> async function Component1() {
>   const data = await fetch('https://api.example.com/data'); // 실제 요청
>   return <div>{data}</div>;
> }
>
> async function Component2() {
>   const data = await fetch('https://api.example.com/data'); // 메모이제이션된 결과 재사용
>   return <div>{data}</div>;
> }
>
> // 컴포넌트 트리 어디서든 같은 fetch 호출 시 자동으로 중복 제거
> ```
>
> **특징**
> - 자동으로 동작, 설정 불필요
> - GET 요청에만 적용
> - 렌더링 완료 후 캐시 초기화
>
> ---
>
> **2. Data Cache (데이터 캐시)**
> - **위치**: 서버
> - **범위**: 영구적 (재배포/재시작 후에도 유지)
> - **대상**: fetch 요청 결과
>
> ```tsx
> // 기본: 무기한 캐시
> fetch('https://api.example.com/posts');
>
> // 캐시 안 함
> fetch('https://api.example.com/posts', { cache: 'no-store' });
>
> // 10초마다 재검증
> fetch('https://api.example.com/posts', { next: { revalidate: 10 } });
>
> // 태그 기반 재검증
> fetch('https://api.example.com/posts', { next: { tags: ['posts'] } });
> ```
>
> **재검증 방법**
> ```tsx
> // 1. 시간 기반 (Time-based Revalidation)
> fetch('https://api.example.com/posts', {
>   next: { revalidate: 60 } // 60초
> });
>
> // 2. 온디맨드 재검증 (On-demand Revalidation)
> import { revalidateTag, revalidatePath } from 'next/cache';
>
> // 특정 태그 재검증
> revalidateTag('posts');
>
> // 특정 경로 재검증
> revalidatePath('/blog');
> ```
>
> ---
>
> **3. Full Route Cache (전체 라우트 캐시)**
> - **위치**: 서버
> - **범위**: 빌드 시 또는 재검증 시
> - **대상**: 정적으로 렌더링된 라우트의 HTML + RSC Payload
>
> ```tsx
> // app/posts/page.tsx
>
> // 정적 렌더링 (캐시됨)
> export const dynamic = 'auto'; // 기본값
>
> export default async function PostsPage() {
>   const posts = await fetch('https://api.example.com/posts');
>   return <div>{/* ... */}</div>;
> }
>
> // 동적 렌더링 (캐시 안 됨)
> export const dynamic = 'force-dynamic';
>
> export default async function LivePage() {
>   const data = await fetch('https://api.example.com/live');
>   return <div>{/* ... */}</div>;
> }
> ```
>
> **동적 렌더링이 되는 경우 (캐시 안 됨)**
> - `cookies()`, `headers()` 사용
> - `searchParams` 사용
> - `dynamic = 'force-dynamic'` 설정
> - `cache: 'no-store'` 사용
>
> ---
>
> **4. Router Cache (라우터 캐시)**
> - **위치**: 클라이언트 (브라우저)
> - **범위**: 세션 동안 (브라우저 메모리)
> - **대상**: RSC Payload (서버 컴포넌트 데이터)
>
> ```tsx
> // 사용자가 페이지 간 이동 시
> <Link href="/about">About</Link> // 프리페치 + 캐시
>
> router.push('/dashboard'); // 이동 시 캐시 활용
> ```
>
> **캐시 유지 시간**
> - 정적 라우트: 5분
> - 동적 라우트: 30초
>
> **무효화 방법**
> ```tsx
> // 1. router.refresh() - 현재 라우트 재검증
> router.refresh();
>
> // 2. revalidatePath() - 서버에서 특정 경로 무효화
> revalidatePath('/dashboard');
>
> // 3. cookies.set(), cookies.delete() - 쿠키 변경 시 자동 무효화
> ```
>
> **프리페치 비활성화**
> ```tsx
> <Link href="/about" prefetch={false}>About</Link>
> ```
>
> ---
>
> **4가지 캐시의 상호작용**
>
> ```
> [요청 흐름]
>
> 1. 사용자가 페이지 방문
>    ↓
> 2. Router Cache 확인 (클라이언트)
>    - 캐시 히트 → 즉시 렌더링
>    - 캐시 미스 → 서버 요청
>    ↓
> 3. Full Route Cache 확인 (서버)
>    - 정적 라우트 캐시 히트 → HTML 반환
>    - 동적 라우트 → 렌더링 시작
>    ↓
> 4. 렌더링 중 fetch 호출
>    ↓
> 5. Request Memoization 확인
>    - 같은 요청 있음 → 메모이제이션 결과 재사용
>    - 없음 → Data Cache 확인
>    ↓
> 6. Data Cache 확인 (서버)
>    - 캐시 히트 → 캐시 데이터 반환
>    - 캐시 미스 → 실제 API 호출
> ```
>
> **캐싱 전략 선택 가이드**
>
> | 페이지 타입 | 권장 설정 | 캐시 레벨 |
> |------------|----------|----------|
> | 정적 콘텐츠 (블로그) | `revalidate: false` | Full Route + Data Cache |
> | 주기적 갱신 (뉴스) | `revalidate: 60` | Full Route + Data Cache (ISR) |
> | 실시간 데이터 (대시보드) | `dynamic: 'force-dynamic'` | Router Cache만 |
> | 사용자별 데이터 | `cache: 'no-store'` | 캐시 안 함 |
> | 하이브리드 | 일부 `no-store`, 일부 `revalidate` | 혼합 전략 |
>
> **캐시 디버깅**
> ```tsx
> // 캐시 상태 확인
> console.log('Cache:', {
>   requestMemo: '자동',
>   dataCache: process.env.NODE_ENV === 'production' ? '활성' : '비활성',
>   routeCache: '클라이언트에서 확인 불가',
>   routerCache: '브라우저 DevTools Network 탭',
> });
> ```

---

**Q15.** ISR(Incremental Static Regeneration)에서 revalidate 옵션은 어떻게 동작하나요?

> **ISR이란**
> - Static Site Generation(SSG) + 주기적 재생성
> - 빌드 시 정적 HTML 생성 + 런타임에 백그라운드 재생성
> - 정적 페이지의 속도 + 동적 데이터 최신성 확보
>
> **revalidate 동작 원리**
>
> ```
> [타임라인]
>
> T=0초: 빌드 시 정적 HTML 생성
> ├─ /posts/1 생성
> ├─ /posts/2 생성
> └─ /posts/3 생성
>
> T=10초: 사용자 A 요청 → 캐시된 HTML 반환 (빠름)
> T=20초: 사용자 B 요청 → 캐시된 HTML 반환
> T=65초: 사용자 C 요청 (revalidate=60 설정)
> ├─ 캐시된 HTML 즉시 반환 (사용자는 기다리지 않음)
> └─ 백그라운드에서 재생성 시작 (비동기)
>
> T=70초: 재생성 완료 → 새 HTML 캐시에 저장
> T=75초: 사용자 D 요청 → 새로 생성된 HTML 반환
> ```
>
> **App Router에서 사용**
>
> **1. 페이지 레벨 설정**
> ```tsx
> // app/posts/page.tsx
> export const revalidate = 60; // 60초
>
> async function PostsPage() {
>   const posts = await fetch('https://api.example.com/posts');
>   return <div>{/* ... */}</div>;
> }
> ```
>
> **2. fetch 레벨 설정**
> ```tsx
> async function PostsPage() {
>   // 이 fetch만 60초마다 재검증
>   const posts = await fetch('https://api.example.com/posts', {
>     next: { revalidate: 60 }
>   });
>
>   // 이 fetch는 캐시 안 함
>   const trending = await fetch('https://api.example.com/trending', {
>     cache: 'no-store'
>   });
>
>   return <div>{/* ... */}</div>;
> }
> ```
>
> **3. 동적 라우트 with ISR**
> ```tsx
> // app/posts/[id]/page.tsx
>
> // 빌드 시 생성할 경로 지정
> export async function generateStaticParams() {
>   const posts = await fetch('https://api.example.com/posts').then(r => r.json());
>
>   return posts.map(post => ({
>     id: post.id.toString(),
>   }));
> }
>
> export const revalidate = 60; // 60초마다 재생성
>
> async function PostPage({ params }) {
>   const post = await fetch(`https://api.example.com/posts/${params.id}`);
>   return <article>{/* ... */}</article>;
> }
> ```
>
> **Pages Router에서 사용**
> ```tsx
> // pages/posts/index.tsx
> export async function getStaticProps() {
>   const posts = await fetch('https://api.example.com/posts').then(r => r.json());
>
>   return {
>     props: { posts },
>     revalidate: 60, // 60초
>   };
> }
>
> function PostsPage({ posts }) {
>   return <div>{/* ... */}</div>;
> }
> ```
>
> **온디맨드 재검증**
> ```tsx
> // app/api/revalidate/route.ts
> import { revalidatePath, revalidateTag } from 'next/cache';
> import { NextRequest, NextResponse } from 'next/server';
>
> export async function POST(request: NextRequest) {
>   const secret = request.nextUrl.searchParams.get('secret');
>
>   // 보안 토큰 검증
>   if (secret !== process.env.REVALIDATE_SECRET) {
>     return NextResponse.json({ message: 'Invalid token' }, { status: 401 });
>   }
>
>   // 1. 경로 기반 재검증
>   revalidatePath('/posts');
>
>   // 2. 태그 기반 재검증
>   revalidateTag('posts');
>
>   return NextResponse.json({ revalidated: true, now: Date.now() });
> }
>
> // 사용: Webhook에서 호출
> // POST /api/revalidate?secret=MY_SECRET
> ```
>
> **태그 기반 재검증**
> ```tsx
> // 데이터 페칭 시 태그 지정
> async function PostsPage() {
>   const posts = await fetch('https://api.example.com/posts', {
>     next: { tags: ['posts'] }
>   });
>
>   return <div>{/* ... */}</div>;
> }
>
> // 특정 태그만 재검증
> import { revalidateTag } from 'next/cache';
>
> revalidateTag('posts'); // 'posts' 태그가 달린 모든 fetch 재검증
> ```
>
> **revalidate 값에 따른 동작**
> ```tsx
> // 1. false 또는 생략: 무기한 캐시 (순수 SSG)
> export const revalidate = false;
>
> // 2. 0: 매 요청마다 재생성 (SSR과 동일)
> export const revalidate = 0;
>
> // 3. 숫자: 해당 초마다 재검증 (ISR)
> export const revalidate = 60;
> ```
>
> **실전 예시: 전자상거래 제품 페이지**
> ```tsx
> // app/products/[id]/page.tsx
> export async function generateStaticParams() {
>   // 인기 상품 100개만 빌드 시 생성
>   const products = await db.product.findMany({
>     take: 100,
>     orderBy: { sales: 'desc' }
>   });
>
>   return products.map(p => ({ id: p.id.toString() }));
> }
>
> export const revalidate = 300; // 5분마다 재생성
>
> async function ProductPage({ params }) {
>   const product = await fetch(`/api/products/${params.id}`, {
>     next: {
>       revalidate: 300,
>       tags: [`product-${params.id}`]
>     }
>   });
>
>   return <div>{/* ... */}</div>;
> }
>
> // 관리자가 제품 수정 시 즉시 재검증
> // app/api/products/[id]/route.ts
> export async function PUT(request, { params }) {
>   await db.product.update({ /* ... */ });
>
>   // 해당 제품 페이지만 재검증
>   revalidateTag(`product-${params.id}`);
>
>   return Response.json({ success: true });
> }
> ```
>
> **장점**
> 1. 빌드 시간 단축 (모든 페이지 생성 불필요)
> 2. 빠른 응답 속도 (CDN 캐싱 가능)
> 3. 최신 데이터 반영 (백그라운드 재생성)
> 4. 서버 부하 감소 (캐시 활용)
>
> **주의사항**
> - 재생성 실패 시 기존 캐시 계속 제공
> - revalidate 시간은 최소값 (동시 요청 시 한 번만 재생성)
> - 너무 짧은 revalidate는 서버 부하 증가
