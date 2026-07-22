package bcGen.Performance

import scala.collection.mutable

object SlidingWindowStdLib:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 100
  private final val N = 4096
  private final val WINDOW_SIZE = 128

  private val values =
    val result = new Array[Int](N)
    var i = 0
    while i < N do
      result(i) = ((i * 1103515245L + 12345L) & 0x7fffffffL).toInt
      i += 1
    result

  private val expectedSum =
    var result = 0L
    var windowStart = 0
    while windowStart + WINDOW_SIZE <= N do
      var maximum = Int.MinValue
      var i = windowStart
      while i < windowStart + WINDOW_SIZE do
        if values(i) > maximum then maximum = values(i)
        i += 1
      result += maximum
      windowStart += 1
    result

  private def benchmark(): Unit =
    var repeat = 0
    val queue = mutable.ArrayDeque.empty[Int]
    while repeat < INNER_REPEAT do
      queue.clear()
      var total = 0L
      var i = 0
      while i < N do
        val current = values(i)
        while queue.nonEmpty && queue.last < current do
          queue.removeLast()
        queue.addOne(current)

        if i >= WINDOW_SIZE then
          val outgoing = values(i - WINDOW_SIZE)
          if queue.nonEmpty && queue.head == outgoing then
            queue.removeHead()

        if i >= WINDOW_SIZE - 1 then
          total += queue.head
        i += 1

      if total != expectedSum then
        println(s"Error: total=$total, expectedSum=$expectedSum")
      repeat += 1

  @main def runSlidingWindowStdLib(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        benchmark()
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1