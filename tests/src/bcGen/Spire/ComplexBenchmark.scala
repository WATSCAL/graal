package bcGen.Spire

object ComplexBenchmark:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 100
  private final val N = 512

  private def xReal(i: Int): Int = (i % 23) - 11
  private def xImag(i: Int): Int = (i % 19) - 9
  private def yReal(i: Int): Int = (i % 17) - 8
  private def yImag(i: Int): Int = (i % 13) - 6

  private val intXs: Array[Complex[Int]] =
    Array.tabulate(N) { i =>
      new Complex[Int](xReal(i), xImag(i))
    }

  private val intYs: Array[Complex[Int]] =
    Array.tabulate(N) { i =>
      new Complex[Int](yReal(i), yImag(i))
    }


  private val longXs: Array[Complex[Long]] =
    Array.tabulate(N) { i =>
      new Complex[Long](xReal(i).toLong, xImag(i).toLong)
    }

  private val longYs: Array[Complex[Long]] =
    Array.tabulate(N) { i =>
      new Complex[Long](yReal(i).toLong, yImag(i).toLong)
    }


  private val doubleXs: Array[Complex[Double]] =
    Array.tabulate(N) { i =>
      new Complex[Double](xReal(i).toDouble, xImag(i).toDouble)
    }

  private val doubleYs: Array[Complex[Double]] =
    Array.tabulate(N) { i =>
      new Complex[Double](yReal(i).toDouble, yImag(i).toDouble)
    }
  
  private val expectedPerRepeat: Long =
    var total = 0L
    var i = 0
    while i < N do
      val a = xReal(i).toLong
      val b = xImag(i).toLong
      val c = yReal(i).toLong
      val d = yImag(i).toLong
      val real = a * c - b * d + a
      val imag = b * c + a * d + b
      total += real
      total += imag
      i += 1
    total

  private val expectedTotal: Long = expectedPerRepeat * INNER_REPEAT

  def complexBenchmark[A](xs: Array[Complex[A]], ys: Array[Complex[A]])(using ring: Ring[A]): Unit =
    var total = 0L
    var repeat = 0
    while repeat < INNER_REPEAT do
      var checksum: A = ring.zero
      var i = 0
      while i < N do
        val multiplied: Complex[A] = xs(i) * ys(i)
        val result: Complex[A] = multiplied + xs(i)
        checksum = ring.plus(checksum, result.real)
        checksum = ring.plus(checksum, result.imag)
        i += 1
      total += ring.toLong(checksum)
      repeat += 1
    if total != expectedTotal then
      println(s"Error: total=$total, expectedTotal=$expectedTotal")


  @main def runSpireComplexMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        complexBenchmark[Int](intXs, intYs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1

  @main def runSpireComplexMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        complexBenchmark[Int](intXs, intYs)
        complexBenchmark[Long](longXs, longYs)
        complexBenchmark[Double](doubleXs, doubleYs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1