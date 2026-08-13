package bcGen.Performance

import scala.collection.mutable

object SlidingWindowStdLib:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 100
  private final val N = 4096
  private final val WINDOW_SIZE = 128

  trait WindowOps[A]:
    def lessThan(x: A, y: A): Boolean
    def same(x: A, y: A): Boolean
    def toLong(x: A): Long
  
  object IntOps extends WindowOps[Int]:
    def lessThan(x: Int, y: Int): Boolean = x < y
    def same(x: Int, y: Int): Boolean = x == y
    def toLong(x: Int): Long = x.toLong

  object LongOps extends WindowOps[Long]:
    def lessThan(x: Long, y: Long): Boolean = x < y
    def same(x: Long, y: Long): Boolean = x == y
    def toLong(x: Long): Long = x

  object DoubleOps extends WindowOps[Double]:
    def lessThan(x: Double, y: Double): Boolean = x < y
    def same(x: Double, y: Double): Boolean = x == y
    def toLong(x: Double): Long = x.toLong
    
  private val intValues: Array[Int] = Array.range(0, N).map(i => ((i * 1103515245L + 12345L) & 0x7fffffffL).toInt)
  private val longValues: Array[Long] = Array.range(0, N).map(i => intValues(i).toLong)
  private val doubleValues: Array[Double] = Array.range(0, N).map(i => intValues(i).toDouble)

  private val expectedSum: Long =
    var result = 0L
    var windowStart = 0
    while windowStart + WINDOW_SIZE <= N do
      var maximum = Int.MinValue
      var i = windowStart
      while i < windowStart + WINDOW_SIZE do
        if intValues(i) > maximum then maximum = intValues(i)
        i += 1
      result += maximum
      windowStart += 1
    result

  def slidingWindowBenchmark[A](values: Array[A], ops: WindowOps[A]): Unit =
    var repeat = 0
    val queue = mutable.ArrayDeque.empty[A]
    while repeat < INNER_REPEAT do
      queue.clear()
      var total = 0L
      var i = 0
      while i < N do
        val current = values(i)
        while queue.nonEmpty && ops.lessThan(queue.last, current) do
          queue.removeLast()
        queue.addOne(current)

        if i >= WINDOW_SIZE then
          val outgoing = values(i - WINDOW_SIZE)
          if queue.nonEmpty && queue.head == outgoing then
            queue.removeHead()

        if i >= WINDOW_SIZE - 1 then
          total += ops.toLong(queue.head)
        i += 1

      if total != expectedSum then
        println(s"Error: total=$total, expectedSum=$expectedSum")
      repeat += 1

  @main def runSlidingWindowStdLibMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        slidingWindowBenchmark[Int](intValues, IntOps)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1

  @main def runSlidingWindowStdLibMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        slidingWindowBenchmark[Int](intValues, IntOps)
        slidingWindowBenchmark[Long](longValues, LongOps)
        slidingWindowBenchmark[Double](doubleValues, DoubleOps)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1