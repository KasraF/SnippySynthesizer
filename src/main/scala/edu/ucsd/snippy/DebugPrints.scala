package edu.ucsd.snippy

import org.slf4j.{Logger, LoggerFactory}

object DebugPrints
{
	val logger: Logger = LoggerFactory.getLogger(this.getClass)
	val debug: Boolean = this.logger.isDebugEnabled
	val info: Boolean = this.logger.isInfoEnabled

	def dprintln(msg: String, args: Any*) = this.logger.debug(msg, args: _*)

	def iprintln(msg: String, args: Any*) = this.logger.info(msg, args: _*)

	def eprintln(msg: String, args: Any*) = this.logger.error(msg, args: _*)

	def eprintln(msg: String, error: Throwable) = this.logger.error(msg, error: Throwable)
}
