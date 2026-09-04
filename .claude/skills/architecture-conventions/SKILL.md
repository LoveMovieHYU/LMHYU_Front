---
name: architecture-conventions
description: LMHYMV.android(무비 추천 안드로이드 앱)의 구조·계층·네트워킹 규약을 정의한다. 패키지 배치(Activity/Adapter/DTO/Service/View), Activity·Fragment·Adapter의 역할과 의존 방향, Retrofit 서비스·Call/Callback·토큰 인증(AuthInterceptor/TokenAuthenticator/TokenManager) 흐름을 다룬다. 새 화면·API·어댑터를 추가하거나 네트워킹 코드를 만질 때 사용한다. 명명·findViewById 바인딩·로깅·DTO·이미지 로딩 등 전역 규약은 global-conventions 를 함께 참고한다.
---

# architecture-conventions

LMHYMV.android 의 코드 구조와 네트워킹 계층 규약이다. 새로 추가하는 화면·API·어댑터는
반드시 아래에서 관찰된 배치와 흐름을 따른다. 새 패턴을 임의로 도입하지 않는다.

**스택**: Java(앱 코드 전량) + 전통적 Android View 시스템(Activity/Fragment + XML + findViewById).
Kotlin/Jetpack Compose 는 `ui/theme/*` 스캐폴딩만 존재하며 **실제 화면에 쓰지 않는다**.
네트워킹은 Retrofit2 + Gson, 이미지 로딩은 Glide.

---

## 패키지 구조 (타입별 배치)

패키지는 도메인이 아니라 **타입/역할**로 나눈다. `com.example.lmhymvandroid` 아래:

```
com.example.lmhymvandroid
├── Activity/     화면 단위 Activity 와 탭 Fragment
│                 (LoginActivity, MainActivity, MovieDetailActivity,
│                  RecommendFragment, SearchFragment, ExploreFragment, MyPageFragment ...)
├── Adapter/      RecyclerView 어댑터 (HorizontalMovieAdapter, MovieClickAdapter,
│                 SearchMovieGridAdapter, FavoriteMovieAdapter)
├── DTO/          서버 요청/응답 모델 (*Request, *Response, *ResponseDTO, MovieItem ...)
├── Service/      Retrofit 서비스 인터페이스와 인증 컴포넌트
│                 (AuthService, MovieService, AuthInterceptor, TokenAuthenticator)
├── View/         커스텀 뷰 (BiorhythmGraphView)
└── (루트)        앱 전역 유틸/인프라
                  RetrofitClient, TokenManager, ToastUtil, AuthInterceptor
```

- 새 화면 → `Activity/`, 새 목록 어댑터 → `Adapter/`, 새 서버 모델 → `DTO/`,
  새 API 그룹 → `Service/` 인터페이스, 커스텀 그리기 뷰 → `View/`.
- 앱 전역에서 공유되는 싱글턴/유틸(네트워크 클라이언트, 토큰 저장소, 토스트)은 **루트 패키지**에 둔다.
- 리소스: 레이아웃 `res/layout/`(`activity_*`, `fragment_*`, `item_*`, `dialog_*`),
  드로어블 `res/drawable/`. 새 화면 추가 시 대응 XML 레이아웃을 함께 만든다.

> 주의: 루트 `AuthInterceptor.java` 와 `Service/AuthInterceptor.java` 가 중복 존재한다.
> `RetrofitClient` 가 실제로 쓰는 것은 **`Service/AuthInterceptor`** 다. 인증 관련 수정은
> `Service/` 쪽을 기준으로 하고, 새 코드에서 루트 버전을 import 하지 않는다.

## 화면 계층: Activity / Fragment

- 독립 화면(로그인, 상세, 프로필 편집, 즐겨찾기 등)은 **Activity**.
- 하단 탭(추천/탐색/검색/마이페이지)은 **Fragment** 이며 `MainActivity` 가 호스팅한다.
- Fragment 표준 수명주기 패턴을 지킨다:
  - `onCreateView` 에서 레이아웃만 inflate.
  - `onViewCreated` 에서 `initViews(view)` → `initRetrofit()` 순으로 초기화.
  - 화면 복귀 시 데이터 갱신이 필요하면 `onResume` 에서 재조회한다
    (예: `RecommendFragment.onResume() → refreshData()`).
- 뷰 참조는 `initViews(view)`(Fragment) / `initViews()`(Activity)에 모아 `findViewById` 로 바인딩한다.
  ViewBinding/DataBinding/Compose 는 사용하지 않는다.
- Fragment → 호스트 Activity 통신은 `getActivity() instanceof MainActivity` 캐스팅으로 호출한다
  (예: `((MainActivity) getActivity()).openMyPageDrawer()`).
- 화면 전환은 `Intent` + `startActivity`, 데이터 전달은 `intent.putExtra("...", ...)`(모델은 `Serializable`).

## Adapter (RecyclerView)

- `RecyclerView.Adapter<XxxAdapter.ViewHolder>` 를 상속하고, `ViewHolder` 를 정적 내부 클래스로 둔다.
- 생성자는 `(Context, List<모델>, OnItemClickListener)` 형태. 클릭 콜백은 어댑터 내부
  `public interface OnItemClickListener { void onItemClick(모델 item); }` 로 정의한다.
- `onCreateViewHolder` 에서 `LayoutInflater.from(context).inflate(R.layout.item_*, parent, false)`.
- 데이터 교체용 setter(예: `setMovieList(List<...>)`)를 제공하고 `notifyDataSetChanged()` 로 갱신한다.
- `getItemCount()` 은 null 방어(`list != null ? list.size() : 0`).

## 네트워킹: Retrofit 서비스

- API 는 `Service/` 의 인터페이스에 선언한다. 성격에 따라 `AuthService`(로그인/토큰), `MovieService`(콘텐츠)로 나눈다.
- 모든 엔드포인트는 `Call<T>` 반환. `@GET/@POST/@DELETE` + `@Query/@Path/@Body` 로 표현한다.
- 응답 모델은 `DTO/` 의 클래스. 리스트 응답은 `Call<List<Xxx>>`.
- **호출 패턴은 항상 비동기 `enqueue(Callback)`** 을 쓴다(단, 토큰 갱신 등 인증 내부 동기 흐름은 예외):

```java
movieService.getRecommendedMovies().enqueue(new Callback<List<MovieItem>>() {
    @Override
    public void onResponse(Call<List<MovieItem>> call, Response<List<MovieItem>> response) {
        if (response.isSuccessful() && response.body() != null) {
            adapter.setMovieList(response.body());
        } else {
            Log.e("API_ERROR", "영화 리스트 로드 실패: " + response.code());
        }
    }
    @Override
    public void onFailure(Call<List<MovieItem>> call, Throwable t) {
        Log.e("API_FAIL", t.getMessage());
    }
});
```

- `onResponse` 에서는 **반드시 `response.isSuccessful() && response.body() != null`** 로 방어한 뒤 사용한다.
- 로딩 상태는 `ProgressBar` 가시성 토글로 표현한다(요청 전 `VISIBLE`, 완료/실패 시 `GONE`).
- 실패 메시지는 사용자에게 상태 텍스트(예: `tvStatusMsg.setText(...)`)나 `ToastUtil` 로 안내한다.

## 네트워크 클라이언트와 인증 흐름

- Retrofit 인스턴스는 **`RetrofitClient.getClient(context)` 싱글턴**으로만 얻는다. 직접 `new Retrofit.Builder()` 하지 않는다.
  - `BASE_URL` 은 `RetrofitClient` 에 한 곳으로 둔다(도메인 변경은 여기 한 줄).
  - 사용처: `RetrofitClient.getClient(getContext()).create(XxxService.class)`.
- 인증 파이프라인(수정 시 흐름 전체를 이해하고 건드릴 것):
  1. `AuthInterceptor`(Service/) — 매 요청에 저장된 access token 을 `Authorization: Bearer ...` 로 부착.
  2. `TokenAuthenticator` — 401 응답 시 refresh token 으로 `AuthService.requestTokenRefresh` 재발급,
     성공하면 원요청 재시도, 실패하면 토큰 삭제 후 `LoginActivity` 로 스택 클리어 이동
     (`FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`). 무한 루프 방지로 `responseCount >= 2` 면 중단.
  3. `TokenManager` — 토큰/`userId` 를 `EncryptedSharedPreferences`(AES256)로 저장.
     저장 `saveTokens`, 갱신 `updateTokens`, 조회 `getAccessToken/getRefreshToken/getUserId`, 삭제 `clearTokens`.
- 새 인증 필요 API 는 `MovieService` 등 인증 파이프라인이 걸린 클라이언트로 호출하면 토큰이 자동 부착된다.
  토큰을 화면 코드에서 수동으로 헤더에 넣지 않는다.

## 의존 방향

- Activity/Fragment/Adapter(표현) → Service(인터페이스)/DTO/RetrofitClient → OkHttp/Retrofit.
- DTO 는 어떤 UI/Service 도 참조하지 않는 순수 데이터 홀더로 유지한다.
- Service 인터페이스는 안드로이드 UI 타입(View, Activity 등)을 참조하지 않는다.

## 하지 말 것

- 화면 코드에서 `new Retrofit.Builder()` 로 클라이언트 직접 생성(반드시 `RetrofitClient` 경유).
- 새 기능을 Compose 로 작성(이 앱은 View 시스템이 표준이다. 명시 요청 없으면 Java+XML 로 만든다).
- 인증 파이프라인을 우회해 화면에서 토큰 헤더를 수동 조립하거나 `BASE_URL` 을 별도로 하드코딩.
- 루트 `AuthInterceptor` 를 새로 import(중복본이며 `Service/` 버전이 실사용).
- DTO 에 UI/네트워크 로직을 넣거나 Service 인터페이스가 안드로이드 뷰 타입에 의존하게 만들기.
