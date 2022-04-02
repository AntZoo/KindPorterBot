package eu.zujev.milovidobot

import eu.zujev.milovidobot.commmandGroups.*
import eu.zujev.milovidobot.lib.Func
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class Milovidobot extends TelegramLongPollingBot {
    def config = new ConfigSlurper().parse(new File('config.groovy').toURL())

    @Override
    void onUpdateReceived(Update update) {
        println 'update received'

        def res = false

        Func.saveStats(update)

        if (update.hasMessage() && update.getMessage().hasText()) {
            def adminCommands = new AdminHandler()
            res = adminCommands.processUpdate(update)

            if (!res) {
                def userCommands = new UserHandler()
                res = userCommands.processUpdate(update)
            }
        }

        if (!res && update.hasInlineQuery()) {
            def inlineCommands = new InlineHandler()
            res = inlineCommands.processUpdate(update)
        }

        if (update.hasChosenInlineQuery()) {
            def query = update.getChosenInlineQuery().getResultId()
            Func.saveUsage(db.get('inline:' + query) as String, update.getChosenInlineQuery().getFrom().getId())
        }

        if (!res && ((update.hasMessage() && update.getMessage().getChat().isUserChat()) || update.hasCallbackQuery())) {
            def privateCommands = new PrivateHandler()
            res = privateCommands.processUpdate(update)
        }

        if (!res && update.hasMessage() && update.getMessage().getChat().isUserChat() && update.getMessage().hasDocument()) {
            def fileCommands = new FilesHandler()
            fileCommands.processUpdate(update)
        }

//        if (!res && update.hasMessage()) {
//
//        }
    }

    @Override
    String getBotUsername() {
        return config.bot_username
    }

    @Override
    String getBotToken() {
        return config.bot_api
    }

    Message sendMessage(String message_text, String chat_id, String replyto_id, InlineKeyboardMarkup keyboard, String md) {
        SendMessage message = new SendMessage()
        message.setChatId(chat_id)
        message.setText(message_text)
        message.setDisableWebPagePreview(true)
        if (replyto_id != null) {
            message.setReplyToMessageId(replyto_id as Integer)
        }
        if (keyboard != null) {
            message.setReplyMarkup(keyboard)
        }
        if (md) {
            switch (md) {
                case 'md':
                    message.setParseMode('MarkdownV2')
                    break
                case 'html':
                    message.setParseMode('HTML')
                    break
            }
        }
        try {
            return execute(message)
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }
        return null
    }

    Message sendMessage(String message_text, Update update) {
        sendMessage(message_text, update.getMessage().getChatId().toString(), update.getMessage().getMessageId() as String, null, null)
    }

    Message editMessage(String new_text, String chat_id, int message_id, InlineKeyboardMarkup keyboard, String md) {
        def message = new EditMessageText()
        message.setText(new_text)
        message.setChatId(chat_id)
        message.setMessageId(message_id)

        if (keyboard) {
            message.setReplyMarkup(keyboard)
            message.disableWebPagePreview()
        }

        if (md) {
            switch (md) {
                case 'md':
                    message.setParseMode('MarkdownV2')
                    break
                case 'html':
                    message.setParseMode('HTML')
                    break
            }
        }

        try {
            return execute(message) as Message
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }
        return null
    }

    Boolean deleteMessage(long chat_id, int message_id) {
        def message = new DeleteMessage()
        message.setChatId(chat_id.toString())
        message.setMessageId(message_id)

        try {
            return execute(message)
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }
        return false
    }

    Boolean answerInlineQuery(inlineQueryId, List message_texts){
        AnswerInlineQuery answer = new AnswerInlineQuery()
        answer.setInlineQueryId(inlineQueryId)
        answer.setResults(message_texts)
        try {
            return execute(answer)
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }
        return false
    }

    Boolean answerCallbackQuery(callbackQueryId, message_text) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery()
        answer.setText(message_text)
        answer.setCallbackQueryId(callbackQueryId)
        try {
            return execute(answer)
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }
        return false
    }
}
