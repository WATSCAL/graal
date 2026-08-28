package bcGen.Spire

object RingBenchmark:
  private final val T = 100
  private final val OUTER_REPEAT = 20
  private final val INNER_REPEAT = 100000
  private final val N = 64

  val intXs: Array[Int] = Array.tabulate(N) { i => (i % 11) - 5 }
  val intYs: Array[Int] = Array.tabulate(N) { i => (i % 7) - 3 }
  val longXs: Array[Long] = Array.tabulate(N) { i => ((i % 11) - 5).toLong }
  val longYs: Array[Long] = Array.tabulate(N) { i => ((i % 7) - 3).toLong }
  val doubleXs: Array[Double] = Array.tabulate(N) { i => ((i % 11) - 5).toDouble }
  val doubleYs: Array[Double] = Array.tabulate(N) { i => ((i % 7) - 3).toDouble }

  val expectedTotal: Long =
    var total = 0L
    var i = 0
    while i < N do
      total += intXs(i).toLong + intYs(i).toLong
      i += 1
    total * INNER_REPEAT

  def benchmark[A](xs: Array[A], ys: Array[A])(using ring: Ring[A]): Unit =
    var total = 0L
    var i = 0
    var repeat = 0
    while repeat < INNER_REPEAT do
      i = 0
      while i < N do
        val result: A = ring.plus(xs(i), ys(i))
        total += ring.toLong(result)
        i += 1
      repeat += 1
    if total != expectedTotal then
      println(s"Error: total=$total, expectedTotal=$expectedTotal")

  @main def runRingMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        benchmark[Int](intXs, intYs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1
  
  @main def runRingMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        benchmark[Int](intXs, intYs)
        benchmark[Long](longXs, longYs)
        benchmark[Double](doubleXs, doubleYs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1