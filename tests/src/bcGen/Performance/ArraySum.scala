package bcGen.Performance

object ArraySum {
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 1000
  private final val N = 1000

  private def benchmark(): Unit = {
    val storage = Array.range(0, N)
    val arr = new ArrayContainer[Int](storage)
    val adder = new IntAddition
    var s = 0
    var i = 0
    while (i < INNER_REPEAT) {
      s += arr.reduce(adder)
      i += 1
    }
    if (s != 499500000) {
      println(s"Error: s=$s")
    }
  }

  @main def runArraySum(): Unit = {
    var t = 1
    while (t <= T) {
      val startTime = System.nanoTime()
      var r = 0
      while (r < OUTER_REPEAT) {
        benchmark()
        r += 1
      }
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1
    }
  }
}
