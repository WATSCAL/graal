package bcGen.Performance

/*
 * Non-expandable, modulo hashing, linear probing
 * Maps positive integers to T.
 */
final class HashMap[T](val capacity: Int):

  private val keys = new Array[Int](capacity)
  private val values = new Array[T](capacity)

  def get(key: Int, defaultValue: T): T =
    val index = findSlot(key)
    if keys(index) == key then values(index) else defaultValue

  def set(key: Int, value: T): Unit =
    val index = findSlot(key)
    keys(index) = key
    values(index) = value

  private def findSlot(key: Int): Int =
    var index = key % capacity
    while keys(index) > 0 && keys(index) != key do
      index += 1
      if index == capacity then index = 0
    index

object HashMap:
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
    val map = new HashMap[Int](CAPACITY)
    while repeat < INNER_REPEAT do
      var i = 0
      while i < N do
        map.set(keys(i), i * 3 + 7)
        i += 1

      var total = 0
      i = N - 1
      while i >= 0 do
        total += map.get(keys(i), -1)
        i -= 1

      if total != expectedSum then
        println(s"Error: total=$total, expectedSum=$expectedSum")
      repeat += 1

  @main def runHashMap(): Unit =
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
