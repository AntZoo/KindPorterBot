package eu.zujev.milovidobot.commmandGroups


import eu.zujev.milovidobot.Milovidobot
import eu.zujev.milovidobot.lib.Const
import eu.zujev.milovidobot.lib.Func
import org.telegram.telegrambots.meta.api.objects.Update

class UserHandler extends Milovidobot {
    Boolean processUpdate(Update update) {
        def message_text = update.getMessage().getText().toLowerCase()

        if (message_text.startsWith('/стих') || message_text.startsWith('/poem')) {
            println 'random poem'
            if (message_text.length() > 5) {
                def text = Func.randomPoem(message_text.drop(5) as String)
                if (text) {
                    sendMessage(text, update)
                } else {
                    sendMessage('По твоему запросу ничего не нашёл, держи хоть такое.', update)
                    sendMessage(Func.randomPoem(), update.getMessage().getChatId() as String, null, null, null)
                }
            } else {
                sendMessage(Func.randomPoem(), update)
            }
            Func.saveUsage('poem', update.getMessage().getFrom().getId())
            return true
        } else if (message_text.startsWith('/цитата') || message_text.startsWith('/quote')) {
            println 'random quote'
            if (message_text.startsWith('/цитата') && message_text.length() > 7) {
                def text = Func.randomQuote(message_text.drop(7) as String)
                if (text) {
                    sendMessage(text, update)
                } else {
                    sendMessage('По твоему запросу ничего не нашёл, держи хоть такое.', update)
                    sendMessage(Func.randomQuote(), update.getMessage().getChatId() as String, null, null, null)
                }
            } else if (message_text.startsWith('/quote') && message_text.length() > 6) {
                def text = Func.randomQuote(message_text.drop(6) as String)
                if (text) {
                    sendMessage(text, update)
                } else {
                    sendMessage('По твоему запросу ничего не нашёл, держи хоть такое.', update)
                    sendMessage(Func.randomQuote(), update.getMessage().getChatId() as String, null, null, null)
                }
            } else {
                sendMessage(Func.randomQuote(), update)
            }
            Func.saveUsage('quote', update.getMessage().getFrom().getId())
            return true
        } else if (message_text.startsWith('/автор') || message_text.startsWith('/author')) {
            println 'author info'
            sendMessage(Func.aboutAuthor(), update.getMessage().getChatId() as String, update.getMessage().getMessageId() as String, null, 'html')
            return true
        } else if (message_text.startsWith('/донат') || message_text.startsWith('/donate')) {
            println 'donate info'
            sendMessage(Func.donate(), update.getMessage().getChatId() as String, update.getMessage().getMessageId() as String, null, 'html')
            return true
        } else if (message_text.startsWith('/start') && !update.getMessage().isUserMessage()) {
            println 'sending start to pm'
            sendMessage('Я отправил панельку тебе в личные сообщения.', update)
            sendMessage(Const.INTROTEXT, update.getMessage().getFrom().getId() as String, null, Func.getKeyboard(update.getMessage().getFrom().getId()), null)
        } else if (message_text.startsWith('/help') && !update.getMessage().isUserMessage()) {
            println 'sending help to pm'
            sendMessage('Я отправил помощь тебе в личные сообщения.', update)
            sendMessage(Const.HELPTEXT, update.getMessage().getFrom().getId() as String, null, Func.getKeyboard(update.getMessage().getFrom().getId()), null)
        }
        return false
    }
}
