package eu.zujev.milovidobot.commmandGroups

import eu.zujev.milovidobot.DB
import eu.zujev.milovidobot.Milovidobot
import eu.zujev.milovidobot.lib.Const
import eu.zujev.milovidobot.lib.Func
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class PrivateHandler extends Milovidobot {
    static db = DB.getInstance()

    Boolean processUpdate(Update update) {
        println 'processing private query'
        db = DB.getInstance()

        if (update.hasMessage() && update.getMessage().hasText() && !update.hasCallbackQuery()) {
            def message_text = update.getMessage().getText()
            if (message_text.toLowerCase().startsWith('/start')) {
                //resetDBWait('all', update)
                sendMessage(Const.INTROTEXT, update.getMessage().getChatId().toString(), null, Func.getKeyboard(update.getMessage().getFrom().getId()), null)
            } else if (message_text.toLowerCase().startsWith('/help')) {
                sendMessage(Const.HELPTEXT, update)
            }
        } else if (update.hasCallbackQuery()) {
            def callbackAnswer = Const.CALLBACKREPLIES[new Random().nextInt(Const.CALLBACKREPLIES.size())]
            if (update.getCallbackQuery().getData() == 'newpoem') {
                answerCallbackQuery(update.getCallbackQuery().getId(), callbackAnswer)
                editMessage(Func.randomPoem(), update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), null)
                Func.saveUsage('poem', update.getCallbackQuery().getFrom().getId())
            } else if (update.getCallbackQuery().getData() == 'newquote') {
                answerCallbackQuery(update.getCallbackQuery().getId(), callbackAnswer)
                editMessage(Func.randomQuote(), update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), null)
                Func.saveUsage('quote', update.getCallbackQuery().getFrom().getId())
            } else if (update.getCallbackQuery().getData() == 'aboutauthor') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'А вот и автор.')
                editMessage(Func.aboutAuthor(), update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), 'html')
            } else if (update.getCallbackQuery().getData() == 'donate') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Поддержи рублём.')
                editMessage(Func.donate(), update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), 'html')
            } else if (update.getCallbackQuery().getData() == 'addpoem') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Жду.')
                resetDBWait('poem', update)
                def cancelKeyboard = getCancelKeyboard()
                editMessage('Жду текста стихотворения.', update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), cancelKeyboard, null)
            } else if (update.getCallbackQuery().getData() == 'addquote') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Жду.')
                resetDBWait('quote', update)
                def cancelKeyboard = getCancelKeyboard()
                editMessage('Жду цитату.', update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), cancelKeyboard, null)
            } else if (update.getCallbackQuery().getData() == 'addauthor') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Жду.')
                resetDBWait('author', update)
                def cancelKeyboard = getCancelKeyboard()
                editMessage('Жду текста информации об авторе. Пока что тут так:\n\n' + Func.aboutAuthor(), update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), cancelKeyboard, null)
            } else if (update.getCallbackQuery().getData() == 'adddonate') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Жду.')
                resetDBWait('donate', update)
                def cancelKeyboard = getCancelKeyboard()
                editMessage('Жду текста о донате. Пока что тут так:\n\n' + Func.donate(), update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), cancelKeyboard, null)
            } else if (update.getCallbackQuery().getData() == 'cancel') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Хорошо, отменил.')
                resetDBWait('all', update)
                editMessage(Const.INTROTEXT, update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), null)
            } else if (update.getCallbackQuery().getData() == 'stats') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Вот, что я насобирал.')
                editMessage(Func.getStatsText(), update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), 'html')
            } else if (update.getCallbackQuery().getData() == 'takePartInContest') {
                //if (update.getCallbackQuery().getFrom().getUserName()) {
                    answerCallbackQuery(update.getCallbackQuery().getId(), 'Отлично! Теперь ты участвуешь в розыгрыше!')
                    //answerCallbackQuery(update.getCallbackQuery().getId(), 'Наша любовь к лингвистике взаимна. Если вам улыбнётся удача, я пришлю открытку.')
                    db.sadd('usersTakingPartInContest', update.getCallbackQuery().getFrom().getId().toString())
                    editMessage('Отлично! Теперь вы участвуете в розыгрыше!\n\n' + Const.INTROTEXT, update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), 'html')
                //} else {
                //    answerCallbackQuery(update.getCallbackQuery().getId(), 'Открой на минутку свой ник, пожалуйста.')
                //    editMessage('Чтобы поучаствовать в розыгрыше, открой свой ник. Иначе мне будет никак не связаться с тобой. :(\n\n' + Const.INTROTEXT, update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), 'html')
                //}
            } else if (update.getCallbackQuery().getData() == 'alreadyTakingPartInContest') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Ура!!! 🥳')
                //answerCallbackQuery(update.getCallbackQuery().getId(), 'Ура!!! ❤️')
            } else if (update.getCallbackQuery().getData() == 'startcontest') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Точно?')
                def cancelKeyboard = getOkCancelKeyboard('startcontest')
                editMessage('Если хочешь начать сбор пользователей для нового розыгрыша, подтверди кнопочкой.', update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), cancelKeyboard, null)
            } else if (update.getCallbackQuery().getData() == 'stopcontest') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Точно?')
                def cancelKeyboard = getOkCancelKeyboard('stopcontest')
                editMessage('Если хочешь отменить текущий розыгрыш, подтверди кнопочкой.', update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), cancelKeyboard, null)
            } else if (update.getCallbackQuery().getData() == 'choosewinner') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Сколько будет победителей?')
                def cancelKeyboard = getWinnersKeyboard()
                editMessage('Выбор победителя остановит розыгрыш.\n\nПока что участников ' + db.scard('usersTakingPartInContest') + '.', update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), cancelKeyboard, null)
            } else if (update.getCallbackQuery().getData() == 'ok:startcontest') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Отлично, начинаем собирать участников!')
                db.set('contest', 'yes')
                db.del('usersTakingPartInContest')
                editMessage('Розыгрыш начат.\n--\n' + Const.INTROTEXT, update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), null)
            } else if (update.getCallbackQuery().getData() == 'ok:stopcontest') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Хорошо, розыгрыш остановлен.')
                db.set('contest', 'no')
                db.del('usersTakingPartInContest')
                editMessage('Розыгрыш остановлен.\n--\n' + Const.INTROTEXT, update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), null)
            } else if (update.getCallbackQuery().getData() == '1winner') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Получай победителей!')
                def winner = db.srandmember('usersTakingPartInContest')
                db.del('lastWinners')
                db.sadd('lastWinners', winner)
                db.set('contest', 'no')
                db.del('usersTakingPartInContest')
                String text = 'Розыгрыш завершён. Победителем объявляется: ' + Func.resolveUsername(winner) + '!\n--\n' + Const.INTROTEXT
                editMessage(text, update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), 'html')
            } else if (update.getCallbackQuery().getData() == '2winners') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Получай победителей!')
                def winner = db.srandmember('usersTakingPartInContest')
                db.srem('usersTakingPartInContest', winner)
                def winner2 = db.srandmember('usersTakingPartInContest')
                db.del('lastWinners')
                db.sadd('lastWinners', winner)
                db.sadd('lastWinners', winner2)
                db.set('contest', 'no')
                db.del('usersTakingPartInContest')
                String text = 'Розыгрыш завершён. Победителями объявляются: ' + Func.resolveUsername(winner) + ' и ' + Func.resolveUsername(winner2) + '!\n--\n' + Const.INTROTEXT
                editMessage(text, update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), 'html')
            } else if (update.getCallbackQuery().getData() == '3winners') {
                answerCallbackQuery(update.getCallbackQuery().getId(), 'Получай победителей!')
                def winner = db.srandmember('usersTakingPartInContest')
                db.srem('usersTakingPartInContest', winner)
                def winner2 = db.srandmember('usersTakingPartInContest')
                db.srem('usersTakingPartInContest', winner2)
                def winner3 = db.srandmember('usersTakingPartInContest')
                db.del('lastWinners')
                db.sadd('lastWinners', winner)
                db.sadd('lastWinners', winner2)
                db.sadd('lastWinners', winner3)
                db.set('contest', 'no')
                db.del('usersTakingPartInContest')
                String text = 'Розыгрыш завершён. Победителями объявляются: ' + Func.resolveUsername(winner) + ', ' + Func.resolveUsername(winner2) + ' и ' + Func.resolveUsername(winner3) + '!\n--\n' + Const.INTROTEXT
                editMessage(text, update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId(), Func.getKeyboard(update.getCallbackQuery().getFrom().getId()), 'html')
            }
        }
    }

    private InlineKeyboardMarkup getCancelKeyboard() {
        def cancelBtn = new InlineKeyboardButton()
        cancelBtn.setText('Отменить')
        cancelBtn.setCallbackData('cancel')

        def kbd = new InlineKeyboardMarkup()
        kbd.setKeyboard([[cancelBtn]])

        return (kbd)
    }

    private InlineKeyboardMarkup getOkCancelKeyboard(String cat) {
        def cancelBtn = new InlineKeyboardButton()
        cancelBtn.setText('Отменить')
        cancelBtn.setCallbackData('cancel')
        def okBtn = new InlineKeyboardButton()
        okBtn.setText('Подтвердить')
        okBtn.setCallbackData('ok' + ':' + cat)

        def kbd = new InlineKeyboardMarkup()
        kbd.setKeyboard([[okBtn, cancelBtn]])

        return kbd
    }

    private InlineKeyboardMarkup getWinnersKeyboard() {
        def cancelBtn = new InlineKeyboardButton()
        cancelBtn.setText('Отменить')
        cancelBtn.setCallbackData('cancel')

        def userCnt = db.scard('usersTakingPartInContest')

        def oneWinnerBtn, twoWinnersBtn, threeWinnersBtn

        if (userCnt > 0) {
            oneWinnerBtn = new InlineKeyboardButton()
            oneWinnerBtn.setText('1 победитель')
            oneWinnerBtn.setCallbackData('1winner')
        }
        if (userCnt > 1) {
            twoWinnersBtn = new InlineKeyboardButton()
            twoWinnersBtn.setText('2 победителя')
            twoWinnersBtn.setCallbackData('2winners')
        }
        if (userCnt > 2) {
            threeWinnersBtn = new InlineKeyboardButton()
            threeWinnersBtn.setText('3 победителя')
            threeWinnersBtn.setCallbackData('3winners')
        }

        def kbdRow1 = []
        if (oneWinnerBtn) { kbdRow1.add(oneWinnerBtn) }
        if (twoWinnersBtn) { kbdRow1.add(twoWinnersBtn) }
        def kbdRow2 = []
        if (threeWinnersBtn) { kbdRow2.add(threeWinnersBtn) }
        if (cancelBtn) { kbdRow2.add(cancelBtn) }

        def kbdRows = []
        if (kbdRow1.size() > 0) { kbdRows.add(kbdRow1) }
        kbdRows.add(kbdRow2)

        def kbd = new InlineKeyboardMarkup()
        kbd.setKeyboard(kbdRows)

        return kbd
    }

    private resetDBWait(cat, update) {
        db.del('waitingforpoem:' + update.getCallbackQuery().getFrom().getId())
        db.del('waitingforpoemmsg:' + update.getCallbackQuery().getFrom().getId())
        db.del('waitingforquote:' + update.getCallbackQuery().getFrom().getId())
        db.del('waitingforquotemsg:' + update.getCallbackQuery().getFrom().getId())
        db.del('waitingforauthor:' + update.getCallbackQuery().getFrom().getId())
        db.del('waitingforauthormsg:' + update.getCallbackQuery().getFrom().getId())
        db.del('waitingfordonate:' + update.getCallbackQuery().getFrom().getId())
        db.del('waitingfordonatemsg:' + update.getCallbackQuery().getFrom().getId())
        switch (cat) {
            case "poem":
                db.setex('waitingforpoem:' + update.getCallbackQuery().getFrom().getId(), 60, update.getCallbackQuery().getMessage().getChatId() as String)
                db.setex('waitingforpoemmsg:' + update.getCallbackQuery().getFrom().getId(), 60, update.getCallbackQuery().getMessage().getMessageId() as String)
                break
            case "quote":
                db.setex('waitingforquote:' + update.getCallbackQuery().getFrom().getId(), 60, update.getCallbackQuery().getMessage().getChatId() as String)
                db.setex('waitingforquotemsg:' + update.getCallbackQuery().getFrom().getId(), 60, update.getCallbackQuery().getMessage().getMessageId() as String)
                break
            case "author":
                db.setex('waitingforauthor:' + update.getCallbackQuery().getFrom().getId(), 60, update.getCallbackQuery().getMessage().getChatId() as String)
                db.setex('waitingforauthormsg:' + update.getCallbackQuery().getFrom().getId(), 60, update.getCallbackQuery().getMessage().getMessageId() as String)
                break
            case "donate":
                db.setex('waitingfordonate:' + update.getCallbackQuery().getFrom().getId(), 60, update.getCallbackQuery().getMessage().getChatId() as String)
                db.setex('waitingfordonatemsg:' + update.getCallbackQuery().getFrom().getId(), 60, update.getCallbackQuery().getMessage().getMessageId() as String)
                break
        }
    }
}
