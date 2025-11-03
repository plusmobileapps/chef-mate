# Chef Mate

An app that is your mate in the kitchen managing the grocery list and keeping recipes to help you cook.

## Getting Started

This is a [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) project targeting Android, iOS, Web, Desktop (JVM), Server. It aims to share as much code as possible in a modular way, so the general file structure of the project is as follows: 

* [client](./client/) - all the shared client code and client application code
  * [composeApp](./client/composeApp/) - the compose multiplatform applications
  * [database](./client/database/) - client database
  * [root](./client/root/) - root BLoC (business logic component) to manage navigation of the whole app
  * [grocery](./client/grocery/) - list and detail BLoCs for groceries
  * [shared](./client/shared/) - common client code
  * [testing](./client/testing/) - common testing helpers
* [server](./server/) - backend code
* [build-logic](./build-logic/) - all the convention plugins to share logic of modular libraries

### Android

```shell
./gradlew :client:composeApp:installDebug
```

### Desktop (JVM)

```shell
./gradlew :client:composeApp:installDebug
```

### iOS

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.


### Server

```shell
./gradlew :server:run
```
---

## Libraries Used

### Shared

* [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) - asynchronous programming framework
* [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) - multiplatform serialization library
* [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) - multiplatform date time library
* [Ktlint](https://github.com/JLLeitschuh/ktlint-gradle) - kotlin code formatting and linter

### Client

* [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform) - shared UI for client
* [SqlDelight](https://sqldelight.github.io/sqldelight/2.1.0/) - sqlite database
* [Essenty](https://github.com/arkivanov/Essenty) - lifecycle, instance keeper, back handler, state keeper
* [Decompose](https://github.com/arkivanov/Decompose) - navigation
* [kotlin-inject](https://github.com/evant/kotlin-inject) - dependency injection
* [kotlin-inject-anvil](https://github.com/amzn/kotlin-inject-anvil) - dependency injection extensions
* [kotlin-inject-anvil-extensions](https://github.com/plusmobileapps/kotlin-inject-anvil-extensions) - assisted factory dependency injection

### Server

* [Ktor](https://github.com/ktorio/ktor) - backend framework

### Testing

* [Kotlin test](https://kotlinlang.org/api/core/kotlin-test/) - KMP testing framework
* [Kotest](https://kotest.io/docs/assertions/assertions.html) - assertions
* [Mokkery](https://mokkery.dev/) - mocking library
* [Turbine](https://github.com/cashapp/turbine) - coroutines testing library