package bcGen.Performance

object ArraySumStdLib:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 1000
  private final val N = 1000

  val intArray: Array[Int] = Array.range(0, N)
  val longArray: Array[Long] = Array.range(0, N).map(_.toLong)
  val doubleArray: Array[Double] = Array.range(0, N).map(_.toDouble)
  val expectedSum: Int = intArray.sum

  private val intAdd: (Int, Int) => Int =
    (x, y) => x + y

  private val longAdd: (Long, Long) => Long =
    (x, y) => x + y

  private val doubleAdd: (Double, Double) => Double =
    (x, y) => x + y

  def reduceBenchmark[A](storage: Array[A], add: (A, A) => A, expected: A): Unit =
    var s: A = storage(0)
    var i = 0
    while i < INNER_REPEAT do
      s = storage.reduce(add)
      i += 1
    if s != expected then
      println(s"Error: s=$s")

  @main def runArraySumStdLibMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        reduceBenchmark[Int](intArray, intAdd, expectedSum)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1
  
  @main def runArraySumStdLibMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        reduceBenchmark[Int](intArray, intAdd, expectedSum)
        reduceBenchmark[Long](longArray, longAdd, expectedSum.toLong)
        reduceBenchmark[Double](doubleArray, doubleAdd, expectedSum.toDouble)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1