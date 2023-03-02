package com.example.techchallenge

import cats.effect.Concurrent
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import io.circe.Codec

object TechChallengeRoutes:
  case class UserReq(name: String, email: String) derives Codec.AsObject

  def userRoutes[F[_]: Concurrent]: HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case req @ POST -> Root / "user" =>
      for
        userReq <- req.as[UserReq]
        resp <- Created()
      yield resp
    }
