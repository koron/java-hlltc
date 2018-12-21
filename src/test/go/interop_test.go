package main

import (
	"fmt"
	"io/ioutil"
	"math"
	"testing"

	"github.com/axiomhq/hyperloglog"
)

func loadSketch(t *testing.T, name string) *hyperloglog.Sketch {
	t.Helper()
	b, err := ioutil.ReadFile(name)
	if err != nil {
		t.Fatalf("failed to read file: %s", err)
	}
	sk := &hyperloglog.Sketch{}
	err = sk.UnmarshalBinary(b)
	if err != nil {
		t.Fatalf("failed to unmarshal sketoch: %s", err)
	}
	return sk
}

func assertSketch(t *testing.T, sk *hyperloglog.Sketch, exp int64, th float64) {
	t.Helper()
	res := sk.Estimate()
	fexp := float64(exp)
	rate := 100.0 * math.Abs(float64(res)-fexp) / fexp
	if rate > th {
		t.Errorf("exact %d, got %d which is %.2f%% error", exp, res, rate)
		return
	}
	//t.Logf("OK: exact %d, got %d which is %.2f%% error", exp, res, rate)
}

func TestLoadSparse(t *testing.T) {
	sk := loadSketch(t, "testdata/sparse-14-3400.hlltc")
	assertSketch(t, sk, 3400, 2)
}

func TestLoadDense(t *testing.T) {
	sk := loadSketch(t, "testdata/dense-14-1M.hlltc")
	assertSketch(t, sk, 1000000, 2)
}

func TestMergeSparse(t *testing.T) {
	sk0 := loadSketch(t, "testdata/sparse-14-3400.hlltc")
	sk1 := loadSketch(t, "testdata/sparse-14-3400.hlltc")

	sk := hyperloglog.New14()
	for i := 1; i <= 3400; i += 2 {
		sk.Insert([]byte(fmt.Sprintf("flow-%d", i)))
	}
	assertSketch(t, sk, 1700, 2)

	sk0.Merge(sk)
	assertSketch(t, sk0, 3400, 2)

	sk.Merge(sk1)
	assertSketch(t, sk, 3400, 2)
}

func TestMergeDense(t *testing.T) {
	sk0 := loadSketch(t, "testdata/dense-14-1M.hlltc")
	sk1 := loadSketch(t, "testdata/dense-14-1M.hlltc")

	sk := hyperloglog.New14()
	for i := 1; i <= 1000000; i += 2 {
		sk.Insert([]byte(fmt.Sprintf("flow-%d", i)))
	}
	assertSketch(t, sk, 500000, 2)

	sk0.Merge(sk)
	assertSketch(t, sk0, 1000000, 2)

	sk.Merge(sk1)
	assertSketch(t, sk, 1000000, 2)
}

func TestMergeDense25per(t *testing.T) {
	sk0 := loadSketch(t, "testdata/dense-14-1M.hlltc")
	sk1 := loadSketch(t, "testdata/dense-14-1M.hlltc")

	sk := hyperloglog.New14()
	for i := 1; i <= 1000000; i++ {
		sk.Insert([]byte(fmt.Sprintf("flow-%d", i + 250000)))
	}
	assertSketch(t, sk, 1000000, 2)

	sk0.Merge(sk)
	assertSketch(t, sk0, 1250000, 2)

	sk.Merge(sk1)
	assertSketch(t, sk, 1250000, 2)
}

func TestMergeDense50per(t *testing.T) {
	sk0 := loadSketch(t, "testdata/dense-14-1M.hlltc")
	sk1 := loadSketch(t, "testdata/dense-14-1M.hlltc")

	sk := hyperloglog.New14()
	for i := 1; i <= 1000000; i++ {
		sk.Insert([]byte(fmt.Sprintf("flow-%d", i + 500000)))
	}
	assertSketch(t, sk, 1000000, 2)

	sk0.Merge(sk)
	assertSketch(t, sk0, 1500000, 2)

	sk.Merge(sk1)
	assertSketch(t, sk, 1500000, 2)
}

func TestMergeDense75per(t *testing.T) {
	sk0 := loadSketch(t, "testdata/dense-14-1M.hlltc")
	sk1 := loadSketch(t, "testdata/dense-14-1M.hlltc")

	sk := hyperloglog.New14()
	for i := 1; i <= 1000000; i++ {
		sk.Insert([]byte(fmt.Sprintf("flow-%d", i + 750000)))
	}
	assertSketch(t, sk, 1000000, 2)

	sk0.Merge(sk)
	assertSketch(t, sk0, 1750000, 2)

	sk.Merge(sk1)
	assertSketch(t, sk, 1750000, 2)
}

func TestMergeDense99per(t *testing.T) {
	sk0 := loadSketch(t, "testdata/dense-14-1M.hlltc")
	sk1 := loadSketch(t, "testdata/dense-14-1M.hlltc")

	sk := hyperloglog.New14()
	for i := 1; i <= 1000000; i++ {
		sk.Insert([]byte(fmt.Sprintf("flow-%d", i + 990000)))
	}
	assertSketch(t, sk, 1000000, 2)

	sk0.Merge(sk)
	assertSketch(t, sk0, 1990000, 2)

	sk.Merge(sk1)
	assertSketch(t, sk, 1990000, 2)
}
