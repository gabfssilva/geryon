package org.geryon.scaladsl

trait MatrixParameterSupport {
  def matrixParameter(tuple: (String, Product))(implicit request: ScalaDslRequest): Product = {
    val (path, (keys)) = tuple

    val result = request
      .matrixParameters
      .get(path)
      .map { params =>
        keys match {
          case Tuple1(key: String) =>
            Tuple1(params(key))
          case Tuple2(key1: String, key2: String) =>
            Tuple2(params(key1), params(key2))
          case Tuple3(key1: String, key2: String, key3: String) =>
            Tuple3(params(key1), params(key2), params(key3))
          case Tuple4(key1: String, key2: String, key3: String, key4: String) =>
            Tuple4(params(key1), params(key2), params(key3), params(key4))
          case Tuple5(key1: String, key2: String, key3: String, key4: String, key5: String) =>
            Tuple5(params(key1), params(key2), params(key3), params(key4), params(key5))
          case Tuple6(key1: String, key2: String, key3: String, key4: String, key5: String, key6: String) =>
            Tuple6(params(key1), params(key2), params(key3), params(key4), params(key5), params(key6))
          case Tuple7(key1: String, key2: String, key3: String, key4: String, key5: String, key6: String, key7: String) =>
            Tuple7(params(key1), params(key2), params(key3), params(key4), params(key5), params(key6), params(key7))
          case Tuple8(key1: String, key2: String, key3: String, key4: String, key5: String, key6: String, key7: String, key8: String) =>
            Tuple8(params(key1), params(key2), params(key3), params(key4), params(key5), params(key6), params(key7), param(key8))
          case Tuple9(key1: String, key2: String, key3: String, key4: String, key5: String, key6: String, key7: String, key8: String, key9: String) =>
            Tuple9(params(key1), params(key2), params(key3), params(key4), params(key5), params(key6), params(key7), param(key8), param(key9))
          case Tuple10(key1: String, key2: String, key3: String, key4: String, key5: String, key6: String, key7: String, key8: String, key9: String, key10: String) =>
            Tuple10(params(key1), params(key2), params(key3), params(key4), params(key5), params(key6), params(key7), param(key8), param(key9), param(key10))
        }
      }
      .orNull

    result
  }

  implicit def asTuple2(product: Product): (String, String) = product.asInstanceOf[(String, String)]
  implicit def asTuple3(product: Product): (String, String, String) = product.asInstanceOf[(String, String, String)]
  implicit def asTuple4(product: Product): (String, String, String, String) = product.asInstanceOf[(String, String, String, String)]
  implicit def asTuple5(product: Product): (String, String, String, String, String) = product.asInstanceOf[(String, String, String, String, String)]
  implicit def asTuple6(product: Product): (String, String, String, String, String, String) = product.asInstanceOf[(String, String, String, String, String, String)]
  implicit def asTuple7(product: Product): (String, String, String, String, String, String, String) = product.asInstanceOf[(String, String, String, String, String, String, String)]
  implicit def asTuple8(product: Product): (String, String, String, String, String, String, String, String) = product.asInstanceOf[(String, String, String, String, String, String, String, String)]
  implicit def asTuple9(product: Product): (String, String, String, String, String, String, String, String, String) = product.asInstanceOf[(String, String, String, String, String, String, String, String, String)]
  implicit def asTuple10(product: Product): (String, String, String, String, String, String, String, String, String, String) = product.asInstanceOf[(String, String, String, String, String, String, String, String, String, String)]
}