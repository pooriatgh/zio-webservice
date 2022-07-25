package dev.zio.quickstart.authentication
import zhttp.http._
import zio.ZIO
import zio.json._

object AuthApp {
  def apply(): Http[AuthService, Throwable, Request, Response] =
    Http.collectZIO[Request] {

      case req @ Method.POST -> !! / "login" =>
        for {
          u <- req.bodyAsString.map(_.fromJson[Login])
          d <- u match {
            case Right(login) =>
              AuthService.authenticate(login).map {
                case Some(token) => Response.text(token)
                case None =>
                  Response.status(Status.Accepted)
              }
            case Left(e) => ZIO.fail(throw new Exception(e))
          }
        } yield d

      case Method.GET -> !! / "logout" =>
        ZIO.succeed(Response.text("Logout"))
    }
}
