package com.epam.rd.telegrambot;

import com.epam.rd.telegrambot.dictionary.AvailableOptions;
import com.epam.rd.telegrambot.dictionary.BotCommand;
import com.epam.rd.telegrambot.dictionary.MessageText;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    private Service service;

    /**
     * Метод для приема сообщений.
     *
     * @param update Содержит сообщение от пользователя.
     */
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        if (update.hasMessage() && message.isCommand()) {
            processCommand(message);
            return;
        }

        try {
            if (update.hasMessage()) {
                sendMessageWithPossibleOptions(message);
            } else if (update.hasCallbackQuery()) {
                processCallBackQuery(update.getCallbackQuery().getData(), update.getCallbackQuery().getMessage());
            }
        } catch (GeneralSecurityException | IOException | TelegramApiException e) {
            sendMessageWithCustomText(update.getCallbackQuery().getMessage(), MessageText.ERROR_MESSAGE);
            log.error("Произошла ошибка, при попытке обработать сообщение/callBackQuery", e);
        }
    }

    private void processCommand(Message message) {
        if (message.getText().equals(BotCommand.START_COMMAND)) {
            sendMessageWithCustomText(message, MessageText.WELCOME_MESSAGE);
        }
    }

    public void sendMessageWithPossibleOptions(Message message) throws TelegramApiException {
        if (!studentExist(message)) {
            sendErrorMessage(message);
        } else {
            SendMessage sendMessage = new SendMessage();
            prepareReplayMessage(sendMessage, message);
            sendMessage.setText(MessageText.CHOOSE_NECESSARY_OPTION);
            sendButtons(sendMessage);
            execute(sendMessage);
        }
    }

    public void processCallBackQuery(String data, Message message) throws IOException, GeneralSecurityException, TelegramApiException {
        if (data.equals(AvailableOptions.GET_ALL_MARKS)) {
            sendMsgWithAllMarks(message.getReplyToMessage());
        } else if (data.equals(AvailableOptions.GET_LAST_CHECKED_TOPIC_AND_MARK)) {
            sendMsgWithLastMark(message.getReplyToMessage());
        }
    }

    public void sendMsgWithAllMarks(Message message) throws IOException, GeneralSecurityException, TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        prepareReplayMessage(sendMessage, message);
        sendMessage.setText(service.getAllMarks(message.getText()));
        execute(sendMessage);
    }

    public void sendMsgWithLastMark(Message message) throws IOException, GeneralSecurityException, TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        prepareReplayMessage(sendMessage, message);
        sendMessage.setText(service.getLastCheckedMarkAndTopic(message.getText()));
        sendButtons(sendMessage);
        execute(sendMessage);
    }

    public void sendErrorMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        prepareReplayMessage(sendMessage, message);
        sendMessage.setText(MessageText.REPEAT_NAME);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Возникла ошибка при отправке error message", e);
        }
    }

    public void sendMessageWithCustomText(Message message, String messageText) {
        SendMessage sendMessage = new SendMessage();
        prepareReplayMessage(sendMessage, message);
        sendMessage.setText(messageText);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Возникла ошибка при отправке сообщения с кастомным текстом",e);
        }
    }

    public boolean studentExist(Message message) {
        boolean result = false;
        try {
            result = Logic.findStudentByName(message.getText()) != 0;
        } catch (IOException | GeneralSecurityException | TelegramApiException e) {
            sendErrorMessage(message);
            log.error("Возникла ошибка при проверке сущевствования студента", e);
        }
        return result;
    }

    public void sendButtons(SendMessage sendMessage) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Последняя провереная тема");
        inlineKeyboardButton1.setCallbackData(AvailableOptions.GET_LAST_CHECKED_TOPIC_AND_MARK);

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Все оценки");
        inlineKeyboardButton2.setCallbackData(AvailableOptions.GET_ALL_MARKS);

        List<InlineKeyboardButton> keyboardRowList = new ArrayList<>();
        keyboardRowList.add(inlineKeyboardButton1);
        keyboardRowList.add(inlineKeyboardButton2);

        List<List<InlineKeyboardButton>> lists = new ArrayList<>();
        lists.add(keyboardRowList);

        inlineKeyboardMarkup.setKeyboard(lists);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

    }

    private static void prepareReplayMessage(SendMessage sendMessage, Message message) {
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

}
