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

  /* Offset: 26, ClassCount: 1
    Class 0: LocalVariableCount: 2, Indices: 0 1

    bcGen.Classes.SimpleClassesTest.pair1 =
    {
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$A: Byte = 73
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$B: Byte = 76
      new SimpleClasses.this.SimpleClasses$Pair(
        bcGen.Classes.SimpleClassesTest.simpleClasses(),
        InvokeReturnType(Byte)
          reifiedLocal$bcGen$Classes$SimpleClasses$Pair$A,
        InvokeReturnType(Byte)
          reifiedLocal$bcGen$Classes$SimpleClasses$Pair$B,
        NoBoxingNeeded Int.box(42), "hello")
    }
   */
  val pair1 = new simpleClasses.Pair[Int, String](42, "hello")
  /* Offset: 57, ClassCount: 2
    Class 0: LocalVariableCount: 2, Indices: 2 3 
    Class 1: LocalVariableCount: 0, Indices: 
    
    bcGen.Classes.SimpleClassesTest.pair2 =
    {
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$A: Byte = 73
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$B: Byte = 68
      new SimpleClasses.this.SimpleClasses$IntDoublePair(
        bcGen.Classes.SimpleClassesTest.simpleClasses(), 10, 3.14d)
    }
   */
  val pair2 = new simpleClasses.IntDoublePair(10, 3.14)
  /* Offset: 90, ClassCount: 2
    Class 0: LocalVariableCount: 2, Indices: 5 6 
    Class 1: LocalVariableCount: 1, Indices: 4

    bcGen.Classes.SimpleClassesTest.pair3 =
    {
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$GenericPair$A: Byte = 68
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$A: Byte = 68
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$B: Byte = 68
      new SimpleClasses.this.SimpleClasses$GenericPair(
        bcGen.Classes.SimpleClassesTest.simpleClasses(),
        InvokeReturnType(Byte)
          reifiedLocal$bcGen$Classes$SimpleClasses$GenericPair$A,
        NoBoxingNeeded Double.box(3.14d), NoBoxingNeeded Double.box(2.71d)
        )
    }
   */
  val pair3 = new simpleClasses.GenericPair[Double](3.14, 2.71)
  /* Offset: 132, ClassCount: 2
    Class 0: LocalVariableCount: 2, Indices: 8 9 
    Class 1: LocalVariableCount: 1, Indices: 7

    bcGen.Classes.SimpleClassesTest.pair4 =
    {
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$GenericPair$A: Byte = 76
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$A: Byte = 76
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$B: Byte = 76
      new SimpleClasses.this.SimpleClasses$GenericPair(
        bcGen.Classes.SimpleClassesTest.simpleClasses(),
        InvokeReturnType(Byte)
          reifiedLocal$bcGen$Classes$SimpleClasses$GenericPair$A,
      "foo", "bar")
    }
   */
  val pair4 = new simpleClasses.GenericPair[String]("foo", "bar")
  /* Offset: 170, ClassCount: 3
    Class 0: LocalVariableCount: 2, Indices: 12 13 
    Class 1: LocalVariableCount: 1, Indices: 11 
    Class 2: LocalVariableCount: 1, Indices: 10

    bcGen.Classes.SimpleClassesTest.pair5 =
    {
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$GenericPair2$A: Byte = 90
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$GenericPair$A: Byte = 90
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$A: Byte = 90
      tracked/<reified-local> val 
        reifiedLocal$bcGen$Classes$SimpleClasses$Pair$B: Byte = 90
      new SimpleClasses.this.SimpleClasses$GenericPair2(
        bcGen.Classes.SimpleClassesTest.simpleClasses(),
        InvokeReturnType(Byte)
          reifiedLocal$bcGen$Classes$SimpleClasses$GenericPair2$A,
        NoBoxingNeeded Boolean.box(true),
        NoBoxingNeeded Boolean.box(false))
    }
   */
  val pair5 = new simpleClasses.GenericPair2[Boolean](true, false)
  val testid = pair1.id[Int](42)[String]

  @main def runSimpleClasses(): Unit = {
    val a = pair1.first
    val b = pair1.second
    println(s"Pair1: (${a}, ${b})")
    val c = pair2.first0
    val d = pair2.second
    println(s"Pair2: (${c}, ${d})")
    val e = pair3.first0
    val f = pair3.second
    println(s"Pair3: (${e}, ${f})")
    val g = pair4.first0
    val h = pair4.second
    println(s"Pair4: (${g}, ${h})")
    val i = pair5.first1
    val j = pair5.second
    println(s"Pair5: (${i}, ${j})")
    pair1.first = pair2.first0 + 10
    val k = pair1.first
    val l = pair1.second
    println(s"Updated Pair1: (${k}, ${l})")
    pair2.first0 = pair1.first
    val m = pair2.first0
    val n = pair2.second
    println(s"Updated Pair2: (${m}, ${n})")
    pair3.first0 = pair2.second
    val o = pair3.first0
    val p = pair3.second
    println(s"Updated Pair3: (${o}, ${p})")
    pair4.first0 = pair5.second.toString()
    val q = pair4.first0
    val r = pair4.second
    println(s"Updated Pair4: (${q}, ${r})")
    pair5.first1 = pair4.second == "bar"
    val s = pair5.first1
    val t = pair5.second
    println(s"Updated Pair5: (${s}, ${t})")
  }
}