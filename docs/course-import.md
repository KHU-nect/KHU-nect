# Course Import

## 현재 구현 범위
- CSV import는 실제 동작 경로를 구현했다.
- `POST /api/admin/courses/import/csv`로 multipart CSV 업로드가 가능하다.
- 중복 `courseCode`는 새로 만들지 않고 기존 강의를 update한다.

## JSON 기반 초기 시드 (CourseDataLoader)
- `khunect-backend/src/main/resources/data/수강데이터_structured.json`에 있는 2300여 건의 강의 데이터를 앱 시작 시 자동으로 PostgreSQL에 적재한다.
- `CourseDataLoader`가 `ApplicationRunner`로 동작하며, **SEEDED** 타입의 강의가 하나라도 존재하면 재적재를 건너뜁니다 (멱등 보장).
- 데이터 출처: `data/수강데이터_structured.json` (2026학년도 1학기 강의 목록).

## SQL 기반 수동 시드 (db/init/02-seed-courses.sql)
- `khunect-backend/db/init/02-seed-courses.sql`은 동일한 데이터를 SQL INSERT로 제공한다.
- PostgreSQL에 직접 실행하거나 `\i` 명령으로 import할 수 있다.
- **멱등**: `course` 테이블 삽입은 `ON CONFLICT (course_code) DO NOTHING`으로 처리되고, `course_schedules`는 SEEDED 스케줄이 없을 때만 삽입된다.
- ⚠️ 이 SQL은 JPA가 테이블을 생성한 **이후**에 실행해야 한다 (테이블이 없으면 실패). 앱을 한 번 기동한 후 실행하거나, CourseDataLoader를 이용하는 것이 더 간단하다.

## KHU Crawler 구조
- `KhuCourseCrawlerService`는 fetch 와 import orchestration 역할을 가진다.
- `KhuCourseParser`는 HTML -> `CourseImportRow` 파싱만 담당한다.
- URL, 파라미터 이름, selector는 `application.yml`의 `app.course-import.khu.*`로 분리했다.
- parser는 fixture 기반 테스트로 검증한다.

## 라이브 사이트 반영 상태
- 이번 작업에서는 경희대 실제 강의 검색 엔드포인트를 확정적으로 검증하지 못했다.
- 그래서 crawler는 config placeholder + fixture parser 중심으로 구현했다.
- live 적용 시 해야 할 일:
  - 실제 검색 URL과 요청 파라미터 이름 확인
  - 응답 HTML 구조에 맞게 selector 조정
  - 필요 시 GET/POST 방식과 인증/세션 요구사항 반영
  - crawler 결과 샘플을 fixture로 추가해 회귀 테스트 강화

## 운영 안정성
- crawler가 비활성화되거나 네트워크 요청에 실패해도 앱 전체는 계속 동작한다.
- admin import API는 local/dev 환경에서만 허용한다.
