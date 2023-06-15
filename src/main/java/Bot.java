import commands.AppBotCommand;
import commands.BotCommonCommands;
import function.FilterOperation;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.PhotoMessageUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "testSinergia_bot";
    }

    @Override
    public String getBotToken() {
        return "5824152753:AAHtOWNoFfIFHAndP6SVry0LTf-lRJex7CY";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        try {
            String response = runCommand(message.getText());
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(response);
            execute(sendMessage);
        } catch (InvocationTargetException | IllegalAccessException | TelegramApiException e) {
            e.printStackTrace();
        }

//        try {
//            ArrayList<String> photoPaths = new ArrayList<>(PhotoMessageUtils.savePhotos(getFilesByMessage(message), getBotToken()));
//            for (String path : photoPaths) {
//                PhotoMessageUtils.processingImage(path);
//                execute(preparePhotoMessage(path,chatId));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private String runCommand(String text) throws InvocationTargetException, IllegalAccessException {
        BotCommonCommands commands = new BotCommonCommands();
        Method[] classMethods = commands.getClass().getDeclaredMethods();

        for (Method method : classMethods) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                AppBotCommand command = method.getAnnotation(AppBotCommand.class);
                if (command.name().equals(text)) {
                    method.setAccessible(true);
                    return (String) method.invoke(commands);
                }
            }
        }
        return null;
    }


    private List<org.telegram.telegrambots.meta.api.objects.File> getFilesByMessage(Message message) {
        List<PhotoSize> photoSizes = message.getPhoto();
        ArrayList<org.telegram.telegrambots.meta.api.objects.File> files = new ArrayList<>();
        for (PhotoSize photoSize : photoSizes) {
            final String fileId = photoSize.getFileId();
            try {
                files.add(sendApiMethod(new GetFile(fileId)));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    private SendPhoto preparePhotoMessage(String LocalPath, String chatId) {
        SendPhoto sendPhoto = new SendPhoto();

        sendPhoto.setReplyMarkup(getKeyboard());
        sendPhoto.setChatId(chatId);
        InputFile newFile = new InputFile();
        newFile.setMedia(new File(LocalPath));
        sendPhoto.setPhoto(newFile);
        return sendPhoto;
    }

    private ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> allKeyboardRows = new ArrayList<>();
        allKeyboardRows.addAll(getKeyboardsRows(BotCommonCommands.class));
        allKeyboardRows.addAll(getKeyboardsRows(FilterOperation.class));

        replyKeyboardMarkup.setKeyboard(allKeyboardRows);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;

    }

    private ArrayList<KeyboardRow> getKeyboardsRows(Class someClass) {
        Method[] classMethods = someClass.getDeclaredMethods();
        ArrayList<AppBotCommand> commands = new ArrayList<>();
        for (Method method : classMethods) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                commands.add(method.getAnnotation(AppBotCommand.class));
            }
        }
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        int columnCount = 3;
        int rowsCount = commands.size() / columnCount + ((commands.size() % columnCount == 0) ? 0 : 1);
        for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
            KeyboardRow row = new KeyboardRow();
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int index = rowIndex * columnCount + columnIndex;
                if (index >= commands.size()) continue;
                AppBotCommand command = commands.get(rowIndex * columnCount + columnIndex);
                KeyboardButton keyboardButton = new KeyboardButton(command.name());
                row.add(keyboardButton);
            }
            keyboardRows.add(row);
        }

        return keyboardRows;
    }

}




