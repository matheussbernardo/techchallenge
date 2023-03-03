package com.example.techchallenge.post

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._

import java.time.ZonedDateTime
import java.util.UUID

class PostRoutesSpec extends CatsEffectSuite:

  test("Calling POST /post returns status code 201") {
    assertIO(retCreatePost.map(_.status), Status.Created)
  }

  private[this] val retCreatePost: IO[Response[IO]] =

    val postStore =
      new PostStore[IO]:
        def get(id: UUID): IO[PostRoutes.PostResponse] = IO.pure(
          PostRoutes.PostResponse(
            UUID.randomUUID(),
            "content",
            "image",
            "author"
          )
        )
        def insert(
            id: UUID,
            content: String,
            author: String
        ): IO[Unit] =
          IO.pure(())

        def insertImage(
            id: UUID,
            base64Image: String
        ): IO[Unit] = IO.pure(())

    val getHW = Request[IO](Method.POST, uri"/post").withEntity(
      PostRoutes.PostRequest("Content", "email@email.com")
    )

    PostRoutes.routes[IO](postStore).orNotFound(getHW)
