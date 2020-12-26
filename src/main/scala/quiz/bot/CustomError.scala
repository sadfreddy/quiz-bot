package quiz.bot

sealed trait CustomError extends Exception {
  def message : String
}

object CustomError {
  case class WebhookError(message: String) extends CustomError
}
