package com.example.techchallenge

import cats.effect.Async
import cats.syntax.all._
import com.comcast.ip4s._
import com.example.techchallenge.post._
import com.example.techchallenge.user._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object TechChallengeServer:
  def run[F[_]: Async](userStore: UserStore[F], postStore: PostStore[F]): F[Nothing] = {
    val httpApp = (
      UserRoutes.routes[F](userStore) <+> PostRoutes.routes[F](postStore)
    ).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)
    for {
      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
