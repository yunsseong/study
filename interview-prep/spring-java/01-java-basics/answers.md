# Java 기초 & JVM 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** JVM이란 무엇이며, Java가 "Write Once, Run Anywhere"를 실현할 수 있는 이유를 설명해주세요.

> JVM(Java Virtual Machine)은 Java 바이트코드를 실행하는 가상 머신입니다. Java 소스코드를 javac로 컴파일하면 .class 파일(바이트코드)이 생성되고, 이 바이트코드를 JVM이 해석하여 실행합니다. JVM은 각 OS별로 구현되어 있으므로, 동일한 바이트코드를 Windows, Linux, Mac 어디서든 실행할 수 있습니다. 즉, 개발자는 OS에 관계없이 하나의 코드를 작성하면 되고, OS별 차이는 JVM이 처리해줍니다. 예를 들어 Spring Boot 앱을 `java -jar app.jar`로 실행하면 JVM이 바이트코드를 해석하여 해당 OS에서 동작시킵니다.

**Q2.** JVM의 메모리 구조를 설명해주세요. 각 영역이 어떤 데이터를 저장하는지, 스레드 간 공유 여부를 포함하여 답변해주세요.

> JVM 메모리는 크게 5가지 영역으로 나뉩니다. 모든 스레드가 공유하는 영역으로는 Method Area와 Heap이 있습니다. Method Area는 클래스 정보, static 변수, 상수 풀을 저장하고, Heap은 new로 생성된 객체와 배열을 저장하며 GC의 대상이 됩니다. 스레드마다 독립적인 영역으로는 Stack, PC Register, Native Method Stack이 있습니다. Stack에는 지역 변수와 메서드 호출 정보(프레임)가 저장되고, PC Register는 현재 실행 중인 명령어 주소를, Native Method Stack은 C/C++ 네이티브 메서드 호출 정보를 저장합니다.

**Q3.** Garbage Collection(GC)이란 무엇이며, 기본적인 동작 원리(Mark & Sweep)를 설명해주세요.

> GC는 Heap 메모리에서 더 이상 사용하지 않는 객체를 자동으로 제거하는 메커니즘입니다. C/C++과 달리 Java는 개발자가 직접 메모리를 해제할 필요가 없습니다. GC의 기본 동작은 Mark & Sweep 알고리즘입니다. 먼저 Mark 단계에서 GC Root(Stack의 지역 변수, Static 변수, JNI 참조)에서 시작하여 참조를 따라가며 도달 가능한 객체를 "살아있음"으로 표시합니다. 그 다음 Sweep 단계에서 Mark되지 않은 객체를 메모리에서 제거합니다. 선택적으로 Compact 단계에서 살아남은 객체를 한쪽으로 모아 메모리 단편화를 해결합니다.

**Q4.** Java 컬렉션 프레임워크에서 List, Set, Map의 차이점을 설명해주세요.

> List는 순서가 있고 중복을 허용하는 컬렉션으로, 인덱스로 요소에 접근할 수 있습니다. 대표 구현체는 ArrayList입니다. Set은 순서가 없고 중복을 허용하지 않는 컬렉션으로, 대표 구현체는 HashSet입니다. Map은 Key-Value 쌍으로 저장하며, Key는 중복 불가하고 Value는 중복을 허용합니다. 대표 구현체는 HashMap입니다. List는 순서가 중요한 데이터에, Set은 중복 제거가 필요한 경우에, Map은 Key-Value 매핑이 필요한 경우에 사용합니다.

**Q5.** HashMap의 내부 동작 원리를 설명해주세요. key를 통해 value를 조회하는 과정을 포함해주세요.

> HashMap은 내부적으로 배열과 연결 리스트(또는 트리) 구조를 사용합니다. put() 시에는 key의 hashCode()를 호출하여 해시값을 계산하고, 이를 배열 크기로 나눈 나머지로 버킷 인덱스를 결정하여 해당 버킷에 Entry(key, value, hash, next)를 저장합니다. get() 시에는 동일하게 key의 hashCode()로 버킷 인덱스를 찾고, 해당 버킷 내의 Entry들을 equals()로 비교하여 일치하는 key의 value를 반환합니다. 평균 시간 복잡도는 O(1)입니다.

## 비교/구분 (6~9)

**Q6.** Stack 영역과 Heap 영역의 차이를 설명하고, 참조 타입 변수가 어떻게 저장되는지 설명해주세요.

> Stack은 스레드마다 독립적이며, 지역 변수와 메서드 호출 정보(프레임)를 저장합니다. 메서드 호출 시 프레임이 push되고 종료 시 pop됩니다(LIFO). Heap은 모든 스레드가 공유하며, new로 생성된 객체가 저장되고 GC의 대상입니다. 참조 타입 변수의 경우, 예를 들어 `User user = new User("홍길동")`을 실행하면 Stack에는 참조(주소값)만 저장되고, 실제 User 객체는 Heap에 저장됩니다. Stack의 user 변수가 Heap의 User 객체 주소를 가리키는 구조입니다.

**Q7.** ArrayList와 LinkedList의 차이점을 설명해주세요. 실무에서는 어떤 것을 주로 사용하나요?

> ArrayList는 내부적으로 배열을 사용하여 인덱스를 통한 조회가 O(1)로 빠르지만, 중간 삽입/삭제 시 뒤의 요소를 이동해야 하므로 O(n)입니다. LinkedList는 노드 연결 구조로 삽입/삭제 자체는 O(1)이지만, 탐색이 O(n)으로 느립니다. 실무에서는 99% ArrayList를 사용합니다. LinkedList가 이론적으로 삽입/삭제에 유리하지만, CPU 캐시 효율(데이터 지역성)과 실제 벤치마크에서 ArrayList가 거의 항상 더 빠르기 때문입니다.

**Q8.** Minor GC와 Major GC(Full GC)의 차이를 설명해주세요.

> Heap은 Young Generation과 Old Generation으로 나뉩니다. Minor GC는 Young Generation(Eden, Survivor 영역)을 대상으로 하며, 빠르게(밀리초 단위) 실행되고 Stop-the-World 시간이 짧습니다. 새로운 객체는 Eden에 생성되고, Eden이 가득 차면 Minor GC가 발생하여 살아남은 객체를 Survivor 영역으로 이동시킵니다. Major GC(Full GC)는 Old Generation 전체를 대상으로 하며, 실행 시간이 길고(밀리초~초) Stop-the-World 시간도 길어 성능에 큰 영향을 줍니다. Survivor에서 age가 임계값을 넘은 객체가 Old Generation으로 이동(Promotion)되고, Old가 가득 차면 Full GC가 발생합니다.

**Q9.** HashSet, LinkedHashSet, TreeSet의 차이를 설명해주세요.

> 세 가지 모두 Set 인터페이스의 구현체로 중복을 허용하지 않습니다. HashSet은 HashMap을 기반으로 구현되어 순서가 없으며, O(1)의 시간 복잡도를 가집니다. LinkedHashSet은 LinkedHashMap을 기반으로 삽입 순서를 유지하면서 O(1)의 시간 복잡도를 가집니다. TreeSet은 Red-Black Tree를 기반으로 요소가 정렬된 상태를 유지하며, O(log n)의 시간 복잡도를 가집니다. 순서가 필요 없으면 HashSet, 삽입 순서가 필요하면 LinkedHashSet, 정렬이 필요하면 TreeSet을 사용합니다.

## 심화/실무 (10~12)

**Q10.** equals()와 hashCode()를 왜 함께 재정의해야 하나요? 둘 중 하나만 재정의하면 어떤 문제가 발생하는지 구체적으로 설명해주세요.

> HashMap, HashSet 등 Hash 기반 컬렉션은 먼저 hashCode()로 버킷을 찾고, 같은 버킷 내에서 equals()로 동일 객체를 판별합니다. equals()만 재정의하고 hashCode()를 재정의하지 않으면, 논리적으로 같은 두 객체가 서로 다른 hashCode()를 반환하여 다른 버킷에 저장됩니다. 그러면 HashSet.contains()가 올바르게 동작하지 않습니다. 예를 들어, 같은 id를 가진 두 User 객체를 만들어 하나를 HashSet에 넣고 다른 하나로 contains()를 호출하면, hashCode()가 달라 다른 버킷을 탐색하므로 equals()까지 가보지도 못하고 false를 반환합니다. 따라서 equals()에서 사용한 필드를 기반으로 hashCode()도 반드시 재정의해야 합니다.

**Q11.** G1 GC의 특징과 기존 GC와의 차이점을 설명해주세요.

> G1 GC는 Java 9부터 기본 GC로, 대용량 힙에 적합합니다. 기존 GC는 Heap을 연속된 Young/Old 영역으로 나누지만, G1 GC는 Heap을 동일한 크기의 Region(1~32MB)으로 분할합니다. 각 Region은 Eden, Survivor, Old, Humongous(큰 객체) 등의 역할을 독립적으로 변경할 수 있습니다. "Garbage First"라는 이름처럼 가비지가 가장 많은 Region부터 수거하여 효율적입니다. STW 시간 목표를 설정할 수 있고(`-XX:MaxGCPauseMillis`), Mixed GC로 Young과 일부 Old Region을 동시에 수거할 수 있어 Full GC 빈도를 줄일 수 있습니다.

**Q12.** HashMap에서 해시 충돌이 발생하면 어떻게 처리하나요? Java 8에서 개선된 점은 무엇인가요?

> 서로 다른 key가 같은 버킷 인덱스에 매핑되면 해시 충돌이 발생합니다. 기본적으로 Separate Chaining 방식으로, 같은 버킷에 연결 리스트로 Entry들을 연결합니다. 조회 시에는 해당 버킷의 연결 리스트를 순회하면서 key.equals()로 비교합니다. Java 8에서는 하나의 버킷에 8개 이상의 Entry가 쌓이면 연결 리스트를 Red-Black Tree로 변환합니다. 이로써 최악의 경우 시간 복잡도가 O(n)에서 O(log n)으로 개선됩니다. 반대로 Entry가 6개 이하로 줄어들면 다시 연결 리스트로 돌아갑니다.

## 꼬리질문 대비 (13~15)

**Q13.** Spring Boot 애플리케이션에서 OutOfMemoryError가 발생했을 때, 어떤 원인을 의심하고 어떻게 대응하시겠습니까?

> 먼저 에러 종류에 따라 원인을 파악합니다. "Java heap space"라면 Heap 메모리 부족으로, 대량 데이터를 한 번에 로딩하거나(예: findAll()로 100만 건 조회) 메모리 누수가 원인일 수 있습니다. 대응으로는 `-Xmx`를 늘리거나, 페이징 처리로 데이터 조회량을 제한하고, 힙 덤프를 분석하여 메모리 누수를 확인합니다. "StackOverflowError"라면 무한 재귀가 원인일 수 있어 코드를 수정해야 합니다. GC 로그(`-Xlog:gc*`)를 활성화하여 Full GC가 자주 발생하는지 확인하고, Full GC가 빈번하면 메모리 누수를 의심합니다.

**Q14.** JIT Compiler란 무엇이며, Interpreter와 어떻게 협력하여 동작하나요?

> JVM의 Execution Engine에는 Interpreter와 JIT Compiler가 있습니다. 처음에는 Interpreter가 바이트코드를 한 줄씩 해석하여 실행합니다. 이 과정에서 자주 호출되는 메서드(Hot Spot)가 발견되면, JIT(Just-In-Time) Compiler가 해당 코드를 기계어로 컴파일하여 캐시에 저장합니다. 이후부터는 컴파일된 기계어를 바로 실행하므로 성능이 크게 향상됩니다. HotSpot JVM이라는 이름도 여기서 유래한 것으로, "뜨거운(자주 실행되는) 지점"을 찾아 최적화하는 방식입니다. 이를 통해 인터프리터의 빠른 시작과 컴파일러의 빠른 실행 속도를 모두 활용합니다.

**Q15.** HashMap의 초기 용량(capacity)과 Load Factor는 무엇이며, 왜 적절하게 설정해야 하나요?

> HashMap의 기본 배열 크기(capacity)는 16이고, Load Factor의 기본값은 0.75입니다. 저장된 Entry 수가 capacity * Load Factor를 초과하면(16 * 0.75 = 12개 초과) 배열 크기를 2배로 늘리고 모든 Entry의 해시값을 다시 계산하여 재배치(rehashing)합니다. 이 리사이징 과정은 비용이 크기 때문에, 예상 데이터 수를 알고 있다면 초기 용량을 적절히 설정하는 것이 중요합니다. 예를 들어 100개의 데이터를 저장할 예정이라면, 100 / 0.75 = 134이므로 다음 2의 거듭제곱인 256을 초기 용량으로 설정하면 리사이징을 방지할 수 있습니다.
