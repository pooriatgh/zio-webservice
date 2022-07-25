package dev.zio.quickstart

import dev.zio.quickstart.authentication.{
  AuthApp,
  AuthServiceImpl,
  SessionRepoInMemoryImpl
}
import dev.zio.quickstart.counter.CounterApp
import dev.zio.quickstart.download.DownloadApp
import dev.zio.quickstart.greet.GreetingApp
import dev.zio.quickstart.users.{InmemoryUserRepo, UserApp}
import zhttp.service.Server
import zio._

object MainApp extends ZIOAppDefault {
  def run =
    Server
      .start(
        port = 8081,
        // orders from left to the right if left can execute then will not pass to the right
        http =
          GreetingApp() ++ DownloadApp() ++ CounterApp() ++ UserApp() ++ AuthApp()
      )
      .provide(
        ZLayer.fromZIO(Ref.make(0)),
        SessionRepoInMemoryImpl.layer,
        AuthServiceImpl.layer,
        InmemoryUserRepo.layer
      )
}
