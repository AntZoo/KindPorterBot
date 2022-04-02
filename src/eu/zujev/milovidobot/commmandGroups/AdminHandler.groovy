package eu.zujev.milovidobot.commmandGroups

import eu.zujev.milovidobot.DB
import eu.zujev.milovidobot.Milovidobot
import eu.zujev.milovidobot.lib.Func
import org.telegram.telegrambots.meta.api.objects.Update
import redis.clients.jedis.Jedis

class AdminHandler extends Milovidobot {
    static Jedis db = DB.getInstance()

    Boolean processUpdate(Update update) {
        def message_text = update.getMessage().getText()

        def config = new ConfigSlurper().parse(new File('config.groovy').toURL())

        if (config.admins.contains(update.getMessage().getFrom().id)) {
            if (message_text.toLowerCase().startsWith('/новое стихотворение')) {
                println 'adding poem'
                def check = Func.checkLength(update, message_text.substring(11))
                if (!check) {
                    if (db.sadd('poems', message_text.substring(11)) > 0) {
                        sendMessage('Стихотворение добавлено.', update)
                        return true
                    } else {
                        sendMessage(check, update)
                        return false
                    }
                }
            } else if (message_text.toLowerCase().startsWith('/новая цитата')) {
                println 'adding quote'
                def check = Func.checkLength(update, message_text.substring(14))
                if (!check) {
                    if (db.sadd('quotes', message_text.substring(14)) > 0) {
                        sendMessage('Цитата добавлена.', update)
                        return true
                    } else {
                        sendMessage(check, update)
                        return false
                    }
                }
            } else if (message_text.toLowerCase().startsWith('/информация об авторе')) {
                println 'updating author info'
                def check = Func.checkLength(update, message_text.substring(22))
                if (!check) {
                    if (db.set('author', message_text.substring(22)) == 'OK') {
                        sendMessage('Информация об авторе обновлена.', update)
                        return true
                    } else {
                        sendMessage(check, update)
                        return false
                    }
                }
            } else if (message_text.toLowerCase().startsWith('/стихи файлом')) {
                println 'waiting for bulk poems'
                db.setex('bulkpoems',60,update.getMessage().getFrom().getId() as String)
                sendMessage('Теперь мне нужен файл со стихами. Обычный файл формата .txt, где отдельные стихи будут отделены друг от друга строкой вида ">>><<<"', update)
            } else if (message_text.toLowerCase().startsWith('/цитаты файлом')) {
                println 'waiting for bulk quotes'
                db.setex('bulkquotes', 60, update.getMessage().getFrom().getId() as String)
                sendMessage('Теперь мне нужен файл с цитатами. Обычный файл формата .txt, где отдельные цитаты будут отделены друг от друга строкой вида ">>><<<"', update)
//            } else if (message_text.toLowerCase().startsWith('/удалить стих')) {
//                println 'deleting poem'
//                def scanParams = new ScanParams()
//                scanParams.match('*' + message_text.substring(14) + '*')
//                def searchRes = db.sscan('poems', '0', scanParams)
//                if (searchRes.result.size() < 1 && db.srem('poems', searchRes.result[0]) != '0') {
//                    sendMessage('Стихотворение удалено.', update)
//                    return true
//                } else {
//                    sendMessage('Стихотворение не найдено', update)
//                    return false
//                }
//
//            } else if (message_text.toLowerCase().startsWith('/удалить цитату')) {
//                println 'deleting quote'

            } else {
                def poemwait = db.get('waitingforpoem:' + update.getMessage().getFrom().getId())
                if (poemwait) {
                    def poemwaitmsg = db.get('waitingforpoemmsg:' + update.getMessage().getFrom().getId()) as int
                    println 'adding poem'
                    def check = Func.checkLength(update, update.getMessage().getText())
                    if (!check) {
                        if (db.sadd('poems', update.getMessage().getText()) > 0) {
                            def trimmedMsg = update.getMessage().getText()
                            if (trimmedMsg.length() > 4070) {
                                trimmedMsg = trimmedMsg.substring(0,4070)
                            }
                            editMessage('Стихотворение добавлено:\n\n' + trimmedMsg,
                                    poemwait as String,
                                    poemwaitmsg,
                                    Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                        } else {
                            editMessage('Что-то не так с бд. Напиши @AntZoo, пусть поправит.',
                                    poemwait as String,
                                    poemwaitmsg,
                                    Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                        }
                    } else {
                        editMessage(check,
                                    poemwait as String,
                                    poemwaitmsg,
                                    Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                    }
                    deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId())
                    db.del('waitingforpoem:' + update.getMessage().getFrom().getId())
                    db.del('waitingforpoemmsg:' + update.getMessage().getFrom().getId())
                    return true
                } else {
                    def quotewait = db.get('waitingforquote:' + update.getMessage().getFrom().getId())
                    if (quotewait) {
                        def quotewaitmsg = db.get('waitingforquotemsg:' + update.getMessage().getFrom().getId()) as int
                        println 'adding quote'
                        def check = Func.checkLength(update, update.getMessage().getText())
                        if (!check) {
                            if (db.sadd('quotes', update.getMessage().getText()) > 0) {
                                def trimmedMsg = update.getMessage().getText()
                                if (trimmedMsg.length() > 4077) {
                                    trimmedMsg = trimmedMsg.substring(0,4077)
                                }
                                editMessage('Цитата добавлена:\n\n' + trimmedMsg,
                                        quotewait as String,
                                        quotewaitmsg,
                                        Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                            } else {
                                editMessage('Что-то не так с бд. Напиши @AntZoo, пусть поправит.',
                                        quotewait as String,
                                        quotewaitmsg,
                                        Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                            }
                        } else {
                            editMessage(check,
                                    quotewait as String,
                                        quotewaitmsg,
                                        Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                        }
                        deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId())
                        db.del('waitingforquotemsg:' + update.getMessage().getFrom().getId())
                        db.del('waitingforquote:' + update.getMessage().getFrom().getId())
                        return true
                    } else {
                        def authorwait = db.get('waitingforauthor:' + update.getMessage().getFrom().getId())
                        if (authorwait) {
                            def authorwaitmsg = db.get('waitingforauthormsg:' + update.getMessage().getFrom().getId()) as int
                            println 'adding author'
                            def check = Func.checkLength(update, update.getMessage().getText())
                            if (!check) {
                                if (db.set('author', update.getMessage().getText()) == 'OK') {
                                    def trimmedMsg = update.getMessage().getText()
                                    if (trimmedMsg.length() > 4064) {
                                        trimmedMsg = trimmedMsg.substring(0,4064)
                                    }
                                    editMessage('Информация об авторе изменена:\n\n' + trimmedMsg,
                                            authorwait as String,
                                            authorwaitmsg,
                                            Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                                } else {
                                    editMessage('Что-то не так с бд. Напиши @AntZoo, пусть поправит.',
                                            authorwait as String,
                                            authorwaitmsg,
                                            Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                                }
                            } else {
                                editMessage(check,
                                        authorwait as String,
                                        authorwaitmsg,
                                        Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                            }
                            deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId())
                            db.del('waitingforauthormsg:' + update.getMessage().getFrom().getId())
                            db.del('waitingforauthor:' + update.getMessage().getFrom().getId())
                            return true
                        } else {
                            def donatewait = db.get('waitingfordonate:' + update.getMessage().getFrom().getId())
                            if (donatewait) {
                                def donatewaitmsg = db.get('waitingfordonatemsg:' + update.getMessage().getFrom().getId()) as int
                                println 'adding donate'
                                def check = Func.checkLength(update, update.getMessage().getText())
                                if (!check) {
                                    if (db.set('donate', update.getMessage().getText()) == 'OK') {
                                        def trimmedMsg = update.getMessage().getText()
                                        if (trimmedMsg.length() > 4064) {
                                            trimmedMsg = trimmedMsg.substring(0, 4064)
                                        }
                                        editMessage('Информация об донате изменена:\n\n' + trimmedMsg,
                                                donatewait as String,
                                                donatewaitmsg,
                                                Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                                    } else {
                                        editMessage('Что-то не так с бд. Напиши @AntZoo, пусть поправит.',
                                                donatewait as String,
                                                donatewaitmsg,
                                                Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                                    }
                                } else {
                                    editMessage(check,
                                            donatewait as String,
                                            donatewaitmsg,
                                            Func.getKeyboard(update.getMessage().getFrom().getId()), null)
                                }
                                deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId())
                                db.del('waitingfordonatemsg:' + update.getMessage().getFrom().getId())
                                db.del('waitingfordonate:' + update.getMessage().getFrom().getId())
                                return true
                            }
                        }
                    }
                }
            }
            return false
        }
    }
}
