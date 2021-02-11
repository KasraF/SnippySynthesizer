package edu.ucsd.snippy

import org.slf4j.{Logger, LoggerFactory}

object DebugPrints
{
	val logger: Logger = LoggerFactory.getLogger(this.getClass)
	var debug: Boolean = this.logger.isDebugEnabled
	var info: Boolean = this.logger.isInfoEnabled

	def setNone(): Unit =
		this.debug = false
		this.info = false

	def dprintln(msg: String, args: Any*) = if (this.debug) this.logger.debug(msg, args: _*)

	def iprintln(msg: String, args: Any*) = if (this.info) this.logger.info(msg, args: _*)

	def eprintln(msg: String, args: Any*) = this.logger.error(msg, args: _*)

	def eprintln(msg: String, error: Throwable) = this.logger.error(msg, error: Throwable)
}
