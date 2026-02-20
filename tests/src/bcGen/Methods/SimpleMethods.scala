package bcGen.Methods

class SimpleMethods {
    def identity[T](value: T): T = value
  
    def first[U, V](fst: U, snd: V): U = fst

    def A[T](t: T)[S, V](s1 : S, v: V, v2: V, s2: S)
        [W, X, Y](w: W, x: X, y: Y)(y2: Y, y3: Y, y4: Y, w2: W)
        (byte: Byte, int: Int, double: Double, ref: Any) = {
        println(s"A called with t: $t, s1: $s1, v: $v, v2: $v2, s2: $s2," +
          s"w: $w, x: $x, y: $y, y2: $y2, y3: $y3, y4: $y4, w2: $w2" +
          s", byte: $byte, int: $int, double: $double, ref: $ref")
    }
}

object testSimpleMethods {

    val simpleMethods = new SimpleMethods
    val rng = new scala.util.Random(1234)

    def genericFirst[U, V](fst: U, snd: V): U = simpleMethods.first[U, V](fst, snd)

    @main def testSimple1(): Unit = {
        val v: Int = simpleMethods.identity[Int](42)
        println(v)
    }

    // this should fail
    @main def testSimple2(): Unit = 
        val v: Any = simpleMethods.identity[Int](42)
        println(v)
    
    def idAny(x: Any): Any = x
    def foo3[T](x: T): Any = idAny(x)

    // this should fail
    @main def testSimple3(): Unit = 
        val v: Any = foo3[Int](42)
        println(v)

    def foo4[T](x: T): Any = simpleMethods.identity[Any](x)

    // this should fail
    @main def testSimple4(): Unit = 
        val v: Any = foo4[Int](42)
        println(v)

    @main def testSimple5(): Unit = {
        simpleMethods.A[Int](10)
            [String, Double]("hello", 3.14, 2.0, "s2")
            [Boolean, Char, Long](true, 'c', 100L)(101L, 102L, 103L, false)
            (1.toByte, 42, 2.71, "ref")
    }

    @main def runIdentitySimple(): Unit = {
        val repeat = 1000000
        val input = 42
        var sum = 0
        var i = 0
        while (i < repeat) {
            sum += simpleMethods.identity[Int](input)
            i += 1
        }
        println(s"Sum of $repeat identities of $input is $sum")
    }

    @main def runFirstMixed(): Unit = {
        val repeat = 10000
        var i = 0
        var sumInt = 0
        var sumDouble = 0.0
        while (i < repeat) {
            val n = rng.nextInt(2)
            if (n == 0) {
                sumInt += simpleMethods.first[Int, Double](i, i.toDouble)
            } else {
                sumDouble += simpleMethods.first[Double, Int](i.toDouble, i)
            }
            i += 1
        }
        println(s"Sum of $repeat firsts is int: $sumInt, double: $sumDouble")
    }

    @main def runGenericFirstMixed(): Unit = {
        val repeat = 10000
        var i = 0
        var sumInt = 0
        var sumDouble = 0.0
        while (i < repeat) {
            val n = rng.nextInt(2)
            if (n == 0) {
                sumInt += genericFirst[Int, Double](i, i.toDouble)
            } else {
                sumDouble += genericFirst[Double, Int](i.toDouble, i)
            }
            i += 1
        }
        println(s"Sum of $repeat genericFirsts is int: $sumInt, double: $sumDouble")
    }

    @main def runAllSimpleMethods(): Unit = {
        testSimple1()
        // testSimple2()
        // testSimple3()
        // testSimple4()
        testSimple5()
        runIdentitySimple()
        runFirstMixed()
        runGenericFirstMixed()
    }
}