package eu.zujev.milovidobot.commmandGroups

import eu.zujev.milovidobot.DB
import eu.zujev.milovidobot.Milovidobot
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Document
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import redis.clients.jedis.Jedis

class FilesHandler extends Milovidobot {
    static Jedis db = DB.getInstance()

    Boolean processUpdate(Update update) {
        def poemsWait = db.get('bulkpoems')
        def quotesWait = db.get('bulkquotes')

        if (poemsWait as Integer == update.getMessage().getFrom().getId()) {
            db.del('bulkpoems')
            downloadAndParseFile(update, 'poems')
        } else if (quotesWait as Integer == update.getMessage().getFrom().getId()) {
            db.del('bulkquotes')
            downloadAndParseFile(update, 'quotes')
        }

        return false
    }

    private String getFilePath(Document file) {
        Objects.requireNonNull(file)

            GetFile getFileMethod = new GetFile()
            getFileMethod.setFileId(file.getFileId())
            try {
                // We execute the method using AbsSender::execute method.
                File newFile = execute(getFileMethod)
                // We now have the file_path
                return newFile.getFilePath()
            } catch (TelegramApiException e) {
                e.printStackTrace()
            }

        return null // Just in case
    }

    private java.io.File downloadByFilePath(String filePath) {
        try {
            // Download the file calling AbsSender::downloadFile method
            return downloadFile(filePath)
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }

        return null
    }

    private void downloadAndParseFile(update, cat) {
        def file = update.getMessage().getDocument()
        def path = getFilePath(file)
        def readyFile
        if (path) {
            readyFile = downloadByFilePath(path)
        } else {
            sendMessage('Что-то не так с файлом, попробуй снова.', update)
            return
        }

        Scanner scanner = new Scanner(readyFile)

        def text = ''
        def addedCnt = 0
        def processedCnt = 0

        while(scanner.hasNext()){
            def line = scanner.nextLine()
            if (line == '>>><<<') {
                text = text.substring(0,text.length()-1)
                if (!db.sismember(cat, text)) {
                    db.sadd(cat, text)
                    addedCnt++
                }
                processedCnt++
                text = ''
            } else {
                text = text + line + '\n'
            }
        }

        sendMessage('Загрузка окончена, загружено ' + addedCnt + ' из ' + processedCnt + ' сущностей.', update)
    }
}
