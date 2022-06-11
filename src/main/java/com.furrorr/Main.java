package com.furrorr; 

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new EchoBot());
    }

    static class EchoBot extends TelegramLongPollingBot {

        @Override
        public String getBotUsername() {
            return "Echo Bot";
        }

        @Override
        public String getBotToken() {
            return "5458232572:AAHCkXzDuIJpxjWRMWPUsE75lsqtcGiQI4s";
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
