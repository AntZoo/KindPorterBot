package eu.zujev.milovidobot.commmandGroups

import eu.zujev.milovidobot.DB
import eu.zujev.milovidobot.Milovidobot
import eu.zujev.milovidobot.lib.Func
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle

class InlineHandler extends Milovidobot {
    static db = DB.getInstance()

    Boolean processUpdate(Update update) {
        println 'processing inline query'

        def query = update.getInlineQuery().getQuery()
        def reply_texts = []

        if (query.isEmpty() || query.contains('стих')) {
            def randId = (Math.floor(Math.random() * 1000))
            db.setex('inline:' + randId as String, 300, 'poem')
            def poemInlineResult = new InlineQueryResultArticle()
                            .setTitle('Стихотворение')
                            //.setDescription('случайное стихотворение')
                            .setThumbUrl('http://dump.zujev.eu/thumb_poem.jpg')
                            .setId(randId as String)
                            .setInputMessageContent(new InputTextMessageContent().setMessageText(Func.randomPoem()))
            reply_texts.add(poemInlineResult)
        }
        if (query.isEmpty() || query.contains('цитата')) {
            def randId = (Math.floor(Math.random() * 1000))
            db.setex('inline:' + randId as String, 300, 'quote')
            def quoteInlineResult = new InlineQueryResultArticle()
                            .setTitle('Цитата')
                            //.setDescription('случайная цитата')
                            .setThumbUrl('http://dump.zujev.eu/thumb_quote.jpg')
                            .setId(randId as String)
                            .setInputMessageContent(new InputTextMessageContent().setMessageText(Func.randomQuote()))
            reply_texts.add(quoteInlineResult)
        }
        /*if (query.isEmpty() || query.contains('автор')) {
            def authorInlineResult = new InlineQueryResultArticle()
                            .setTitle('Автор')
                            .setDescription('информация об авторе')
                            .setThumbUrl('http://dump.zujev.eu/thumb_author.jpg')
                            .setId((Math.floor(Math.random() * 1000)) as String)
                            .setInputMessageContent(new InputTextMessageContent().setMessageText(Func.aboutAuthor())
                            .disableWebPagePreview()
                            .setParseMode('HTML'))
            reply_texts.add(authorInlineResult)
        }
        if (query.isEmpty() || query.contains('донат')) {
            def authorInlineResult = new InlineQueryResultArticle()
                    .setTitle('Донат')
                    .setDescription('контакты для доната')
                    .setThumbUrl('http://dump.zujev.eu/bot.png')
                    .setId((Math.floor(Math.random() * 1000)) as String)
                    .setInputMessageContent(new InputTextMessageContent().setMessageText(Func.donate())
                    .disableWebPagePreview()
                    .setParseMode('HTML'))
            reply_texts.add(authorInlineResult)
        }*/

        answerInlineQuery(update.getInlineQuery().getId(), reply_texts)

        return reply_texts.size() > 0
    }
}
