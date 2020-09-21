```groovy
repositories {
    . . .
    maven {url = "https://dl.bintray.com/user11681/maven"}
}

dependencies {
    . . .
    modImplementation(include("user11681:dynamicentry:+"))
}
```
