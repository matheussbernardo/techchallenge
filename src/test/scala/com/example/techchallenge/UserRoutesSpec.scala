package com.example.techchallenge

import cats.effect.IO
import com.example.techchallenge.TechChallengeRoutes
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._
import com.example.techchallenge.TechChallengeRoutes.UserReq
import org.http4s.circe.CirceEntityCodec._

class UserRoutesSpec extends CatsEffectSuite:

  test("Calling POST /user returns status code 201") {
    assertIO(retUserReq.map(_.status), Status.Created)
  }

  private[this] val retUserReq: IO[Response[IO]] =
    val getHW = Request[IO](Method.POST, uri"/user").withEntity(
      UserReq("name", "email@email.com")
    )
    TechChallengeRoutes.userRoutes[IO].orNotFound(getHW)
