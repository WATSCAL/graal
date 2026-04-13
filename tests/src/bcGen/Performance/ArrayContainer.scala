package bcGen.Performance

class ArrayContainer[T](val storage: Array[T]) {
  def reduce(fn: BinaryOp[T]): T = {
    var state = storage(0)
    var i = 1
    while (i < storage.length) {
      state = fn.apply(state, storage(i))
      i += 1
    }
    state
  }
}
