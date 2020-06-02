package com.epam.rd.telegrambot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;

@org.springframework.stereotype.Service
public class Service extends Logic {

    public String getLastCheckedMarkAndTopic(String studentName) throws IOException, GeneralSecurityException, TelegramApiException {
        String result = null;
        List<LinkedList<String>> allMarksAndTopic = getAllMarkAndTopicByName(studentName);
        for (LinkedList<String> linkedList : allMarksAndTopic) {
            if (!linkedList.getLast().equals("") && Character.isDigit(linkedList.getLast().charAt(0))) {
                result = linkedList.getFirst() + " " + linkedList.getLast();
            }
        }
        return result;
    }

    public String getAllMarks(String studentName) throws IOException, GeneralSecurityException, TelegramApiException {
        StringBuilder stringBuilder = new StringBuilder();
        List<LinkedList<String>> allMarksAndTopic = getAllMarkAndTopicByName(studentName);
        for (LinkedList<String> linkedList : allMarksAndTopic) {
            stringBuilder.append(linkedList.getFirst()).append(" ").append(linkedList.getLast()).append("\n");
        }

        return stringBuilder.toString();
    }

    public String getMarkByTopicName(String studentName, String topicName) throws IOException, GeneralSecurityException, TelegramApiException {
        String result = null;
        List<LinkedList<String>> allMarksAndTopic = getAllMarkAndTopicByName(studentName);
        for (LinkedList<String> linkedList : allMarksAndTopic) {
            if (linkedList.getFirst().equals(topicName)) {
                result = linkedList.getLast();
            }
        }
        return result;
    }

}
