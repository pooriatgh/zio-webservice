package dev.zio.quickstart.authentication

import zio.{Ref, Task, ZLayer}

import scala.collection.mutable

trait SessionRepo[A] {
  def get(key: A): Task[Option[A]]
  def set(key: A): Task[Option[A]]
  def delete(key: A): Task[Unit]
}

case class SessionRepoInMemoryImpl(map: Ref[mutable.Map[String, String]])
    extends SessionRepo[String] {

  override def get(key: String): Task[Option[String]] = map.get.map(_.get(key))

  override def set(key: String): Task[Option[String]] =
    map.updateAndGet(_ addOne (key, "value")).map(_.get(key))

  override def delete(key: String): Task[Unit] = map.update { p =>
    p.remove(key)
    p
  }
}

object SessionRepoInMemoryImpl {
  def layer: ZLayer[Any, Nothing, SessionRepo[String]] = ZLayer {
    Ref
      .make(mutable.Map.empty[String, String])
      .map(new SessionRepoInMemoryImpl(_))
  }
}
