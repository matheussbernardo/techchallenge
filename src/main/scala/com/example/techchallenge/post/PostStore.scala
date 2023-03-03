package com.example.techchallenge.post

import cats.effect.kernel.Async
import cats.effect.kernel.MonadCancel
import cats.syntax.all._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import java.util.UUID

trait PostStore[F[_]]:
  def get(id: UUID): F[PostRoutes.PostResponse]
  def insert(
      id: UUID,
      content: String,
      author: String
  ): F[Unit]

  def insertImage(
      id: UUID,
      base64Image: String
  ): F[Unit]

object PostStore:
  def impl[F[_]: Async](transactor: Transactor[F]): PostStore[F] =
    new PostStore[F]:
      def get(id: UUID): F[PostRoutes.PostResponse] =
        sql"""
        select id, content, image, author from post where id = $id"
        """.query[PostRoutes.PostResponse].unique
        .transact(transactor)

      override def insert(
          id: UUID,
          content: String,
          author: String
      ): F[Unit] =
        sql"""
          insert into post (id, content, author)
          values ($id, $content, $author)
          """.update.run.void
          .transact(transactor)

      override def insertImage(
          id: UUID,
          base64Image: String
      ): F[Unit] =
        sql"""
          update post set image = $base64Image where id = $id"
          """.update.run.void
          .transact(transactor)
