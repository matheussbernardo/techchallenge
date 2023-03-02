package com.example.techchallenge.user

import cats.effect.Concurrent
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import io.circe.Codec

object UserRoutes:
  case class UserReq(name: String, email: String) derives Codec.AsObject

  def routes[F[_]: Concurrent](userStore: UserStore[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case req @ POST -> Root / "user" =>
      for
        userReq <- req.as[UserReq]
        resultFromInsert <- userStore.insert(userReq.name, userReq.email)
        resp <- resultFromInsert match 
          case InsertResult.Inserted => Created()
          case InsertResult.Duplicated => Conflict()
      yield resp
    }
