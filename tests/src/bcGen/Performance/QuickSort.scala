package bcGen.Performance

object QuickSort:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val N = 8192

  private val values =
    val result = new Array[Int](N)
    var i = 0
    while i < N do
      result(i) = ((i * 1103515245L + 12345L) & 0x7fffffffL).toInt
      i += 1
    result

  private val expectedSum =
    var result = 0L
    var i = 0
    while i < N do
      result += values(i)
      i += 1
    result
  
  private def recursiveQuickSort[T](using ordering: TotalOrdering[T])(arr: Array[T], l: Int, r: Int): Unit =
    if l < r then
      val pivot = arr(l + (r - l) / 2)
      var i = l
      var j = r

      while i <= j do
        while ordering.lt(arr(i), pivot) do
          i += 1
        while ordering.lt(pivot, arr(j)) do
          j -= 1
        if i <= j then
          val value = arr(i)
          arr(i) = arr(j)
          arr(j) = value
          i += 1
          j -= 1

      if l < j then
        recursiveQuickSort(arr, l, j)
      if i < r then
        recursiveQuickSort(arr, i, r)
  
  private def quickSort[T: TotalOrdering](arr: Array[T]): Array[T] =
    val len = arr.size
    val result = new Array[T](len)
    var i = 0
    while i < len do
      result(i) = arr(i)
      i += 1
    recursiveQuickSort(result, 0, len - 1)
    result

  private def benchmark(): Unit =
    val sorted = quickSort(values)(using new IntOrdering)

    var total = 0L
    var last = Int.MinValue
    var i = 0
    while i < N do
      val current = sorted(i)
      if current < last then
        println(s"Wrong order: current=$current last=$last")
        return
      total += current
      last = current
      i += 1
    if total != expectedSum then
      println(s"$total != $expectedSum")

  @main def runQuickSort(): Unit =
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
