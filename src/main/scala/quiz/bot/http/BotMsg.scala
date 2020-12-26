package quiz.bot.http

import quiz.bot.http.BotMsg.Out.CallbackQuery.User

object BotMsg {
  type ChatId = Long
  type MessageId = Long
  type UpdateId = Long
  type UserId = Long

  object In {
    final case class Chat(id: ChatId)

    final case class Message(message_id: MessageId, chat: Chat, text: Option[String])
    final case class Update(update_id: UpdateId, message: Message)
  }

  object Out {
    type InlineButtons = List[List[InlineKeyboardButton]]
    final case class InlineKeyboardButton(text: String, callback_data: String)
    final case class MessageResponse(ok: Boolean, result: Result)
    final case class Result(message_id: MessageId)
    final case class WebhookResponse(ok: Boolean, description: String, error_code: Option[Int])

    final case class CallbackQuery(from: User, data: Option[String], message: MessageResponse)

    object CallbackQuery {
      final case class User(id: UserId,  first_name: String, username: Option[String])
    }

  }

}
