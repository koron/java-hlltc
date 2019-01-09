# java-hlltc

java-hlltc is an implementation of HyperLogLog-TailCut+.

This is a port of [axiomhq/hyperloglog][ax] and have compatibility
on serialization format with it.


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

[ax]:https://github.com/axiomhq/hyperloglog
