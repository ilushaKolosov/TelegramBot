import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CryptoRateBot extends TelegramLongPollingBot {

    private final String botToken = "7818272832:AAHqu4odyDqgTZyWp8xSnhSwf6EDrzevrX4";
    private final String botUsername = "arbitration_crypto_dpr_bot";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Обработка команды /start
            if (messageText.equals("/start")) {
                sendMessage(chatId, "Привет! Я ваш Telegram-бот. Используйте команду /help, чтобы узнать о моих возможностях.");
            }
            // Обработка команды /rate
            else if (messageText.equals("/rate")) {
                String rate = getUSDTtoRUBRate();
                sendMessage(chatId, "Текущий курс USDT/RUB: " + rate);
            }
            // Обработка команды /buy
            else if (messageText.equals("/buy")) {
                String buyRate = getBuyRate();
                sendMessage(chatId, "Купить 1 USDT = " + buyRate + " RUB\n");
            }
            // Обработка команды /sell
            else if (messageText.equals("/sell")) {
                String sellRate = getSellRate();
                sendMessage(chatId, "Продать 1 USDT = " + sellRate + " RUB");
            }
            // Обработка неизвестной команды
            else {
                sendMessage(chatId, "Я не понимаю эту команду. Вот доступные команды:\n" +
                        "/start - Приветствие и информация о боте\n" +
                        "/rate - Узнать спотовый курс USDT/RUB\n" +
                        "/buy - Узнать курс покупки USDT\n" +
                        "/sell - Узнать курс продажи USDT");
            }
        }
    }

    private String getSellRate() {
        String spotRateStr = getUSDTtoRUBRate();
        BigDecimal spotRate = new BigDecimal(spotRateStr.split(" ")[0]);
        BigDecimal sellRate = spotRate.multiply(BigDecimal.valueOf(0.985)).setScale(2, RoundingMode.CEILING);
        return sellRate.toString();
    }


    private String getUSDTtoRUBRate() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.coinbase.com/v2/prices/USDT-RUB/spot";

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String jsonData = response.body().string();
                JSONObject jsonObject = new JSONObject(jsonData);
                String amountStr = jsonObject.getJSONObject("data").getString("amount");

                BigDecimal amount = new BigDecimal(amountStr);
                BigDecimal roundedAmount = amount.setScale(2, RoundingMode.CEILING);

                return roundedAmount + " RUB";
            } else {
                return "Ошибка при получении данных.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при выполнении запроса.";
        }
    }

    private String getBuyRate() {
        String spotRateStr = getUSDTtoRUBRate();
        BigDecimal spotRate = new BigDecimal(spotRateStr.split(" ")[0]);
        BigDecimal sellRate = spotRate.multiply(BigDecimal.valueOf(1.015)).setScale(2, RoundingMode.CEILING);
        return sellRate.toString();
    }


    private void sendMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder().chatId(chatId).text(text).build();

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
