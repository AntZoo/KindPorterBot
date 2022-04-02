package eu.zujev.milovidobot.lib

import eu.zujev.milovidobot.DB
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class Func {
    private static db = DB.getInstance()
    private static config = new ConfigSlurper().parse(new File('config.groovy').toURL())

    static String checkLength(Update update, String text) {
        if (text.length() > 4096) {
            return 'Слишком длинный текст, максимальный размер сообщения — 4096 символов.'
        } else {
            return null
        }
    }

    static String dumbDownString(String input) {
        return input.toLowerCase()
                    .trim()
                    .replaceAll("\\p{P}", "")
                    .replaceAll("\\s+", " ")
    }

    static String resolveUsername(String id) {
        def name = db.hget('useridToName', id)
        if (name && name != '') {
            return '@' + name
        } else {
            name = db.hget('useridToName', id + ':name')
            if (name) {
                return '<a href="tg://user?id=' + id + '">' + name + '</a>'
            } else {
                return 'Пользователь #' + id + ' (скрыл имя)'
            }
        }
    }

    static String randomPoem() {
        def answer = db.srandmember('poems').trim()
        if (answer) {
            answer = answer + '\n' + Const.CHANNEL_LINK
        } else {
            answer = 'В базе нет совсем никаких стихов. :('
        }
        return answer
    }

    static String randomPoem(String query) {
        def allPoems = db.smembers('poems')
        def formattedQuery = dumbDownString(query)
        def resultSet = allPoems.findAll { dumbDownString(it).contains(formattedQuery) }
        def answer = null
        if (resultSet.size() > 0) {
            answer = resultSet[new Random().nextInt(resultSet.size())].trim() + '\n' + Const.CHANNEL_LINK
        }
        return answer
    }

    static String randomQuote() {
        def answer = db.srandmember('quotes').trim()
        if (answer) {
            answer = "«" + answer + "»\n\n— Ви Миловидова\n" + Const.CHANNEL_LINK
        } else {
            answer = 'В базе нет совсем никаких цитат. :('
        }
        return answer
    }

    static String randomQuote(String query) {
        def allPoems = db.smembers('quotes')
        def formattedQuery = dumbDownString(query)
        def resultSet = allPoems.findAll { dumbDownString(it).contains(formattedQuery) }
        def answer = null
        if (resultSet.size() > 0) {
            answer = resultSet[new Random().nextInt(resultSet.size())].trim()
        }
        if (answer) {
            answer = "«" + answer + "»\n\n— Ви Миловидова\n" + Const.CHANNEL_LINK
        }
        return answer
    }

    static String aboutAuthor() {
        return db.get('author') ?: 'Информация об авторе не заполнена. :('
    }

    static String donate() {
        return db.get('donate') ?: 'Информация о донате не заполнена. :('
    }

    static InlineKeyboardMarkup getKeyboard(int User) {
        def contest = db.get('contest') ?: 'no'
        def alreadyTakingPart = db.sismember('usersTakingPartInContest', User.toString())

        def keyboard_row1 = []
        def poemkey = new InlineKeyboardButton()
        poemkey.setText('Стихотворение')
        poemkey.setCallbackData('newpoem')
        def quotekey = new InlineKeyboardButton()
        quotekey.setText('Цитата')
        quotekey.setCallbackData('newquote')
        keyboard_row1.add(poemkey)
        keyboard_row1.add(quotekey)
        def keyboard_row2 = []
        def authorkey = new InlineKeyboardButton()
        authorkey.setText('Автор')
        authorkey.setCallbackData('aboutauthor')
        def donatekey = new InlineKeyboardButton()
        donatekey.setText('₽ $ €')
        donatekey.setCallbackData('donate')
        keyboard_row2.add(authorkey)
        keyboard_row2.add(donatekey)
        def keyboard_rows = []
        keyboard_rows.add(keyboard_row1)
        keyboard_rows.add(keyboard_row2)

        if (contest == 'yes' && !config.admins.contains(User)) {
            def contestkey = new InlineKeyboardButton()
            if (alreadyTakingPart) {
                contestkey.setText('Вы участвуете в розыгрыше! 🎉🎉🎉')
                //contestkey.setText('Вы участвуете в розыгрыше! ❤️❤️❤️')
                contestkey.setCallbackData('alreadyTakingPartInContest')
            } else {
                contestkey.setText('Поучаствовать в розыгрыше')
                //contestkey.setText('❤️')
                contestkey.setCallbackData('takePartInContest')
            }
            def keyboard_row2andaHalf = []
            keyboard_row2andaHalf.add(contestkey)
            keyboard_rows.add(keyboard_row2andaHalf)
        }

        if (config.admins.contains(User)) {
            def keyboard_row3 = []
            def addpoemkey = new InlineKeyboardButton()
            addpoemkey.setText('Добавить стихотворение')
            addpoemkey.setCallbackData('addpoem')
            def addquotekey = new InlineKeyboardButton()
            addquotekey.setText('Добавить цитату')
            addquotekey.setCallbackData('addquote')
            keyboard_row3.add(addpoemkey)
            keyboard_row3.add(addquotekey)
            def keyboard_row4 = []
            def addauthorkey = new InlineKeyboardButton()
            addauthorkey.setText('Изменить информацию об авторе')
            addauthorkey.setCallbackData('addauthor')
            def adddonate = new InlineKeyboardButton()
            adddonate.setText('Изменить донат')
            adddonate.setCallbackData('adddonate')
            keyboard_row4.add(addauthorkey)
            keyboard_row4.add(adddonate)
            def keyboard_row5 = []
            def startStopContestKey = new InlineKeyboardButton()
            if (contest == 'yes') {
                startStopContestKey.setText('Отменить розыгрыш')
                startStopContestKey.setCallbackData('stopcontest')
                def chooseWinnerKey = new InlineKeyboardButton()
                chooseWinnerKey.setText('Выбрать победителя')
                chooseWinnerKey.setCallbackData('choosewinner')
                keyboard_row5.add(startStopContestKey)
                keyboard_row5.add(chooseWinnerKey)
            } else {
                startStopContestKey.setText('Начать розыгрыш')
                startStopContestKey.setCallbackData('startcontest')
                keyboard_row5.add(startStopContestKey)
            }
            def keyboard_row6 = []
            def statskey = new InlineKeyboardButton()
            statskey.setText('Статистика')
            statskey.setCallbackData('stats')
            keyboard_row6.add(statskey)
            keyboard_rows.add(keyboard_row3)
            keyboard_rows.add(keyboard_row4)
            keyboard_rows.add(keyboard_row5)
            keyboard_rows.add(keyboard_row6)
        }

        def kbd = new InlineKeyboardMarkup()
        kbd.setKeyboard(keyboard_rows)

        return kbd
    }

    static saveStats(Update update) {
        def userEntity = null
        if (update.hasMessage()) {
            userEntity = update.getMessage().getFrom()
        } else if (update.hasInlineQuery()) {
            userEntity = update.getInlineQuery().getFrom()
        } else if (update.hasCallbackQuery()) {
            userEntity = update.getCallbackQuery().getFrom()
        }
        def userId = userEntity.getId()

        if (userId && !config.admins.contains(userId)) {
            if (!!userEntity.getUserName()) {
                db.hset('useridToName', userId as String, userEntity.getUserName())
            }
            db.hset('useridToName', (userId as String) + ':name', userEntity.getFirstName() + (userEntity.getLastName() ?: ''))

            def chattype = null
            def group = null

            if (update.hasMessage()) {
                if (update.getMessage().isUserMessage()) {
                    chattype = 'pm'
                } else if (update.getMessage().isGroupMessage()) {
                    chattype = 'group'
                    group = update.getMessage().getChatId()
                }
            } else if (update.hasCallbackQuery()) {
                chattype = 'pm'
            } else if (update.hasInlineQuery()) {
                chattype = 'inline'
            }

            db.sadd('stats:users', userId as String)

            if (group) {
                db.sadd('stats:groups', group as String)
            }

            if (chattype == 'pm') {
                db.incr('stats:used:pm')
            } else if (chattype == 'inline') {
                db.incr('stats:used:inline')
            } else if (chattype == 'group') {
                db.incr('stats:used:group')
            }
        }
    }

    static saveUsage(String cat, int user) {
        if (cat && !config.admins.contains(user)) {
            db.incr('stats:' + cat)
            db.zincrby('usageByUser:' + cat, 1, user as String)
            db.zincrby('usageByUser:all', 1, user as String)
        }
    }

    static getStatsText() {
        def totalUsers = db.scard('stats:users') ?: 0
        def totalGroups = db.scard('stats:groups') ?: 0
        def usedInPM = db.get('stats:used:pm') ?: 0
        def usedInGroups = db.get('stats:used:group') ?: 0
        def usedInline = db.get('stats:used:inline') ?: 0
        def poemsRequested = db.get('stats:poem') ?: 0
        def quotesRequested = db.get('stats:quote') ?: 0
        def topThreePoems = db.zrevrangeWithScores('usageByUser:poem',0,2) ?: []
        def topThreeQuotes = db.zrevrangeWithScores('usageByUser:quote',0,2) ?: []
        def topThreeOverall = db.zrevrangeWithScores('usageByUser:all',0,2) ?: []
        def contestRunning = db.get('contest') ?: 'no'
        def usersInContest = db.scard('usersTakingPartInContest')

        def head = 'Вот такая у нас статистика.\n\n'

        def contestText = ''
        if (contestRunning == 'yes') {
            def users = (usersInContest.toString().reverse().take(1) == '1' && usersInContest.toString().reverse().take(2) != '11') ? usersInContest + ' пользователя.' : usersInContest + ' пользователей.'
            contestText = 'Во-первых, у нас идёт розыгрыш. Пока что я насобирал ' + users + (usersInContest == 0 ? ' :( ' : '') + '\n\n'
        }

        def lastWinners = db.smembers('lastWinners')
        if (lastWinners.size() > 0) {
            contestText += 'В прошлый раз в розыгрыше '
            if (lastWinners.size() == 1) {
                contestText += 'победил(а) ' + resolveUsername(lastWinners[0]) + '.'
            } else if (lastWinners.size() == 2) {
                contestText += 'победили ' + resolveUsername(lastWinners[0]) + ' и ' + resolveUsername(lastWinners[1]) + '.'
            } else if (lastWinners.size() == 3) {
                contestText += 'победили ' + resolveUsername(lastWinners[0]) + ', ' + resolveUsername(lastWinners[1]) + ' и ' + resolveUsername(lastWinners[2]) + '.'
            }
            contestText += '\n\n'
        }

        def unique = 'Всего со мной общалось\n' +
                '- уникальных пользователей: <pre>' + totalUsers + '</pre>;\n' +
                '- уникальных групп: <pre>' + totalGroups + '</pre>.\n\n'

        def loc = 'Меня спрашивали\n' +
                '- в группе: <pre>' + usedInGroups + '</pre> раз(а);\n' +
                '- в личке: <pre>' + usedInPM + '</pre> раз(а);\n' +
                '- инлайном: <pre>' + usedInline + '</pre> раз(а).\n\n'

        def total = 'Всего я успел раздать\n' +
                '- стихотворений: <pre>' + poemsRequested + '</pre>;\n' +
                '- цитат: <pre>' + quotesRequested + '</pre>.\n\n'

        def top3 = ''
        if (topThreeOverall.size() == 3 && topThreePoems.size() == 3 && topThreeQuotes.size() == 3) {
            top3 = 'Небольшой топ.\n\nБольше всего стихов просили:\n'

            def index = 1
            topThreePoems.each { tup ->
                tup.each { prop ->
                    top3 = top3 + index + '. ' + resolveUsername(prop['element'] as String) + ': <pre>' + (prop['score'] as Integer) + '</pre>\n'
                    index++
                }
            }

            top3 = top3 + '\nЦитаты больше всего интересуют:\n'

            index = 1
            topThreeQuotes.each { tup ->
                tup.each { prop ->
                    top3 = top3 + index + '. ' + resolveUsername(prop['element'] as String) + ': <pre>' + (prop['score'] as Integer) + '</pre>\n'
                    index++
                }
            }

            top3 = top3 + '\nНу а самые любопытные здесь:\n'

            index = 1
            topThreeOverall.each { tup ->
                tup.each { prop ->
                    top3 = top3 + index + '. ' + resolveUsername(prop['element'] as String) + ': <pre>' + (prop['score'] as Integer) + '</pre>\n'
                    index++
                }
            }
        }

        return head + contestText + unique + loc + total + top3
    }
}