package com.example.techchallenge

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp.Simple:
  val run = TechChallengeServer.run[IO]
