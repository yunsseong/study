package com.example.todo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [설계 포인트] JPA Auditing 활성화
 *
 * @EnableJpaAuditing을 Application 클래스에 직접 붙이는 경우가 많은데,
 * 실무에서는 별도 Config 클래스로 분리한다.
 *
 * 이유: @WebMvcTest 등 슬라이스 테스트 시 Application 클래스가 로드되면
 * JPA 관련 Bean이 없어서 테스트가 깨진다.
 * Config로 분리하면 테스트에서 이 설정을 제외할 수 있다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
