package com.example.techchallenge

import cats.effect.Async
import cats.syntax.all._
import com.comcast.ip4s._
import com.example.techchallenge.TechChallengeRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
object TechChallengeServer:

  def run[F[_]: Async]: F[Nothing] = {
    val httpApp = (
      TechChallengeRoutes.userRoutes[F]
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
