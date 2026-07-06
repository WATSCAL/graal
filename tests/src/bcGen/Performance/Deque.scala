package bcGen.Performance

class Deque[A](initialCapacity: Int):
  private var elements = new Array[A](math.max(1, initialCapacity))
  private var first = 0
  private var currentSize = 0

  def isEmpty: Boolean = currentSize == 0

  def nonEmpty: Boolean = currentSize != 0

  def size: Int = currentSize

  def pushFront(value: A): Unit =
    ensureCapacity(currentSize + 1)
    first = wrap(first - 1)
    elements(first) = value
    currentSize += 1

  def pushBack(value: A): Unit =
    ensureCapacity(currentSize + 1)
    elements(physicalIndex(currentSize)) = value
    currentSize += 1

  def front: A =
    elements(first)

  def back: A =
    elements(physicalIndex(currentSize - 1))

  def popFront(): A =
    val result = elements(first)
    first = wrap(first + 1)
    currentSize -= 1
    if currentSize == 0 then first = 0
    result

  def popBack(): A =
    val index = physicalIndex(currentSize - 1)
    val result = elements(index)
    currentSize -= 1
    if currentSize == 0 then first = 0
    result

  def apply(index: Int): A =
    elements(physicalIndex(index))

  def update(index: Int, value: A): Unit =
    elements(physicalIndex(index)) = value

  def clear(): Unit =
    first = 0
    currentSize = 0

  private def physicalIndex(logicalIndex: Int): Int =
    wrap(first + logicalIndex)

  private def wrap(index: Int): Int =
    val capacity = elements.length
    if index >= capacity then index - capacity
    else if index < 0 then index + capacity
    else index

  private def ensureCapacity(requiredCapacity: Int): Unit =
    if requiredCapacity > elements.length then
      var newCapacity = elements.length
      while newCapacity < requiredCapacity do
        newCapacity *= 2

      val resized = new Array[A](newCapacity)
      var index = 0
      while index < currentSize do
        resized(index) = elements(physicalIndex(index))
        index += 1
      elements = resized
      first = 0
