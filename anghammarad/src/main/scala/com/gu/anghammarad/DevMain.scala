package com.gu.anghammarad

import com.gu.anghammarad.models._
import org.slf4j.{Logger, LoggerFactory}


object DevMain {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    val environment = Env()

    val devConfig = Config.loadConfig()
    val devMappings = Serialization.parseAllMappings(devConfig.getOrElse(""))

    logger.info(environment.toString)
    logger.info(s"Loaded configuration from S3: ${devConfig.isSuccess}")
    logger.info(s"Config parsing succeeded: ${devMappings.isSuccess}")

    // get config from s3... and fail fast if this is not successful
    val config: String = Config.loadConfig().getOrElse(throw new RuntimeException("Unable to load config from S3"))

    // parse the config and extract the mappings...
    val mappings: List[Mapping] = Serialization.parseAllMappings(config).getOrElse(throw new RuntimeException("Failed to parse config"))

    logger.info(mappings.toString)

//    val result = Anghammarad.run(notification, config)
  }
}
