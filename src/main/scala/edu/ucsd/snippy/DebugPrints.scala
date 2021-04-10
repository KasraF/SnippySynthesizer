package edu.ucsd.snippy

import org.slf4j.{Logger, LoggerFactory}

object DebugPrints
{
	// TODO Support this with the stdio interface
	val logger: Logger = LoggerFactory.getLogger(this.getClass)
	var debug: Boolean = false // this.logger.isDebugEnabled
	var info: Boolean = false // this.logger.isInfoEnabled

	def setNone(): Unit =
		this.debug = false
		this.info = false

	def dprintln(msg: String, args: Any*): Unit = if (this.debug) this.logger.debug(msg, args: _*)

	def iprintln(msg: String, args: Any*): Unit = if (this.info) this.logger.info(msg, args: _*)

	def eprintln(msg: String): Unit = ()//scala.sys.process.stderr.println(msg)

//	def eprintln(msg: String, args: Any*): Unit = this.logger.error(msg, args: _*)
//
//	def eprintln(msg: String, error: Throwable): Unit = this.logger.error(msg, error: Throwable)
}
