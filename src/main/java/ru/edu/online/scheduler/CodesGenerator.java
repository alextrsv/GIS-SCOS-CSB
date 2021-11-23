package ru.edu.online.scheduler;

import java.util.Random;

/**
 * Класс - генератор кодов для wiegand - 34
 */
public class CodesGenerator {

    /**
     * Получить коды для wiegand-34
     * @param numberOfCodes количество кодов
     * @return миссив кодов
     */
    public static String[] getWiegand34CodesFromInteger(int numberOfCodes) {
        String[] codesFromInteger = new String[numberOfCodes];
        int startCode = getRandomInt();

        // Иногда, по совершенно непонятной мне причине, генерируется int с длинной 31 бит.
        // Цикл ниже фиксит эту проблему.
        while (Integer.toBinaryString(startCode).length() != 32) {
            startCode = getRandomInt();
        }
        for (int i = 0; i < numberOfCodes; i++) {
            codesFromInteger[i] = transformCodeForWiegand34(Integer.toBinaryString(startCode));
            startCode++;
        }

        return codesFromInteger;
    }

    /**
     * Метод для получения случайного int. Для упрощения кода
     * @return случайное число типа int
     */
    private static int getRandomInt() {
        return new Random().nextInt();
    }

    /**
     * Трансформация 32-битного кода из int в 34-битный код для wiegand-34
     * По сути метод довольно коряво рассчитывает биты чётности
     * @param code код для преобразования
     * @return код для wiegand-34
     */
    private static String transformCodeForWiegand34(String code) {

        char firstBit;
        char lastBit;
        // Количество единиц
        int onesCount = 0;
        for (int j = 0; j < code.length() / 2; j++) {
            if (code.toCharArray()[j] == '1') {
                onesCount++;
            }
        }
        if (onesCount % 2 == 0) {
            firstBit = '0';
        } else {
            firstBit = '1';
        }

        onesCount = 0;

        for (int j = 0; j < code.length() / 2; j++) {
            if (code.toCharArray()[j + code.length() / 2] == '1') {
                onesCount++;
            }
        }
        if (onesCount % 2 == 0) {
            lastBit = '0';
        } else {
            lastBit = '1';
        }

        code = addToBeginAndEnd(String.valueOf(firstBit), String.valueOf(lastBit), code);

        return code;
    }

    /**
     * Добавление битов чётности в начало и конец кода
     * @param begin символ, для вставки в начало кода
     * @param end символ, для вставки в конец кода
     * @param code код
     * @return код со вставленными битами коррекции
     */
    private static String addToBeginAndEnd(String begin, String end, String code) {
        return String.join("", begin, code, end);
    }
}