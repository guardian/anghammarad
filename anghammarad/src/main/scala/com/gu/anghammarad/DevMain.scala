package com.gu.anghammarad

import com.gu.anghammarad.serialization.Serialization
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try


object DevMain {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    val stage = Config.getStage()
    val devConfig = Config.loadConfig(stage)
    val devMappings = Serialization.parseAllMappings(devConfig.getOrElse(""))

    logger.info(s"Loaded configuration from S3: ${devConfig.isSuccess}")
    logger.info(s"Config parsing succeeded: ${devMappings.isSuccess}")

    val result = for {
      config <- Config.loadConfig(stage)
      mappings <- Serialization.parseAllMappings(config)
      notification <- Try(???)
      _ <- Anghammarad.run(notification, mappings)
    } yield ()

    result.fold(
      { err =>
        logger.error("Failed to send notification", err)
      },
      _ =>
        logger.info("Ok")
    )
  }
}
