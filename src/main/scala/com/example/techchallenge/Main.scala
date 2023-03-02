package com.example.techchallenge

import doobie._
import doobie.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import com.example.techchallenge.user._

import cats.implicits._

object Main extends IOApp.Simple:

  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:postgres", // connect URL (driver-specific)
    "admin", // user
    "admin" // password
  )

  val create =
    sql"""
      CREATE TABLE "user" (
        name text NOT NULL,
        email text NOT NULL UNIQUE
      )
  """.update.run

  val run =
    for
      _ <- create.transact[IO](transactor)
      userStore =  UserStore.impl[IO](transactor)
      _ <- TechChallengeServer.run[IO](userStore)
    yield ()
