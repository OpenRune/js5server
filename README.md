# js5server

_fast simple JS5 server_

Use Java 17 (or change toolchain in `build.gradle.kts`) and start via `run` Gradle task.

Configure with `js5server.properties` file.

### Add as a library

In `build.gradle.kts` (Kotlin)

```kotlin
implementation("dev.openrune:js5server:1.0.4")
```

Or in `build.gradle` (Groovy)

```groovy
implementation 'dev.openrune:js5server:1.0.4'
```

You will also need to declare the following under your repositories to add the maven remote

```kotlin
   maven("https://raw.githubusercontent.com/OpenRune/hosting/master")
```
