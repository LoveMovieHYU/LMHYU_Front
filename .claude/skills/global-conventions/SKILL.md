---
name: global-conventions
description: LMHYMV.android 의 전역 코딩 규약을 정의한다. 클래스·파일 명명 접미사, DTO(@SerializedName + Serializable) 규칙, findViewById 뷰 바인딩과 id↔필드 명명, 로깅 태그와 ToastUtil, 서술 언어(한국어), Glide 이미지 로딩(TMDB URL 접두사) 등을 다룬다. 어떤 파일을 만들거나 수정하든 함께 지킨다. 구조·계층·네트워킹 배치는 architecture-conventions 를 참고한다.
---

# global-conventions

LMHYMV.android 전역에서 파일 종류와 무관하게 지키는 규약이다. architecture-conventions 가
"어디에 무엇을 두는가"라면, 이 문서는 "그 안을 어떻게 쓰는가"다.

---

## 명명 규칙

- 클래스는 역할 접미사를 붙인다: `*Activity`, `*Fragment`, `*Adapter`, `*Service`(Retrofit 인터페이스),
  커스텀 뷰 `*View`. 인증 인프라는 관례명(`AuthInterceptor`, `TokenAuthenticator`, `TokenManager`, `RetrofitClient`).
- DTO 명명:
  - 응답: `*Response` 또는 `*ResponseDTO` (예: `HomeResponse`, `MovieDetailResponse`, `UserResponseDTO`).
  - 요청: `*Request` (예: `LikeRequest`, `NicknameRequest`, `GoogleLoginRequest`).
  - 서버 모델을 그대로 담는 공용 아이템은 명사형(`MovieItem`, `ErrorResponse`).
  - 하나로 통일돼 있지 않다(Response vs ResponseDTO 혼재). **새 파일은 같은 API 그룹의 기존 이웃 이름을 따른다.**
- 레이아웃 파일: `activity_*`(Activity), `fragment_*`(Fragment), `item_*`(리스트 아이템), `dialog_*`(다이얼로그).
- 리소스 id 는 `snake_case`, 뷰 타입 접두사를 붙인다: `tv_`(TextView), `rv_`(RecyclerView),
  `btn_`(버튼/클릭 ImageView), `iv_`(ImageView), `pb_`(ProgressBar), `layout_`(컨테이너).
- 자바 필드는 대응하는 `camelCase` + 같은 접두사: id `tv_physical_index` ↔ 필드 `tvPhysical`,
  `rv_movie_list` ↔ `rvMovieList`, `btn_menu` ↔ `btnMenu`, `pb_movie_loading` ↔ `pbMovieLoading`.

## 뷰 바인딩

- 뷰 참조는 **`findViewById`** 로만 얻는다. ViewBinding/DataBinding/Compose 를 새로 도입하지 않는다.
- Activity/Fragment 는 `initViews(...)` 한 곳에서 모든 뷰를 바인딩하고 리스너를 연결한다.
- 클릭 리스너는 람다로: `btnMenu.setOnClickListener(v -> ...)`.
- 가시성 전환/애니메이션은 `View.VISIBLE/GONE` + `view.animate()...` 패턴을 쓴다(기존 토글 패널들과 동일하게).

## DTO 규칙

- 필드마다 서버 키를 `@SerializedName("serverKey")` 로 명시한다(서버 키와 자바 필드명이 달라도 됨.
  예: 서버 `posterPath` → 필드 `posterUrl`).
- 화면 간 전달(Intent extra)에 쓰는 모델은 `implements Serializable`. 중첩 모델도 `Serializable`.
- 접근은 getter 로 노출한다(필드는 `private`). 필요한 getter 만 두는 스타일을 유지한다.
- 파싱/네트워크 라이브러리는 Gson. 새 컨버터를 도입하지 않는다.

## 로깅 · 사용자 알림

- 디버그/에러 로그는 `android.util.Log` 사용. API 에러 태그는 기존 관례를 따른다:
  응답 실패 `Log.e("API_ERROR", ...)`, 네트워크 실패(`onFailure`) `Log.e("API_FAIL", t.getMessage())`.
- 사용자에게 짧게 알릴 때는 프로젝트의 `ToastUtil` 을 쓴다(직접 `Toast.makeText` 남발 금지).
- 화면 내 상태 안내는 상태 TextView 텍스트 갱신으로 표현(예: `tvStatusMsg.setText("...")`).

## 이미지 로딩

- 이미지는 **Glide** 로 로드한다: `Glide.with(context).load(url).into(imageView)`.
- 포스터 등 TMDB 경로는 절대 URL 이 아니면 접두사를 붙인다:
  `if (url != null && !url.startsWith("http")) url = "https://image.tmdb.org/t/p/w500" + url;`
  (기존 어댑터들과 동일 규칙). 새 목록/상세에서도 같은 방식으로 처리한다.

## 서술 언어

- 주석, 로그 메시지, 사용자 노출 문자열(상태 메시지/토스트)은 **한국어**로 작성한다(기존 코드와 동일).
- 커밋 메시지도 한국어. 브랜치는 `Feature/*`, `Fix/*` 를 쓰고 PR 로 머지한다.

## 하지 말 것

- 뷰 접근에 ViewBinding/DataBinding/Compose 를 새로 끌어들이기(이 앱 표준은 `findViewById`).
- DTO 에 `@SerializedName` 없이 필드 추가(서버 키 매핑이 깨질 수 있음).
- Intent 로 넘길 모델에서 `Serializable` 누락.
- API 로그 태그를 매번 새로 짓기(`API_ERROR`/`API_FAIL` 관례 유지).
- 이미지 URL 접두사 처리 없이 상대 경로를 Glide 에 그대로 넘기기.
- 영어로 사용자 노출 문자열 작성.
