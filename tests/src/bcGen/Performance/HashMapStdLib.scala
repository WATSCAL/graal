package bcGen.Performance

import scala.collection.mutable

object HashMapStdLib:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 100
  private final val N = 1024
  private final val CAPACITY = 8192

  private val keys =
    val result = new Array[Int](N)
    var i = 0
    while i < N do
      result(i) = i * 12345 + 123
      i += 1
    result

  private val expectedSum =
    var result = 0
    var i = 0
    while i < N do
      result += i * 3 + 7
      i += 1
    result

  private def benchmark(): Unit =
    var repeat = 0
    val map = new mutable.HashMap[Int, Int](CAPACITY, 0.75)
    while repeat < INNER_REPEAT do
      var i = 0
      while i < N do
        map.update(keys(i), i * 3 + 7)
        i += 1

      var total = 0
      i = N - 1
      while i >= 0 do
        total += map.getOrElse(keys(i), -1)
        i -= 1

      if total != expectedSum then
        println(s"Error: total=$total, expectedSum=$expectedSum")
      repeat += 1

  @main def runHashMapStdLib(): Unit =
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