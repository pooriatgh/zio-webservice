package dev.zio.quickstart.authentication
import zio.json._

case class Login(email: String, password: String)
object Login {
  implicit val decoder: JsonDecoder[Login] = DeriveJsonDecoder.gen[Login]
  implicit val encoder: JsonEncoder[Login] = DeriveJsonEncoder.gen[Login]
}
