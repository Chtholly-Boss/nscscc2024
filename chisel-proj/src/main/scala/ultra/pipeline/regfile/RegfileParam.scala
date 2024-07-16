package ultra.pipeline.regfile

import chisel3._

object RegfileParam {
  val addrWidth = 5.W
  val dataWidth = 32.W
  // Regfile Table
  object RegsNames {
    val zero =	"r0"
    val ra =	"r1"
    val tp =	"r2"
    val sp =	"r3"
    val a0 =	"r4"
    val a1 =	"r5"
    val a2 =	"r6"
    val a3 =	"r7"
    val a4 =	"r8"
    val a5 =	"r9"
    val a6 =	"r10"
    val a7 =	"r11"
    val v0 =	"r10" // r4
    val v1 =	"r11" // r5
    val t0 =	"r12"
    val t1 =	"r13"
    val t2 =	"r14"
    val t3 =	"r15"
    val t4 =	"r16"
    val t5 =	"r17"
    val t6 =	"r18"
    val t7 =	"r19"
    val t8 =	"r20"
    val x  =	"r21"
    val fp =	"r22"
    val s0 =	"r23"
    val s1 =	"r24"
    val s2 =	"r25"
    val s3 =	"r26"
    val s4 =	"r27"
    val s5 =	"r28"
    val s6 =	"r29"
    val s7 =	"r30"
    val s8 =	"r31"
  }

}