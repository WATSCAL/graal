package bcGen.Classes

class SimpleClasses {

    class Pair[A, B](var first: A, val second: B){
      def getFirst: A = first
      def setFirst(newFirst: A): Unit = {
        first = newFirst
      }
      def id[T](x: T)[U]: T = x
    }

    class IntDoublePair(var first0: Int, override val second: Double) extends Pair[Int, Double](first0, second)

    class GenericPair[A](var first0: A, override val second: A) extends Pair[A, A](first0, second)

    class GenericPair2[A](var first1: A, override val second: A) extends GenericPair[A](first1, second)

}

object SimpleClassesTest {

  val simpleClasses = new SimpleClasses

  val pair1 = new simpleClasses.Pair[Int, String](42, "hello")
  val pair2 = new simpleClasses.IntDoublePair(10, 3.14) //need locals for this?
  val pair3 = new simpleClasses.GenericPair[Double](3.14, 2.71)
  val pair4 = new simpleClasses.GenericPair[String]("foo", "bar")
  val pair5 = new simpleClasses.GenericPair2[Boolean](true, false)
  val testid = pair1.id[Int](42)[String]

  @main def runSimpleClasses(): Unit = {
    println(s"Pair1: (${pair1.first}, ${pair1.second})")

    println(s"Pair2: (${pair2.first0}, ${pair2.second})")

    println(s"Pair3: (${pair3.first0}, ${pair3.second})")

    println(s"Pair4: (${pair4.first0}, ${pair4.second})")

    println(s"Pair5: (${pair5.first1}, ${pair5.second})")
    pair1.first = pair2.first0 + 10
    println(s"Updated Pair1: (${pair1.first}, ${pair1.second})")
    pair2.first0 = pair1.first
    println(s"Updated Pair2: (${pair2.first0}, ${pair2.second})")
    pair3.first0 = pair2.second
    println(s"Updated Pair3: (${pair3.first0}, ${pair3.second})")
    pair4.first0 = pair5.second.toString()
    println(s"Updated Pair4: (${pair4.first0}, ${pair4.second})")
    pair5.first1 = pair4.second == "bar"
    println(s"Updated Pair5: (${pair5.first1}, ${pair5.second})")
  }
}