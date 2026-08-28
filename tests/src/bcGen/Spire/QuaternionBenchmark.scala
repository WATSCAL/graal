package bcGen.Spire

object QuaternionBenchmark:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 50
  private final val N = 256

  private def xr(i: Int): Int = (i % 11) - 5
  private def xi(i: Int): Int = (i % 13) - 6
  private def xj(i: Int): Int = (i % 17) - 8
  private def xk(i: Int): Int = (i % 19) - 9

  private def yr(i: Int): Int = (i % 7) - 3
  private def yi(i: Int): Int = (i % 9) - 4
  private def yj(i: Int): Int = (i % 15) - 7
  private def yk(i: Int): Int = (i % 21) - 10


  private val intXs: Array[Quaternion[Int]] =
    Array.tabulate(N) { i => Quaternion[Int](xr(i), xi(i), xj(i), xk(i)) }

  private val intYs: Array[Quaternion[Int]] =
    Array.tabulate(N) { i => Quaternion[Int](yr(i), yi(i), yj(i), yk(i)) }

  private val longXs: Array[Quaternion[Long]] =
    Array.tabulate(N) { i => Quaternion[Long](xr(i).toLong, xi(i).toLong, xj(i).toLong, xk(i).toLong) }

  private val longYs: Array[Quaternion[Long]] =
    Array.tabulate(N) { i => Quaternion[Long](yr(i).toLong, yi(i).toLong, yj(i).toLong, yk(i).toLong) }

  private val doubleXs: Array[Quaternion[Double]] =
    Array.tabulate(N) { i => Quaternion[Double](xr(i).toDouble, xi(i).toDouble, xj(i).toDouble, xk(i).toDouble) }

  private val doubleYs: Array[Quaternion[Double]] =
    Array.tabulate(N) { i => Quaternion[Double](yr(i).toDouble, yi(i).toDouble, yj(i).toDouble, yk(i).toDouble) }

  private val expectedPerRepeat: Long =
    var total = 0L
    var n = 0
    while n < N do
      val ar = xr(n).toLong
      val ai = xi(n).toLong
      val aj = xj(n).toLong
      val ak = xk(n).toLong
      val br = yr(n).toLong
      val bi = yi(n).toLong
      val bj = yj(n).toLong
      val bk = yk(n).toLong
      val rr = ar * br - ai * bi - aj * bj - ak * bk
      val ri = ar * bi + ai * br + aj * bk - ak * bj
      val rj = ar * bj - ai * bk + aj * br + ak * bi
      val rk = ar * bk + ai * bj - aj * bi + ak * br
      total += rr
      total += ri
      total += rj
      total += rk
      n += 1
    total

  private val expectedTotal = expectedPerRepeat * INNER_REPEAT

  def quaternionBenchmark[A](xs: Array[Quaternion[A]], ys: Array[Quaternion[A]])(using ring: Ring[A]): Unit =
    var total = 0L
    var repeat = 0
    while repeat < INNER_REPEAT do
      var checksum: A = ring.zero
      var i = 0
      while i < N do
        val result: Quaternion[A] = xs(i) * ys(i)
        checksum = ring.plus(checksum, result.r)
        checksum = ring.plus(checksum, result.i)
        checksum = ring.plus(checksum, result.j)
        checksum = ring.plus(checksum, result.k)
        i += 1
      total += ring.toLong(checksum)
      repeat += 1
    if total != expectedTotal then
      println(s"Error: total=$total, expectedTotal=$expectedTotal")

  @main def runSpireQuaternionMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        quaternionBenchmark[Int](intXs, intYs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1


  @main def runSpireQuaternionMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        quaternionBenchmark[Int](intXs, intYs)
        quaternionBenchmark[Long](longXs, longYs)
        quaternionBenchmark[Double](doubleXs, doubleYs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1