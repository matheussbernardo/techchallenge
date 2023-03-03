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

object PostRoutes:
  case class PostRequest(content: String, author: String) derives Codec.AsObject

  case class PostResponse(
      id: UUID,
      content: String,
      image: String,
      author: String
  ) derives Codec.AsObject

  def routes[F[_]: Async](postStore: PostStore[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req @ GET -> Root / "post" / id =>
        val uuid = UUID.fromString(id)
        for
          post <- postStore.get(uuid)
          resp <- Ok(post)
        yield resp

      case req @ POST -> Root / "post" =>
        for
          postReq <- req.as[PostRequest]
          uuid = UUID.randomUUID()
          _ <- postStore.insert(uuid, postReq.content, postReq.author)
          resp <- Created(uuid)
        yield resp

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
