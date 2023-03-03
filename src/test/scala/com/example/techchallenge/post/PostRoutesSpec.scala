package com.example.techchallenge.post

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._

import java.time.ZonedDateTime
import java.util.UUID
import java.time.LocalDate
import com.example.techchallenge.post.PostRoutes.PostResponse
import java.time.LocalDateTime

class PostRoutesSpec extends CatsEffectSuite:

  test("Calling POST /post returns status code 201") {
    assertIO(retCreatePost.map(_.status), Status.Created)
  }

  private[this] val retCreatePost: IO[Response[IO]] =

    val postStore =
      new PostStore[IO]:
        def getAll(
            fromAuthor: Option[String],
            sortByDate: Option[Boolean]
        ): IO[List[PostRoutes.PostResponse]] = IO.pure(
          List(
            PostRoutes.PostResponse(
              UUID.randomUUID(),
              "content",
              "image",
              "author",
              LocalDateTime.now()
            )
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
