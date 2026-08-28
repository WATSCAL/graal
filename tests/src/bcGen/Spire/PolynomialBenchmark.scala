package bcGen.Spire

object PolynomialBenchmark:
  private final val T = 100
  private final val OUTER_REPEAT = 100
  private final val INNER_REPEAT = 50
  private final val COEFF_COUNT = 64

  private def lhsCoeff(i: Int): Int = (i % 7) - 3
  private def rhsCoeff(i: Int): Int = (i % 5) - 2

  private val intLhs = new PolyDense[Int](Array.tabulate(COEFF_COUNT) { i => lhsCoeff(i) })
  private val intRhs = new PolyDense[Int](Array.tabulate(COEFF_COUNT) { i => rhsCoeff(i) })

  private val longLhs = new PolyDense[Long](Array.tabulate(COEFF_COUNT) { i => lhsCoeff(i).toLong })
  private val longRhs = new PolyDense[Long](Array.tabulate(COEFF_COUNT) { i => rhsCoeff(i).toLong })

  private val doubleLhs = new PolyDense[Double](Array.tabulate(COEFF_COUNT) { i => lhsCoeff(i).toDouble })
  private val doubleRhs = new PolyDense[Double](Array.tabulate(COEFF_COUNT) { i => rhsCoeff(i).toDouble })

  private val expectedPerRepeat: Long =
    val result = new Array[Long](COEFF_COUNT + COEFF_COUNT - 1)
    var i = 0
    while i < COEFF_COUNT do
      val lhs = lhsCoeff(i).toLong
      var j = 0
      while j < COEFF_COUNT do
        result(i + j) += lhs * rhsCoeff(j).toLong
        j += 1
      i += 1
    var total = 0L
    i = 0
    while i < COEFF_COUNT + COEFF_COUNT - 1 do
      total += result(i)
      i += 1
    total

  private val expectedTotal = expectedPerRepeat * INNER_REPEAT

  def polynomialBenchmark[A](lhs: PolyDense[A], rhs: PolyDense[A])(using ring: Ring[A]): Unit =
    var total = 0L
    var repeat = 0
    while repeat < INNER_REPEAT do
      val result: PolyDense[A] = lhs * rhs
      var checksum: A = ring.zero
      var i = 0
      while i < result.length do
        checksum = ring.plus(checksum, result.coeffs(i))
        i += 1
      total += ring.toLong(checksum)
      repeat += 1
    if total != expectedTotal then
      println(s"Error: total=$total, expectedTotal=$expectedTotal")

  @main def runSpirePolynomialMono(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        polynomialBenchmark[Int](intLhs, intRhs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1

  @main def runSpirePolynomialMega(): Unit =
    var t = 1
    while t <= T do
      val startTime = System.nanoTime()
      var r = 0
      while r < OUTER_REPEAT do
        polynomialBenchmark[Int](intLhs, intRhs)
        polynomialBenchmark[Long](longLhs, longRhs)
        polynomialBenchmark[Double](doubleLhs, doubleRhs)
        r += 1
      val duration = System.nanoTime() - startTime
      println(s"round $t: ${duration / 1000}us")
      t += 1