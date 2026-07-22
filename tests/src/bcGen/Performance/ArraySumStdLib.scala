package bcGen.Performance

object ArraySumStdLib:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 1000
  private final val N = 1000

  private def benchmark(): Unit =
    val storage = Array.range(0, N)
    var s = 0
    var i = 0
    while i < INNER_REPEAT do
      s += storage.reduce(_ + _)
      i += 1
    if s != 499500000 then
      println(s"Error: s=$s")

  @main def runArraySumStdLib(): Unit =
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