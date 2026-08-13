package bcGen.Performance

import scala.collection.mutable

object HashMapStdLib:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 100
  private final val N = 1024
  private final val CAPACITY = 8192

  private val intKeys = Array.range(0, N).map(i => i * 12345 + 123)
  private val longKeys = Array.range(0, N).map(i => i * 12345L + 123L)
  private val doubleKeys = Array.range(0, N).map(i => i * 12345.0 + 123.0)

  private val intValues = Array.range(0, N).map(i => i * 3 + 7)
  private val longValues = Array.range(0, N).map(i => i * 3L + 7L)
  private val doubleValues = Array.range(0, N).map(i => i * 3.0 + 7.0)

  private val expectedSum = Array.range(0, N).map(i => i * 3 + 7).sum

  def hashMapBenchmark[K, V](keys: Array[K], values: Array[V], default: V): Unit =
    var repeat = 0
    val map = new mutable.HashMap[K, V](CAPACITY, 0.75)
    while repeat < INNER_REPEAT do
      var i = 0
      while i < N do
        map.update(keys(i), values(i))
        i += 1

      i = N - 1
      while i >= 0 do
        // val actual = map.getOrElse(keys(i), default)
        val actual = map.get(keys(i)) match {
          case Some(v) => v
          case None => default
        }
        // this works
        val expected: V = values(i)
        if actual != expected then
          println(s"Error: actual=$actual, expected=${values(i)}")
        // no reifiedAsInstanceOf around values(i)
        // values(i) is already Any
        // if actual != values(i) then
        //   println(s"Error: actual=$actual, expected=${values(i)}")
        i -= 1
      repeat += 1

  @main def runHashMapStdLibMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        hashMapBenchmark[Int, Int](intKeys, intValues, -1)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1

  @main def runHashMapStdLibMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        hashMapBenchmark[Int, Int](intKeys, intValues, -1)
        hashMapBenchmark[Long, Long](longKeys, longValues, -1L)
        hashMapBenchmark[Double, Double](doubleKeys, doubleValues, -1.0)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1