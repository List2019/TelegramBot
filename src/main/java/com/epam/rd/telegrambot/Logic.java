package com.epam.rd.telegrambot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Logic {

    public abstract String getLastCheckedMarkAndTopic(String studentName) throws IOException, GeneralSecurityException, TelegramApiException;

    public abstract String getAllMarks(String studentName) throws IOException, GeneralSecurityException, TelegramApiException;

    public abstract String getMarkByTopicName(String studentName, String topicName) throws IOException, GeneralSecurityException, TelegramApiException;

    public List<LinkedList<String>> getAllMarkAndTopicByName(String name) throws IOException, GeneralSecurityException, TelegramApiException {

        int rowNumber = findStudentByName(name);
        List<String> marks;
        List<String> topicName;

        List<Object> tempTopicName = GoogleSheets.getValues("J1", "AB1").get(0);
        List<Object> tempMarks = GoogleSheets.getValues("J" + rowNumber, "O" + rowNumber).get(0);

        marks = castListObjectsToString(tempMarks);
        topicName = castListObjectsToString(tempTopicName);

        List<LinkedList<String>> result = new ArrayList<>(new LinkedList<>());

        /**
         * Далее, названия предметов можно будет получить вызвав метод {@code getFist()} ,
         * а оценки вызвав метод {@code getLast()}. Записаны предметы в таком же порядке как и на google docs
         */
        for (int i = 0; i < marks.size(); i++) {
            LinkedList<String> linkedList = new LinkedList<>();
            linkedList.add(topicName.get(i));
            linkedList.add(marks.get(i));
            result.add(linkedList);
        }

        return result;
    }

    /**
     * @param name имя студента
     * @return номер ячейки студента
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static int findStudentByName(String name) throws IOException, GeneralSecurityException, TelegramApiException {
        int counter = 0;
        int rowNumber = 0;
        List<List<Object>> studentNames = GoogleSheets.getValues("B2", "B26");

        for (List<Object> nameFromList : studentNames) {
            if (name.equals(nameFromList.get(0))) {
                //+2 делаем потому что именна начинаются с ячейки B2
                rowNumber = counter + 2;
            }
            counter++;
        }

        return rowNumber;
    }

    private List<String> castListObjectsToString(List<Object> objectList) {
        return objectList.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }
}
