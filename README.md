# java-hlltc

java-hlltc is an implementation of HyperLogLog-TailCut+.

This is a port of [axiomhq/hyperloglog][ax] and have compatibility
on serialization format with it.

## Pre-requirements

java-hlltc is available on [jcenter/bintray][latest].


### for Maven

Add this to your pom.xml:

```pom
<dependency>
  <groupId>net.kaoriya</groupId>
  <artifactId>hlltc</artifactId>
  <version>0.9.1</version>
  <type>pom</type>
</dependency>
```

### for Gradle

Copy this to your `dependencies` section.

```groovy
compile 'net.kaoriya:hlltc:0.9.1'
```

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
    *   `Sketch()` - constructor with default precision (14)
    *   `void insert(byte[] d)` - insert/count a data
    *   `long estimate()` - estimate cardinality
    *   `byte[] toBytes() throws IOException` - serialize as byte array
    *   `static Sketch fromBytes(byte[] b) throws IOException` -
        deserialize from byte array
    *   `Sketch merge(Sketch other)` - merge other `Sketch` to this
    *   `Sketch(int precision)` - constructor with precision (between 4 and 18)
    *   `Sketch clone()` - create a clone of the `Sketch`

[latest]:https://bintray.com/koron/hlltc/net.kaoriya.hlltc/_latestVersion
[ax]:https://github.com/axiomhq/hyperloglog
[benchmark_dir]:tree/master/benchmark
