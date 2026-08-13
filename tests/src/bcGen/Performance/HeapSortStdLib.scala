package bcGen.Performance

import scala.collection.mutable

object HeapSortStdLib:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 4
  private final val N = 4096

  private val intValues = Array.range(0, N).map(i => ((i * 1103515245L + 12345L) & 0x7fffffffL).toInt)
  private val longValues: Array[Long] = Array.range(0, N).map(i => intValues(i).toLong)
  private val doubleValues: Array[Double] = Array.range(0, N).map(i => intValues(i).toDouble)

  private val intExpectedMax = intValues.reduceLeft((x, y) => if x > y then x else y)
  private val longExpectedMax = intExpectedMax.toLong
  private val doubleExpectedMax = intExpectedMax.toDouble

  def heapSortBenchmark[A](values: Array[A], expectedMax: A)(using ordering: Ordering[A]): Unit = 
    val queue = mutable.PriorityQueue.empty[A]
    var repeat = 0
    while repeat < INNER_REPEAT do
      var i = 0
      while i < N do
        queue.addOne(values(i))
        i += 1
      var max: A = queue.dequeue()
      if !ordering.equiv(max, expectedMax) then
        println(s"Error: max=$max, expectedMax=$expectedMax")
      repeat += 1
      // var total = 0L
      // var last = Int.MaxValue
      // while queue.nonEmpty do
      //   val current = queue.dequeue()
      //   if current > last then
      //     println(s"Error: heap order violated, current=$current last=$last")
      //     return
      //   total += current
      //   last = current
      // if total != expectedSum then
      //   println(s"$total != $expectedSum")
      // repeat += 1

  @main def runHeapSortStdLibMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        heapSortBenchmark[Int](intValues, intExpectedMax)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1

  @main def runHeapSortStdLibMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        heapSortBenchmark[Int](intValues, intExpectedMax)
        heapSortBenchmark[Long](longValues, longExpectedMax)
        heapSortBenchmark[Double](doubleValues, doubleExpectedMax)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")  
      t += 1