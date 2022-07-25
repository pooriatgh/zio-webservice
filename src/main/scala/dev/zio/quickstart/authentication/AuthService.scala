package dev.zio.quickstart.authentication

import dev.zio.quickstart.users.UserRepo
import zio.{Task, ZIO, ZLayer}

trait AuthService {
  def authenticate(login: Login): Task[Option[String]]
  def logout(token: String): Task[Unit]
}

object AuthService {
  def authenticate(login: Login): ZIO[AuthService, Throwable, Option[String]] =
    ZIO.serviceWithZIO[AuthService](_.authenticate(login))
}

case class AuthServiceImpl(sessionRepo: SessionRepo[String], userRepo: UserRepo)
    extends AuthService {
  override def authenticate(login: Login): Task[Option[String]] = {
    for {
      u <- userRepo.users
      m <- u.find(_.name == login.email) match {
        case Some(user) => ZIO.succeed(user)
        case None => ZIO.fail(throw new Exception("User not found"))
      }
    } yield Some(m.name)
  }

  override def logout(token: String): Task[Unit] = sessionRepo.delete(token)
}

object AuthServiceImpl {
  def layer
      : ZLayer[SessionRepo[String] with UserRepo, Nothing, AuthServiceImpl] =
    ZLayer {
      for {
        ur <- ZIO.service[UserRepo]
        sr <- ZIO.service[SessionRepo[String]]
      } yield new AuthServiceImpl(sr, ur)
    }
}
