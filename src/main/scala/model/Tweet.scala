package model

case class Tweet(message: String,timestamp:Long)

case class Handle(handle: String) extends AnyVal
