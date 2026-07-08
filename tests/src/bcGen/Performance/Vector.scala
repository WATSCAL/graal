package bcGen.Performance

final class Vector[A] private (initialCapacity: Int):
  private var elements = new Array[A](math.max(1, initialCapacity))
  private var currentSize = 0

  def isEmpty: Boolean = currentSize == 0

  def nonEmpty: Boolean = currentSize != 0

  def size: Int = currentSize

  def pushBack(value: A): Unit =
    ensureCapacity(currentSize + 1)
    elements(currentSize) = value
    currentSize += 1
  
  def popBack(): Unit =
    currentSize -= 1

  def apply(index: Int): A =
    elements(index)

  def update(index: Int, value: A): Unit =
    elements(index) = value

  def clear(): Unit =
    currentSize = 0

  private def ensureCapacity(requiredCapacity: Int): Unit =
    if requiredCapacity > elements.length then
      var newCapacity = elements.length
      while newCapacity < requiredCapacity do
        newCapacity *= 2

      val resized = new Array[A](newCapacity)
      var index = 0
      while index < currentSize do
        resized(index) = elements(index)
        index += 1
      elements = resized

object Vector:
  def empty[A]: Vector[A] =
    new Vector[A](1)
