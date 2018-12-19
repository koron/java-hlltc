package main

import (
	"fmt"
	"io/ioutil"
	"log"

	"github.com/axiomhq/hyperloglog"
)

func main() {
	sk1 := hyperloglog.New14()
	for i := 0; i <= 3400; i++ {
		k := fmt.Sprintf("flow-%d", i)
		sk1.Insert([]byte(k))
	}
	b1, err := sk1.MarshalBinary()
	if err != nil {
		log.Fatalf("sk1.MarshalBinary failed: %s", err)
	}
	if b1[3] != 0x01 {
		log.Fatalf("sk1 should be sparse: %02x", b1[3])
	}
	err = ioutil.WriteFile("sparse-3400.hlltc", b1, 0666)
	if err != nil {
		log.Fatalf("sk1 save failed: %s", err)
	}

	sk2 := hyperloglog.NewNoSparse()
	for i := 0; i <= 1000000; i++ {
		k := fmt.Sprintf("flow-%d", i)
		sk2.Insert([]byte(k))
	}
	b2, err := sk2.MarshalBinary()
	if err != nil {
		log.Fatalf("sk2.MarshalBinary failed: %s", err)
	}
	if b2[3] != 0x00 {
		log.Fatalf("sk2 should be dense: %02x", b2[3])
	}
	err = ioutil.WriteFile("dense-1M.hlltc", b2, 0666)
	if err != nil {
		log.Fatalf("sk2 save failed: %s", err)
	}
}
