package dev.zio.quickstart.authentication

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zhttp.http.Middleware.bearerAuth
import zhttp.http._
import zhttp.service.Server
import zio._

import java.time.Clock
////https://github.com/dream11/zio-http/blob/main/example/src/main/scala/example/AuthenticationServer.scala

object JWTService extends ZIOAppDefault {

  /** This is an example to demonstrate barer Authentication middleware. The
    * Server has 2 routes. The first one is for login,Upon a successful login,
    * it will return a jwt token for accessing protected routes. The second
    * route is a protected route that is accessible only if the request has a
    * valid jwt token. AuthenticationClient example can be used to makes
    * requests to this server.
    */

  // Secret Authentication key
  val SECRET_KEY = "secretKey"

  implicit val clock: Clock = Clock.systemUTC

  // Helper to encode the JWT token
  def jwtEncode(username: String): String = {
    val json  = s"""{"user": "${username}"}"""
    val claim = JwtClaim { json }.issuedNow.expiresIn(300)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)
  }

  // Helper to decode the JWT token
  def jwtDecode(token: String): Option[JwtClaim] = {
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption
  }

  // Http app that is accessible only via a jwt token
  def secured: Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> !! / "user" / name / "greet" =>
      Response.text(s"Welcome to the ZIO party! ")
    case other =>
      println(other)
      Response.status(Status.NotFound)
  } @@ bearerAuth(jwtDecode(_).isDefined)

  def securedWithoutMiddleware: Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {
      case r @ Method.GET -> !! / "user" / name / "greet" =>
        r.bearerToken match {
          case Some(token) =>
            val decoded = jwtDecode(token)
            decoded match {
              case Some(claim) =>
                Response.text(s"Welcome to the ZIO party! ")
              case None =>
                Response.status(Status.Unauthorized)
            }
          case None =>
            Response.status(Status.Unauthorized)
        }
      case other =>
        println(other)
        Response.status(Status.NotFound)
    } @@ bearerAuth(jwtDecode(_).isDefined)

  // App that let's the user login
  // Login is successful only if the password is the reverse of the username
  def login: Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> !! / "login" / username / password =>
      if (password.reverse.hashCode == username.hashCode)
        Response.text(jwtEncode(username))
      else
        Response
          .text("Invalid username or password.")
          .setStatus(Status.Unauthorized)
  }

  def simplePublic: Http[Any, Throwable, Request, Response] =
    Http.collect[Request] { case Method.GET -> !! / "simple" =>
      Response.text("Welcome to the ZIO party!")
    }

  // Composing all the HttpApps together
  val app: Http[Any, Throwable, Request, Response] =
    login ++ simplePublic ++ secured

  // Run it like any simple app
  override val run = Server.start(8090, app)
}
