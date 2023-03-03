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
import java.time.LocalDate
import doobie.postgres.syntax.fragment

trait PostStore[F[_]]:
  def getAll(
      fromAuthor: Option[String],
      sortByDate: Option[Boolean]
  ): F[List[PostRoutes.PostResponse]]

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
      def getAll(
          fromAuthor: Option[String],
          sortByDate: Option[Boolean]
      ): F[List[PostRoutes.PostResponse]] =
        val defaultQuery =
          sql"""
            select id, content, image, author, date from post 
            where (author = $fromAuthor OR $fromAuthor IS NULL)
            """

        val sortedQuery =
          sql"""
            select id, content, image, author, date from post 
            where (author = $fromAuthor OR $fromAuthor IS NULL)
            order by $sortByDate DESC
            """

        val query = sortByDate match
          case None        => defaultQuery
          case Some(false) => defaultQuery
          case Some(true)  => sortedQuery

        query
          .query[PostRoutes.PostResponse]
          .to[List]
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
          update post set image = $base64Image where id = $id
          """.update.run.void
          .transact(transactor)
