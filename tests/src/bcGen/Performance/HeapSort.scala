package bcGen.Performance

object HeapSort:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 4
  private final val N = 4096

  private val values = Array.tabulate(N) { i =>
    ((i * 1103515245L + 12345L) & 0x7fffffffL).toInt
  }
  private val expectedSum = values.foldLeft(0L)(_ + _)

  private def benchmark(): Unit =
    val queue = new PriorityQueue[Int]()
    var repeat = 0
    while repeat < INNER_REPEAT do
      var i = 0
      while i < N do
        queue.enqueue(values(i))
        i += 1

      var total = 0L
      var last = Int.MaxValue
      while queue.nonEmpty do
        val current = queue.dequeue()
        if current > last then
          println(s"Error: heap order violated, current=$current last=$last")
          return
        total += current
        last = current
      if total != expectedSum then
        println(s"$total != $expectedSum")
      repeat += 1

  @main def runHeapSort(): Unit =
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
