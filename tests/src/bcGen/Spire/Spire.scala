package bcGen.Spire

import scala.reflect.ClassTag

trait Ring[A]:
  def zero: A
  def plus(x: A, y: A): A
  def minus(x: A, y: A): A
  def times(x: A, y: A): A
  def toLong(x: A): Long

object Ring:
  given intRing: Ring[Int] with
    def zero: Int = 0
    def plus(x: Int, y: Int): Int = x + y
    def minus(x: Int, y: Int): Int = x - y
    def times(x: Int, y: Int): Int = x * y
    def toLong(x: Int): Long = x.toLong

  given longRing: Ring[Long] with
    def zero: Long = 0L
    def plus(x: Long, y: Long): Long = x + y
    def minus(x: Long, y: Long): Long = x - y
    def times(x: Long, y: Long): Long = x * y
    def toLong(x: Long): Long = x

  given doubleRing: Ring[Double] with
    def zero: Double = 0.0
    def plus(x: Double, y: Double): Double = x + y
    def minus(x: Double, y: Double): Double = x - y
    def times(x: Double, y: Double): Double = x * y
    def toLong(x: Double): Long = x.toLong

class Complex[A](val real: A, val imag: A):

  def +(rhs: Complex[A])(using ring: Ring[A]): Complex[A] =
    new Complex[A](ring.plus(real, rhs.real), ring.plus(imag, rhs.imag))

  def -(rhs: Complex[A])(using ring: Ring[A]): Complex[A] =
    new Complex[A](ring.minus(real, rhs.real), ring.minus(imag, rhs.imag))

  def *(rhs: Complex[A])(using ring: Ring[A]): Complex[A] =
    new Complex[A](ring.minus(ring.times(real, rhs.real), ring.times(imag, rhs.imag)),
                   ring.plus(ring.times(imag, rhs.real), ring.times(real, rhs.imag))
    )

class Quaternion[A](val r: A, val i: A, val j: A, val k: A):

  def +(rhs: Quaternion[A])(using ring: Ring[A]): Quaternion[A] =
    Quaternion[A](ring.plus(r, rhs.r), ring.plus(i, rhs.i), ring.plus(j, rhs.j), ring.plus(k, rhs.k))

  def -(rhs: Quaternion[A])(using ring: Ring[A]): Quaternion[A] =
    Quaternion[A](ring.minus(r, rhs.r), ring.minus(i, rhs.i), ring.minus(j, rhs.j), ring.minus(k, rhs.k))

  def *(rhs: Quaternion[A])(using ring: Ring[A]): Quaternion[A] =
    Quaternion[A](
      // r
      ring.minus(
        ring.minus(
          ring.minus(
            ring.times(r, rhs.r),
            ring.times(i, rhs.i)
          ),
          ring.times(j, rhs.j)
        ),
        ring.times(k, rhs.k)
      ),

      // i
      ring.minus(
        ring.plus(
          ring.plus(
            ring.times(r, rhs.i),
            ring.times(i, rhs.r)
          ),
          ring.times(j, rhs.k)
        ),
        ring.times(k, rhs.j)
      ),

      // j
      ring.plus(
        ring.plus(
          ring.minus(
            ring.times(r, rhs.j),
            ring.times(i, rhs.k)
          ),
          ring.times(j, rhs.r)
        ),
        ring.times(k, rhs.i)
      ),

      // k
      ring.plus(
        ring.minus(
          ring.plus(
            ring.times(r, rhs.k),
            ring.times(i, rhs.j)
          ),
          ring.times(j, rhs.i)
        ),
        ring.times(k, rhs.r)
      )
    )

class PolyDense[A](val coeffs: Array[A])(using val classTag: ClassTag[A]):

  val length: Int = coeffs.length

  def degree: Int = if length == 0 then 0 else length - 1

  def isZero: Boolean = length == 0

  def *(rhs: PolyDense[A])(using ring: Ring[A]): PolyDense[A] =
    if this.isZero || rhs.isZero then
      new PolyDense[A](new Array[A](0))
    else
      val lhsCoeffs = coeffs
      val rhsCoeffs = rhs.coeffs
      val lhsLength = length
      val rhsLength = rhs.length
      val result = new Array[A](lhsLength + rhsLength - 1)
      val resultLength = lhsLength + rhsLength - 1
      var i = 0
      while i < resultLength do
        result(i) = ring.zero
        i += 1
      i = 0
      while i < lhsLength do
        val c = lhsCoeffs(i)
        var j = 0
        var k = i
        while j < rhsLength do
          result(k) = ring.plus(result(k), ring.times(c, rhsCoeffs(j)))
          j += 1
          k += 1
        i += 1
      new PolyDense[A](result)

  def evaluate(x: A)(using ring: Ring[A]): A =
    if isZero then
      return ring.zero
    var even = length - 1
    var odd = length - 2
    if (even & 1) == 1 then
      even = odd
      odd = length - 1
    var c0 = coeffs(even)
    val x2 = ring.times(x, x)
    var index = even - 2
    while index >= 0 do
      c0 = ring.plus(coeffs(index), ring.times(c0, x2))
      index -= 2
    if odd >= 1 then
      var c1 = coeffs(odd)
      index = odd - 2
      while index >= 1 do
        c1 = ring.plus(coeffs(index), ring.times(c1, x2))
        index -= 2
      ring.plus(c0, ring.times(c1, x))
    else
      c0

class Jet[A](val real: A, val infinitesimal: Array[A]):

  val dimension: Int = infinitesimal.length

  def +(rhs: Jet[A])(using ring: Ring[A], classTag: ClassTag[A]): Jet[A] =
    val result = new Array[A](dimension)
    var i = 0
    while i < dimension do
      result(i) = ring.plus(infinitesimal(i), rhs.infinitesimal(i))
      i += 1
    new Jet[A](ring.plus(real, rhs.real), result)

  def *(rhs: Jet[A])(using ring: Ring[A], classTag: ClassTag[A]): Jet[A] =
    val result = new Array[A](dimension)
    var i = 0
    while i < dimension do
      result(i) = ring.plus(
          ring.times(rhs.real, infinitesimal(i)),
          ring.times(real, rhs.infinitesimal(i))
      )
      i += 1
    new Jet[A](ring.times(real, rhs.real), result)