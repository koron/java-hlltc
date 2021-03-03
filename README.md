# java-hlltc

java-hlltc is an implementation of HyperLogLog-TailCut+ for Java.

This is a port of [axiomhq/hyperloglog][ax] and have compatibility
on serialization format with it.

And this includes [axiomhq/hyperminhash][hmh] also.

## Pre-requirements

java-hlltc is available on [GitHub Packages][gp].
([Japanese version][gp-ja])

~java-hlltc is available on [jcenter/bintray][latest].~

### for Maven

See also [example project](./example-project/pom.xml) for Maven project's settings.

1.  Create a personal access token with `read:packages` permission at <https://github.com/settings/tokens>

2.  Put username and token to your ~/.m2/settings.xml file with `<server>` tag.

    ```pom
    <settings>
      <servers>
        <server>
          <id>github</id>
          <username>USERNAME</username>
          <password>YOUR_PERSONAL_ACCESS_TOKEN_WITH_READ</password>
        </server>
      </servers>
    </settings>
    ```

3.  Add a repository to your `repositories` section in project's pom.xml file.

    ```pom
    <repository>
      <id>github</id>
      <url>https://maven.pkg.github.com/koron/java-hlltc</url>
    </repository>
    ```

4.  Add a `<dependency>` tag to your `<dependencies>` tag.

    ```pom
    <dependency>
      <groupId>net.kaoriya</groupId>
      <artifactId>hlltc</artifactId>
      <version>0.11.0</version>
    </dependency>
    ```

Please read [public document](https://docs.github.com/en/packages/guides/configuring-apache-maven-for-use-with-github-packages) also. ([Japanese](https://docs.github.com/ja/packages/guides/configuring-apache-maven-for-use-with-github-packages))

### for Gradle

See also [example project](./example-project/build.gradle) for Gradle project's settings.

1.  Create a personal access token with `read:packages` permission at <https://github.com/settings/tokens>

2.  Put username and token to your ~/.gradle/gradle.properties file.

    ```
    gpr.user=YOUR_USERNAME
    gpr.key=YOUR_PERSONAL_ACCESS_TOKEN_WITH_READ:PACKAGES
    ```

3.  Add a repository to your `repositories` section in build.gradle file.

    ```groovy
    maven {
        url = uri("https://maven.pkg.github.com/koron/java-hlltc")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
    ```

4.  Add an `implementation` to your `dependencies` section.

    ```groovy
    implementation 'net.kaoriya:hlltc:0.11.0'
    ```

Please read [public document](https://docs.github.com/en/packages/guides/configuring-gradle-for-use-with-github-packages) also. ([Japanese](https://docs.github.com/ja/packages/guides/configuring-gradle-for-use-with-github-packages)).

## Getting Started

To use java-hlltc, you need just four steps.

At first, import `Sketch` class.

```java
import net.kaoriya.hlltc.Sketch;
```

Then create a `Sketch` with default precision 14,
it will use about 8KB (= `2^(14-1)`) for serialization.

```java
Sketch sk = new Sketch();
```

Next, insert values for counting cardinality.
`Sketch#insert()` accepts `byte[]` value.

```java
for (int i = 0; i < 1000000; i++) {
  String key = String.format("some_id-%d", i);
  sk.insert(key.getBytes());
}
```

At last, you can estimate its cardinality with `Sketch#estimate()` method.

```java
long cnt = sk.estimate();
```

### Serialization

Serialization means that getting a byte stream format of a `Sketch`, and vice
versa. The format has compatibility with [axiomhq/hyperloglog][ax].

You can serialize (get a byte arary of) a `Sketch` with `Sketch#toBytes()`
method like this.

```java
byte[] b = sk.toBytes();
// TODO: write b to file or so.
```

Then you can deserialize it with `Sketch#fromBytes()` method.
Deserialized `Sketch` will have same cardinality with another `Sketch` which
serialize the byte array.

```java
// FIXME: b might be loaded from file or so.
Sketch sk2 = Sketch.fromBytes(b);

// cnt2 will match with cnt.
long cnt2 = sk2.estimate();
```

### Merge Sketches

`Sketche#merge()` method merges another `Sketch` into the receiver.
Merged `Sketch` will estimate merged cardinality well.
Note that each `Sketch` should have same precision to merge.

```java
sk.merge(sk2);
// sk has merged cardinality.
```

### Interoperate with Golang

This section describes how to use [axiomhq/hyperloglog][ax].


At first, import [axiomhq/hyperloglog][ax] like this.

```go
import "github.com/axiomhq/hyperloglog"
```

Then create a `Sketch` with `hyperloglog.New()` function.

```go
sk := hyperloglog.New()
```

Next, insert values for measuring cardinality.

```go
for i := 0 ; i < 1000000; i++ {
  key := fmt.Sprintf("some_id-%d", i)
  sk.Insert([]byte(key))
}
```

At last, you can estimate its cardinality with `Estimate()` method.

```go
cnt := sk.Estimate()
```

How to do for serialization (marshaling) like this.

```go
b, err := sk.MarshalBinary()
if err != nil {
  return err
}
```

And this code shows how to unmarshal it.

```go
sk2 := hyperloglog.New()
err = sk2.UnmarshalBinary(b)
if err != nil {
  return err
}
```

At the end, this shows how to merge.

```go
err := sk.Merge(sk2)
if err != nil {
  return err
}
```

### Intersection with HyperMinHash sketch

Prepare two `MinHashSketch` sketches.

```java
MinHashSketch sk1 = new MinHashSketch();
MinHashSketch sk2 = new MinHashSketch();
// TODO: `add()` data to sk1 and sk2.
```

Then `intersection()` returns estimated number of shared members in two sets.

```java
long res = sk1.intersection(sk2);
```

## Benchmark

| Exact    | hlltc-14                | hlltc-15               | axiom-14                | axiom-15               | Influx HLL+ (16391)     |
|---------:|------------------------:|-----------------------:|------------------------:|-----------------------:|------------------------:|
|       10 |       10 (0.0000% off)  |      10 (0.0000% off)  |       10 (0.0000% off)  |      10 (0.0000% off)  |       10 (0.0000% off)  |
|       50 |       50 (0.0000% off)  |      50 (0.0000% off)  |       50 (0.0000% off)  |      50 (0.0000% off)  |       50 (0.0000% off)  |
|      250 |      250 (0.0000% off)  |     250 (0.0000% off)  |      250 (0.0000% off)  |     250 (0.0000% off)  |      250 (0.0000% off)  |
|     1250 |     1250 (0.0000% off)  |    1250 (0.0000% off)  |     1250 (0.0000% off)  |    1250 (0.0000% off)  |     1250 (0.0000% off)  |
|     6250 |     6191 (0.9440% off)  |  **6250 (0.0000% off)**|     6191 (0.9440% off)  |  **6250 (0.0000% off)**|   **6250 (0.0000% off)**|
|    31250 |    30979 (0.8672% off)  |   31657 (1.3024% off)  |    30979 (0.8672% off)  |   31657 (1.3024% off)  |  **30996 (0.8128% off)**|
|   156250 |   156012 (0.1523% off)  |**156184 (0.0422% off)**|   156012 (0.1523% off)  |**156184 (0.0422% off)**|   156715 (0.2976% off)  |
|   781250 | **782363 (0.1425% off)**|  777083 (0.5334% off)  | **782363 (0.1425% off)**|  777083 (0.5334% off)  |   775988 (0.6735% off)  |
|  3906250 |  3869332 (0.9451% off)  | 3837943 (1.7487% off)  |  3869331 (0.9451% off)  | 3837943 (1.7487% off)  |**3889909 (0.4183% off)**|
| 10000000 |**9952687 (0.4731% off)**| 9881121 (1.1888% off)  |**9952681 (0.4732% off)**| 9881119 (1.1888% off)  |  9889556 (1.1044% off)  |
| **Size** |                   8200  |                 16392  |                   8200  |                 16392  |                  16391  |

See [here][benchmark_dir] for other benchmarks.

## Appendix

### Popular API signature

*   `class Sketch`

    Small footprint HyperLogLog.

    *   `Sketch()` - constructor with default precision (14)
    *   `void insert(byte[] d)` - insert/count a data
    *   `long estimate()` - estimate cardinality
    *   `byte[] toBytes() throws IOException` - serialize as byte array
    *   `static Sketch fromBytes(byte[] b) throws IOException` -
        deserialize from byte array
    *   `Sketch merge(Sketch other)` - merge other `Sketch` to this
    *   `Sketch(int precision)` - constructor with precision (between 4 and 18)
    *   `Sketch clone()` - create a clone of the `Sketch`

*   `class MinHashSketch`

    Pure HyperLogLog + MinHash. Intersection estimation.

    *   `MinHashSketch()` - constructor with default precision (14)
    *   `void add(byte[] d)` - insert/count a data
    *   `long cardinality()` - estimate cardinality
    *   `void merge(MinHashSketch other)` - merge other `MinHashSketch` to this
    *   `double similarity(MinHashSketch other)` - estimate similarity
    *   `long intersection(MinHashSketch other)` - estimate intersection
    *   `byte[] toBytes() throws IOException` - serialize as byte array
    *   `static MinHashSketch fromBytes(byte[] b) throws IOException` -
        deserialize from byte array
    *   `MinHashSketch clone()` - create a clone of the `MinHashSketch`

[latest]:https://bintray.com/koron/hlltc/net.kaoriya.hlltc/_latestVersion
[ax]:https://github.com/axiomhq/hyperloglog
[hmh]:https://github.com/axiomhq/hyperminhash
[benchmark_dir]:tree/master/benchmark
[gp]:https://docs.github.com/en/packages
[gp-ja]:https://docs.github.com/ja/packages
