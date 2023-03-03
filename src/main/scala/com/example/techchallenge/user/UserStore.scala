package com.example.techchallenge.user

import doobie.util.transactor
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.effect.kernel.MonadCancel
import doobie.postgres._
import cats.effect.kernel.Async

enum InsertResult:
  case Duplicated
  case Inserted

trait UserStore[F[_]]:
  def insert(name: String, email: String): F[InsertResult]

object UserStore:
  def impl[F[_]: Async](transactor: Transactor[F]): UserStore[F] =
    new UserStore[F]:
      def insert(name: String, email: String): F[InsertResult] =
        def query(name: String, email: String) =
          sql"""insert into "user" (name, email) values ($name, $email)""".update.run
        query(name, email)
          .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
            ()
          }
          .map {
            case Right(_) => InsertResult.Inserted
            case Left(_)  => InsertResult.Duplicated
          }
          .transact(transactor)
