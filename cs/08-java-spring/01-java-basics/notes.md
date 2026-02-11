# Java 기초 문법

## 변수와 타입

### 기본형 vs 참조형

```java
// 기본형 (Primitive) - 값 자체를 저장, 스택에 저장
int age = 25;          // 4byte 정수
long id = 123456789L;  // 8byte 정수
double price = 99.9;   // 8byte 실수
boolean isActive = true;
char grade = 'A';

// 참조형 (Reference) - 주소(참조)를 저장, 힙에 저장
String name = "John";         // 문자열 (불변 객체)
int[] numbers = {1, 2, 3};    // 배열
List<String> list = new ArrayList<>();  // 컬렉션
```

### Wrapper 클래스

```java
// 기본형 → 참조형 (Auto Boxing)
Integer num = 25;       // int → Integer
Long id = 100L;         // long → Long

// 참조형 → 기본형 (Auto Unboxing)
int value = num;        // Integer → int

// 왜 필요한가?
// 컬렉션은 기본형을 담을 수 없음
List<int> list;      // ❌ 컴파일 에러
List<Integer> list;  // ✅
```

### String

```java
// String은 불변(Immutable) 객체
String a = "hello";
String b = a + " world";  // 새 객체 생성 (a는 그대로)

// == vs equals()
String s1 = "hello";
String s2 = "hello";
String s3 = new String("hello");

s1 == s2;       // true  (String Pool에서 같은 참조)
s1 == s3;       // false (다른 객체)
s1.equals(s3);  // true  (값 비교) ← 항상 equals() 사용!

// StringBuilder (가변, 문자열 연결 시 성능)
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);  // 같은 객체에 추가 → 빠름
}
String result = sb.toString();
```

---

## 컬렉션 (Collection)

### List

```java
// ArrayList: 인덱스 접근 O(1), 삽입/삭제 O(n)
List<String> list = new ArrayList<>();
list.add("a");
list.add("b");
list.get(0);     // "a"
list.size();     // 2
list.remove(0);  // "a" 삭제

// LinkedList: 삽입/삭제 O(1), 인덱스 접근 O(n)
List<String> linked = new LinkedList<>();
```

### Map

```java
// HashMap: 순서 없음, null 허용, O(1) 조회
Map<String, Integer> map = new HashMap<>();
map.put("age", 25);
map.get("age");           // 25
map.getOrDefault("x", 0); // 키 없으면 기본값
map.containsKey("age");   // true

// 순회
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}

// LinkedHashMap: 삽입 순서 유지
// TreeMap: 키 정렬
```

### Set

```java
// HashSet: 중복 불허, 순서 없음
Set<String> set = new HashSet<>();
set.add("a");
set.add("a");  // 무시됨
set.size();    // 1

// TreeSet: 정렬된 Set
// LinkedHashSet: 삽입 순서 유지
```

---

## Stream API

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// filter: 조건 필터링
List<Integer> evens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());  // [2, 4, 6, 8, 10]

// map: 변환
List<String> strings = numbers.stream()
    .map(n -> "숫자:" + n)
    .collect(Collectors.toList());

// reduce: 집계
int sum = numbers.stream()
    .reduce(0, Integer::sum);  // 55

// 체이닝
double avg = numbers.stream()
    .filter(n -> n > 3)
    .mapToInt(Integer::intValue)
    .average()
    .orElse(0.0);

// 실전: 유저 목록에서 활성 유저의 이메일 목록
List<String> emails = users.stream()
    .filter(User::isActive)
    .map(User::getEmail)
    .sorted()
    .collect(Collectors.toList());
```

---

## 예외 처리

```java
// Checked Exception: 컴파일 시점에 처리 강제 (IOException, SQLException)
// Unchecked Exception: 런타임 예외 (NullPointerException, IllegalArgumentException)

try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("0으로 나눌 수 없음: " + e.getMessage());
} finally {
    System.out.println("항상 실행");
}

// 커스텀 예외 (Spring에서 자주 사용)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("유저를 찾을 수 없습니다. ID: " + id);
    }
}
```

---

## 제네릭 (Generics)

```java
// 타입 안전성 + 재사용성
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }
}

// 사용
ApiResponse<User> response = ApiResponse.ok(user);
ApiResponse<List<Post>> response = ApiResponse.ok(posts);
```

---

## 람다와 함수형 인터페이스

```java
// 함수형 인터페이스: 추상 메서드가 1개인 인터페이스
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}

// 람다 표현식
Predicate<Integer> isEven = n -> n % 2 == 0;
isEven.test(4);  // true

// 메서드 참조
Function<String, Integer> parser = Integer::parseInt;
parser.apply("123");  // 123

// 주요 함수형 인터페이스
// Predicate<T>:  T → boolean  (조건 검사)
// Function<T,R>: T → R        (변환)
// Consumer<T>:   T → void     (소비)
// Supplier<T>:   () → T       (생산)
```

---

## Optional

```java
// NullPointerException 방지

// ❌ 기존 방식
User user = userRepository.findById(1L);
if (user != null) {
    System.out.println(user.getName());
}

// ✅ Optional 방식
Optional<User> optUser = userRepository.findById(1L);

// 값이 있으면 처리
optUser.ifPresent(u -> System.out.println(u.getName()));

// 없으면 기본값
User user = optUser.orElse(new User("Unknown"));

// 없으면 예외
User user = optUser.orElseThrow(
    () -> new UserNotFoundException(1L)
);

// 체이닝
String name = optUser
    .map(User::getName)
    .orElse("Unknown");
```

---

## 면접 예상 질문

1. **기본형과 참조형의 차이는?**
   - 기본형: 값 자체 저장(스택) / 참조형: 주소 저장(힙), null 가능

2. **== 와 equals()의 차이는?**
   - ==: 참조(주소) 비교 / equals(): 값 비교

3. **String이 불변인 이유는?**
   - 보안(해시 키), String Pool 재사용, 스레드 안전

4. **Checked vs Unchecked Exception 차이는?**
   - Checked: 컴파일 강제 / Unchecked: 런타임, 개발자 실수

5. **Stream API의 장점은?**
   - 선언적 코드, 병렬 처리 용이, 가독성 향상
