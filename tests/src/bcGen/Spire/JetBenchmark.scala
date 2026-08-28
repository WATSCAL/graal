package bcGen.Spire

object JetBenchmark:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 50
  private final val N = 64
  private final val DIMENSION = 8

  private def xReal(i: Int): Int = (i % 17) - 8
  private def yReal(i: Int): Int = (i % 13) - 6

  private def xDerivative(i: Int, dimension: Int): Int = ((i + dimension * 3) % 11) - 5
  private def yDerivative(i: Int, dimension: Int): Int = ((i * 2 + dimension * 5) % 9) - 4

  private def makeIntX(i: Int): Jet[Int] =
    val infinitesimal = new Array[Int](DIMENSION)
    var d = 0
    while d < DIMENSION do
      infinitesimal(d) = xDerivative(i, d)
      d += 1
    new Jet[Int](xReal(i), infinitesimal)

  private def makeIntY(i: Int): Jet[Int] =
    val infinitesimal = new Array[Int](DIMENSION)
    var d = 0
    while d < DIMENSION do
      infinitesimal(d) = yDerivative(i, d)
      d += 1
    new Jet[Int](yReal(i), infinitesimal)

  private def makeLongX(i: Int): Jet[Long] =
    val infinitesimal = new Array[Long](DIMENSION)
    var d = 0
    while d < DIMENSION do
      infinitesimal(d) = xDerivative(i, d).toLong
      d += 1
    new Jet[Long](xReal(i).toLong, infinitesimal)

  private def makeLongY(i: Int): Jet[Long] =
    val infinitesimal = new Array[Long](DIMENSION)
    var d = 0
    while d < DIMENSION do
      infinitesimal(d) = yDerivative(i, d).toLong
      d += 1
    new Jet[Long](yReal(i).toLong, infinitesimal)

  private def makeDoubleX(i: Int): Jet[Double] =
    val infinitesimal = new Array[Double](DIMENSION)
    var d = 0
    while d < DIMENSION do
      infinitesimal(d) = xDerivative(i, d).toDouble
      d += 1
    new Jet[Double](xReal(i).toDouble, infinitesimal)

  private def makeDoubleY(i: Int): Jet[Double] =
    val infinitesimal = new Array[Double](DIMENSION)
    var d = 0
    while d < DIMENSION do
      infinitesimal(d) = yDerivative(i, d).toDouble
      d += 1
    new Jet[Double](yReal(i).toDouble, infinitesimal)

  private val intXs: Array[Jet[Int]] = Array.tabulate(N) { i => makeIntX(i) }
  private val intYs: Array[Jet[Int]] = Array.tabulate(N) { i => makeIntY(i) }

  private val longXs: Array[Jet[Long]] = Array.tabulate(N) { i => makeLongX(i) }
  private val longYs: Array[Jet[Long]] = Array.tabulate(N) { i => makeLongY(i) }

  private val doubleXs: Array[Jet[Double]] = Array.tabulate(N) { i => makeDoubleX(i) }
  private val doubleYs: Array[Jet[Double]] = Array.tabulate(N) { i => makeDoubleY(i) }

  private val expectedPerRepeat: Long =
    var total = 0L
    var i = 0
    while i < N do
      val a = xReal(i).toLong
      val b = yReal(i).toLong
      val real = a * b
      total += real
      var d = 0
      while d < DIMENSION do
        val du = xDerivative(i, d).toLong
        val dv = yDerivative(i, d).toLong
        val derivative = b * du + a * dv
        total += derivative
        d += 1
      i += 1
    total

  private val expectedTotal = expectedPerRepeat * INNER_REPEAT

  def jetBenchmark[A](xs: Array[Jet[A]], ys: Array[Jet[A]])(using ring: Ring[A], classTag: scala.reflect.ClassTag[A]): Unit =
    var total = 0L
    var repeat = 0
    while repeat < INNER_REPEAT do
      var checksum: A = ring.zero
      var i = 0
      while i < N do
        val result: Jet[A] = xs(i) * ys(i)
        checksum = ring.plus(checksum, result.real)
        var d = 0
        while d < result.dimension do
          checksum = ring.plus(checksum, result.infinitesimal(d))
          d += 1
        i += 1
      total += ring.toLong(checksum)
      repeat += 1

    if total != expectedTotal then
      println(s"Error: total=$total, expectedTotal=$expectedTotal")

  @main def runSpireJetMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        jetBenchmark[Int](intXs, intYs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1

  @main def runSpireJetMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        jetBenchmark[Int](intXs, intYs)
        jetBenchmark[Long](longXs, longYs)
        jetBenchmark[Double](doubleXs, doubleYs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1