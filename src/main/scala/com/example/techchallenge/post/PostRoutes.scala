package com.example.techchallenge.post

import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import io.circe.Codec
import java.time.ZonedDateTime
import java.util.UUID
import org.http4s.multipart.Multipart
import fs2.Stream
import fs2.compression.Compression
import cats.effect.kernel.Async
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import java.time.LocalDate
import org.http4s.QueryParamDecoder
import java.time.LocalDateTime

object PostRoutes:

  object AuthorQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[String]("author")

  object SortByDateQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[Boolean]("sortByDate")

  case class PostRequest(content: String, author: String) derives Codec.AsObject

  case class PostResponse(
      id: UUID,
      content: String,
      image: String,
      author: String,
      date: LocalDateTime
  ) derives Codec.AsObject

  def routes[F[_]: Async](postStore: PostStore[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      // Fetch All Posts and Filter by Author or Sort By Date
      case req @ GET -> Root / "posts" :? AuthorQueryParamMatcher(
            author
          ) +& SortByDateQueryParamMatcher(sortByDate) =>
        for
          post <- postStore.getAll(author, sortByDate)
          resp <- Ok(post)
        yield resp

      // Create a Post, returns the Post UUID
      case req @ POST -> Root / "post" =>
        for
          postReq <- req.as[PostRequest]
          uuid = UUID.randomUUID()
          _ <- postStore.insert(uuid, postReq.content, postReq.author)
          resp <- Created(uuid)
        yield resp

      // Update  a Post with an image
      case req @ PUT -> Root / "post" / "image" / id =>
        req.decode[Multipart[F]] { m =>
          m.parts.find(_.name == Some("file")) match {
            case None => BadRequest(s"File part not found")
            case Some(part) =>
              val saveImageStream = for
                contents <- part.body.through(Compression[F].gunzip())
                encodedImage <- contents.content.through(fs2.text.base64.encode)
                _ <- Stream.eval(
                  postStore.insertImage(UUID.fromString(id), encodedImage)
                )
              yield ()
              saveImageStream.compile.drain *> Ok()
          }
        }

    }
