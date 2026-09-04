# LMHYMV.android

무비 추천 안드로이드 앱. 겉 폴더명은 `LMHYU_Front` 이지만 웹 프론트가 아니라
**안드로이드 네이티브 앱**이다.

## 스택

- **언어**: 앱 코드 전량 **Java**. Kotlin/Jetpack Compose 는 `ui/theme/*` 스캐폴딩만 존재하며 실화면 미사용.
- **UI**: 전통적 Android View 시스템 — Activity/Fragment + XML 레이아웃 + `findViewById` + RecyclerView Adapter.
  (MVVM/ViewModel/ViewBinding/Compose 없음)
- **네트워킹**: Retrofit2 + Gson, `RetrofitClient` 싱글턴, `Call<T>.enqueue(Callback)` 패턴.
- **인증**: `AuthInterceptor`(토큰 부착) + `TokenAuthenticator`(401 시 refresh) + `TokenManager`(EncryptedSharedPreferences).
- **이미지**: Glide (TMDB 상대경로 접두사 `https://image.tmdb.org/t/p/w500`).
- **빌드**: Gradle KTS + 버전 카탈로그(`gradle/libs.versions.toml`), `minSdk 33 / target·compile 36`, JDK 11.
- 로그인: Google / Naver SDK. 서버 BASE_URL 은 `RetrofitClient.BASE_URL` 한 곳.

## 패키지 구조 (타입별 배치)

```
app/src/main/java/com/example/lmhymvandroid
├── Activity/   화면 Activity + 탭 Fragment (Login, Main, MovieDetail, Recommend/Search/Explore/MyPage ...)
├── Adapter/    RecyclerView 어댑터
├── DTO/        서버 요청/응답 모델 (@SerializedName + Serializable)
├── Service/    Retrofit 인터페이스(AuthService/MovieService) + 인증(AuthInterceptor/TokenAuthenticator)
├── View/       커스텀 뷰 (BiorhythmGraphView)
└── (루트)      RetrofitClient · TokenManager · ToastUtil
app/src/main/res/layout   activity_* / fragment_* / item_* / dialog_*
```

> 주의: 루트 `AuthInterceptor.java` 와 `Service/AuthInterceptor.java` 가 중복 존재하며,
> `RetrofitClient` 가 실제로 쓰는 것은 **`Service/` 버전**이다.

## 빌드 / 실행 (Windows)

```bat
gradlew.bat assembleDebug        :: 디버그 APK 빌드(컴파일 검증)
gradlew.bat testDebugUnitTest    :: 유닛 테스트
gradlew.bat installDebug         :: 연결된 기기/에뮬레이터에 설치
```

`local.properties` 에 `GOOGLE_SERVER_CLIENT_ID` 등 키가 필요하다(VCS 미포함).

## 컨벤션 (요약 — 상세는 아래 스킬)

- 화면 추가: DTO → Service 엔드포인트 → XML 레이아웃 → (목록이면) Adapter → Activity/Fragment 순.
- API 는 `RetrofitClient.getClient(context).create(XxxService.class)` 로만 얻고 `enqueue(Callback)` 로 호출,
  `onResponse` 에서 `isSuccessful() && body()!=null` 방어 필수.
- 뷰는 `findViewById`, id↔필드 접두사 규칙(`tv_/rv_/btn_/iv_/pb_` ↔ camelCase).
- DTO 는 `@SerializedName` + (전달 모델이면) `Serializable`.
- 주석·로그·사용자 문자열은 **한국어**. 로그 태그 `API_ERROR`/`API_FAIL`. 커밋 메시지 한국어, 브랜치 `Feature/*`·`Fix/*`.
- 새 기능을 Compose/ViewBinding 으로 만들지 않는다(명시 요청 없는 한 Java+XML).

## Claude 세팅

- 컨벤션 스킬: `.claude/skills/architecture-conventions`(구조·계층·네트워킹·인증),
  `.claude/skills/global-conventions`(명명·바인딩·DTO·로깅·언어·이미지).
- 에이전트: `.claude/agents/android-developer`(구현 write-owner), `.claude/agents/code-reviewer`(read-only 리뷰).
- 커맨드: `/codereview` — 변경 파일을 프로젝트 규칙으로 리뷰.
