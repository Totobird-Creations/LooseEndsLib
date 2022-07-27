# Loose Ends Lib
A library that controls how servers and clients will act when joining without the mod.

__Installation__:
Add the following to your `build.gradle`.
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    modImplementation 'com.github.Totobird-Creations:LooseEndsLib:${looseendslib_version}'
    // Optional.
    include 'com.github.Totobird-Creations:LooseEndsLib:${looseendslib_version}'
}
```
Add the following to your `gradle.properties`.
```properties
looseendslib_version = v1.0.1-mc1.19
```

__Usage__:
In your initialiser's `onInitialize` method, add the following:
```java
LooseEndManager.getInstance().register(mod_id, mod_name, mod_version)
    .whenClientJoins(condition)
    .whenJoinServer(condition);
```
Conditions:
- `LooseEnd.Condition.REQUIRED` : The peer must also have the mod.
- `LooseEnd.Condition.NONE` : Nothing happens.
- `LooseEnd.Condition.DISALLOWED` : The peer can not have the mod.
