package eu.zujev.milovidobot

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

class Main {
    static void main(String[] args) {
        //def defaultPathBase = new File( "." ).getCanonicalPath()
        //println "Current dir:" + defaultPathBase
        def config = new ConfigSlurper().parse(new File('config.groovy').toURL())

        def db_address = config.db_address
        def db_port = config.db_port

        DB.connect(db_address, db_port)

        //ApiContextInitializer.init()
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class)

        println "Milovidobot started. Waiting for commands."

        try {
            botsApi.registerBot(new Milovidobot())
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }
    }
}
