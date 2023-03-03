package com.example.techchallenge

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits._
import com.example.techchallenge.post.PostStore
import com.example.techchallenge.user.UserStore
import doobie._
import doobie.implicits._

object Main extends IOApp.Simple:

  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:postgres", // connect URL (driver-specific)
    "admin", // user
    "admin" // password
  )

  val createUserTable =
    sql"""
      CREATE TABLE "user" (
        name text NOT NULL,
        email text NOT NULL UNIQUE
      )
  """.update.run

  val createPostTable =
    sql"""
      CREATE TABLE post (
        id uuid NOT NULL PRIMARY KEY,
        content text NOT NULL,
        image text,
        date timestamp DEFAULT NOW(),
        author text NOT NULL REFERENCES "user"(email)
      )
  """.update.run

  val run =
    for
      // Start Migrations
      _ <- createUserTable.transact[IO](transactor)
      _ <- createPostTable.transact[IO](transactor)
      // End Migrations

      userStore = UserStore.impl[IO](transactor)
      postStore = PostStore.impl[IO](transactor)

      _ <- TechChallengeServer.run[IO](userStore, postStore)
    yield ()
