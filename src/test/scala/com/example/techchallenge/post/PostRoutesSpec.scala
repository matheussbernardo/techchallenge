package com.example.techchallenge.post

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.headers._
import java.time.ZonedDateTime
import java.util.UUID
import java.time.LocalDate
import com.example.techchallenge.post.PostRoutes.PostResponse
import java.time.LocalDateTime
import fs2.Stream

class PostRoutesSpec extends CatsEffectSuite:

  test("Calling POST /post returns status code 201") {
    val createPostRequest = Request[IO](Method.POST, uri"/post").withEntity(
      PostRoutes.PostRequest("Content", "email@email.com")
    )
    assertIO(doRequest(createPostRequest).map(_.status), Status.Created)
  }

  test("Calling GET /posts returns all posts") {
    val getAllPostRequest = Request[IO](Method.GET, uri"/posts")
    assertIO(doRequest(getAllPostRequest).map(_.status), Status.Ok)
  }

  private[this] val doRequest: Request[IO] => IO[Response[IO]] = request =>
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

    PostRoutes.routes[IO](postStore).orNotFound(request)
