package com.furrorr; 

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final String TOKEN = "5458232572:AAHCkXzDuIJpxjWRMWPUsE75lsqtcGiQI4s";
    private static final ConcurrentHashMap<PomodoroBot.Timer, Long> userTimers = new ConcurrentHashMap();

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        PomodoroBot bot = new PomodoroBot();
        telegramBotsApi.registerBot(bot);
        new Thread(() -> {
            try {
                bot.checkTimer();
            } catch (InterruptedException e) {
                System.out.println("Oopps");
            }
        }).run();
    }

    //----------------------------------------------------------------------------------------

    static class PomodoroBot extends TelegramLongPollingBot {

        enum TimerType {
            WORK,
            BREAK
        }

        static record Timer(Instant time, TimerType timerType) { };

        @Override
        public String getBotUsername() {
            return "Pomodoro Bot";
        }

        @Override
        public String getBotToken() {
            return TOKEN;
        }

        @Override
        public void onUpdateReceived(Update update) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Long chatId = update.getMessage().getChatId();
                if (update.getMessage().getText().equals("/start")) {
                    sendMsg(printHelpMessage(), chatId.toString());
                } else {

                    // считали пользовательский ввод, после чего разделили на разные аргументы через пробел
                    var args = update.getMessage().getText().split(" ");

                    // если есть хотя-бы один аргумент
                    if (args.length >= 1) {

                        // то мы пытаемся прочитать его как число, а после прибавляем к текущему времени
                        var workTime = Instant.now().plus(Long.parseLong(args[0]), ChronoUnit.MINUTES);

                        // после создаем запись в хранилище, что есть таймер, который должен сработать в
                        // определенное время
                        userTimers.put(new Timer(workTime, TimerType.WORK), chatId);

                        sendMsg("Выбранные параметры: \nРабота - " + args[0] + " min \nОтдых - " + args[1] + " min",
                                chatId.toString());

                        sendMsg("Время работы пошло!",
                                chatId.toString());

                        // если пользователь ввел два и более аргументов
                        if (args.length >= 2) {
                            var breakTime = workTime.plus(Long.parseLong(args[1]), ChronoUnit.MINUTES);
                            userTimers.put(new Timer(breakTime, TimerType.BREAK), chatId);
                        }
                    }
                }
            }
        }



        public void checkTimer() throws InterruptedException {
            while (true) {
                System.out.println("Количество таймеров пользователей " + userTimers.size());
                userTimers.forEach((timer, userId) -> {
                    System.out.printf("Проверка userId = %d, server_time = %s, user_timer = %s\n",
                            userId, Instant.now().toString(), timer.time.toString());
                    if (Instant.now().isAfter(timer.time)) {
                        userTimers.remove(timer);
                        switch (timer.timerType) {
                            case WORK -> sendMsg("Время отдыха пошло!", userId.toString());
                            case BREAK -> sendMsg("Таймер завершен.", userId.toString());
                        }
                    }
                });
                Thread.sleep(1000);
            }
        }

        private void sendMsg(String text, String chatId) {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setProtectContent(true);
            msg.setText(text);

            try {
                execute(msg);
            } catch (TelegramApiException e) {
                System.out.println("Oopps...");
            }
        }

        private static String printHelpMessage() {

            String text = "Привет! Добро пожаловать в программу Pomodorro!";
            return text;
        }
    }

    //----------------------------------------------------------------------------

    static class EchoBot extends TelegramLongPollingBot {

        @Override
        public String getBotUsername() {
            return "Echo Bot";
        }

        @Override
        public String getBotToken() {
            return TOKEN;
        }

        @Override
        public void onUpdateReceived(Update update) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                if (update.getMessage().getText().equals("/start")) {
                    sendMsg("I am Echo Bot!",
                            update.getMessage().getChatId().toString());
                } else {
                    sendMsg("Echo Bot say \"" + update.getMessage().getText() + "\"",
                            update.getMessage().getChatId().toString());
                }
            }
        }

        private void sendMsg(String text, String chatId) {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setProtectContent(true);
            msg.setText(text);

            try {
                execute(msg);
            } catch (TelegramApiException e) {
                System.out.println("Oopps...");
            }
        }
    }
}
