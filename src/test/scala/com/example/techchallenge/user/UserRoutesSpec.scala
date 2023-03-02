package com.example.techchallenge.user

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityCodec._
import com.example.techchallenge.user.UserRoutes.UserReq

class UserRoutesSpec extends CatsEffectSuite:

  test("Calling POST /user returns status code 201") {
    assertIO(retUserReq(InsertResult.Inserted).map(_.status), Status.Created)
  }

  test("Calling POST /user duplicated email returns status code 409") {
    assertIO(retUserReq(InsertResult.Duplicated).map(_.status), Status.Conflict)
  }

  private[this] val retUserReq: InsertResult => IO[Response[IO]] = insertResult =>
    val userStore = 
      new UserStore[IO]:
        def insert(name: String, email: String): IO[InsertResult] = IO.pure(insertResult)
    val getHW = Request[IO](Method.POST, uri"/user").withEntity(
      UserReq("name", "email@email.com")
    )
    UserRoutes.routes[IO](userStore).orNotFound(getHW)
