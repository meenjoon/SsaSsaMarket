<h1 align="center">SsaSsa Market</h1>

<p align="center">
  <a href="https://kotlinlang.org"><img alt="Kotlin Version" src="https://img.shields.io/badge/Kotlin-1.7.20-blueviolet.svg?style=flat"/></a>
  <a href="https://android-arsenal.com/api?level=24"><img alt="API" src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://firebase.google.com/support/release-notes/android#bom_v32-2-0"><img alt="Fireabase" src="https://img.shields.io/badge/Fireabase-bom:32.0.0-blue.svg"/></a>
  <a href="https://developer.android.com/studio/releases/gradle-plugin"><img alt="build.gradle.kts" src="https://img.shields.io/badge/build.gradle.kts-blue?style=flat"/></a>
</p>

위치 기반 거래 앱으로, 사용자는 위치 권한을 허용해야만 상품 등록, 구매, 채팅이 사용할 수 있도록 하여 안전하고 신뢰성 높은 앱을 이용할 수 있도록 지원하였으며, 더 나아가 채팅 기능 사용 시 판매자와 구매자의 최신 업데이트 된 위치를 기반으로 채팅방 내부에 상대방의 지역과 상대방과의 거리차를 표시하여 안전하고 신뢰성 높은 거래를 스스로 판단을 하게끔 도와주는 어플리케이션입니다.

<div style="text-align:center">
    <img width = '900' src="https://github.com/meenjoon/SsaSsaMarket/assets/88024665/0a35761e-701a-4ef3-9941-394da1fba50e" alt="SsaSsa Market">
</div>

## Go Play Store ➔ [Store](https://play.google.com/store/apps/details?id=com.mbj.ssassamarket) <br>
## Go document ➔  [Documented files](https://docs.google.com/spreadsheets/d/1TUqe6q8c2EUH-EN_W1N8Z9lt2UwcFd1BynxX0GRGyXg/edit?usp=sharing)
## Go Figma ➔  [Figma files](https://www.figma.com/file/3Y0TEWGzYi9iTlQOFXNNk2/Ssa-Ssa-Market?type=design&node-id=0%3A1&mode=design&t=IMRA7szPslcJnFJr-1)
## Go UserFlow ➔ [UserFlow](https://drive.google.com/file/d/1rlRqZj6xH3i-ib6l6zWGEfG1164BbdqG/view?usp=sharing)
## Go ERD ➔ [ERD](https://www.erdcloud.com/d/QyFKWgEhLc9mtu2Sa)

<br>

## Tech stack & Open-source libraries

### Android

- Minimum SDK level 24 / Target SDK level 33
- [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous.
- JetPack
  - [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) - Create a UI that automatically responds to lifecycle events.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Build data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Store UI related data that isn't destroyed on app rotations.
  - [Room](https://developer.android.com/training/data-storage/room) - Constructs Database by providing an abstraction layer over SQLite to allow fluent database access.
  - [DataBinding](https://developer.android.com/topic/libraries/data-binding) - Useful to bind data directly through layouts xml file, so no `findViewById()` anymore.
  - [Navigation](https://developer.android.com/guide/navigation) - Handles navigating between your app's destinations.
- [Hilt](https://dagger.dev/hilt/) - Dependency injection.
- [Coil](https://coil-kt.github.io/coil/) - An image loading library for Android backed by Kotlin Coroutines.
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit) - Construct the REST APIs.
- [Moshi](https://github.com/square/moshi) - Moshi is a modern JSON library for Android that simplifies JSON parsing and serialization.

### API Data & Open API

- [Firebase](https://firebase.google.com/)
  - Realtime Database
  - Storage
  - Crashlytics
  - Cloud Messaging

- [Kakao Map](https://apis.map.kakao.com/android/) - The Kakao Maps Open API provides access to mapping and location-based services for developers to integrate into their applications.

## Features(Android 10[API Level 29])

> ### LogIn & Nickname Setting & Auto LogIn

<div align="center">

| Login screen <br> (Use Google Login) | Nickname setting screen <br> (pattern & duplicate check) | Login screen <br> (automatic login function) |
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/login.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/auto_login.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/setting_nickname.webp" align="center" width="300px"/> |
</div>

> ### Home screen & Product registration screen

<div align="center">

| Home screen <br> (search filtering <br> sorting functions <br> [Latest, Lowest, Like]) | Home screen <br> (by category, clothing, <br> home appliances, food, <br> furniture, etc)| Product registration screen <br> (only when location permission is allowed <br> & automatically inserts location data) |
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/home_filter_sort.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/home_swipe.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/product_registration.webp" align="center" width="300px"/> |


</div>

> ### Buyer screen

<div align="center">

| Buyer screen <br> (like function) | Buyer screen <br> (chat function)| Buyer screen <br> (purchase function) |
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/product_favorite.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/product_chat.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/product_purchase.webp" align="center" width="300px"/> |


</div>


> ### Seller screen

<div align="center">

| Seller screen <br> (deletion function) | Seller screen <br> (modification function) |
| :---------------: | :---------------: |
| <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/product_deletion%20.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/product_modification.webp" align="center" width="300px"/> |


</div>

> ### Inventory screen & Setting screen

<div align="center">

| Buyer screen <br> (product like list, <br> product registration list, <br> product purchase list) | Settings screen <br> (logout function) |  Settings screen <br> (member withdrawal function)|
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/inventory.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/logout.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/membership_withdrawal.webp" align="center" width="300px"/> |


</div>

> ### Notification Function & Chat Function

<div align="center">

| Notification <br> (chat function) | Notification <br> (sell function) | Chat <br>
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/notification_chat.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/notification_sell.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/chat.webp" align="center" width="300px"/>


</div>

> ### Themes

<div align="center">

| Lite Theme | Dark Theme|
| :---------------: | :---------------: |
| <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/theme_lite.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/theme_dark.webp" align="center" width="300px"/> |


</div>


> ### Network Connectivity

<div align="center">

| Network Disconnected | Network Connected |
| :---------------: | :---------------: |
| <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/network_disconnected.webp" align="center" width="300px"/> | <img src="https://github.com/meenjoon/SsaSsaMarket/blob/main/docs/network_connected.webp" align="center" width="300px"/> |

</div>

## Architecture

SsaSsa Market is based on the MVVM architecture and the Repository pattern.

<p align = 'center'>
<img width = '600' src = 'https://user-images.githubusercontent.com/39554623/184456867-195f5989-dc9a-4dea-8f35-41e1f11145ff.png'>
</p>

Currently, product data is obtained when users register products by making API calls to the root object of Firebase Realtime Database. This data retrieval method is utilized for subsequent access.

<p align = 'center'>
<img width = '900' src = 'https://github.com/meenjoon/SsaSsaMarket/assets/88024665/5a797e5e-7b34-4bf7-a895-60d4e8048686'>
</p>

## Design in Figma

<p align = 'center'>
<img width = '900' src = 'https://github.com/meenjoon/SsaSsaMarket/assets/88024665/a45d88dd-a001-4cdc-8636-34ed200a53d8'>
</p>

## User Flow

<p align = 'center'>
<img width = '900' src = 'https://github.com/meenjoon/SsaSsaMarket/assets/88024665/ce5b5ca2-aaa8-4e82-8973-df9039760371'>
</p>

## ERD
<p align = 'center'>
<img width = '900' src = 'https://github.com/meenjoon/SsaSsaMarket/assets/88024665/69056650-c764-44eb-97ed-39dc0074879c'>
</p>