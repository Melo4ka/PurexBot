package ru.meldren.purexbot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.meldren.purexbot.command.Command;
import ru.meldren.purexbot.command.commands.HelpCommand;
import ru.meldren.purexbot.command.commands.SheetCommand;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Meldren on 13/06/2021
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class PureXBot extends TelegramLongPollingBot {

    private static final Map<String, String> PREFIXES = new HashMap<>() {
        {
            put("BOT", "Бот");
            put("CMD", "⚙️");
        }
    };

    List<Command> commands = new ArrayList<>();

    public PureXBot() {
        registerCommands();
        createCommandsList();

        System.out.println("PurexBot has started! Have a nice results :3");
    }

    /**
     * Возвращает префикс, который может быть использован при отправке сообщений
     * @param prefix Идентификатор префикса или, в случае не нахождения такого префикса,
     *               используется переданный аргумент
     * @return Префикс в формате "[Префикс] "
     */
    public static String getPrefix(String prefix) {
        return String.format("[%s] ", PREFIXES.getOrDefault(prefix, prefix));
    }

    /**
     * Инициализирует все команды для дальнейшего использования
     */
    private void registerCommands() {
        commands = List.of(
                new HelpCommand(this),
                new SheetCommand(this)
        );

        System.out.printf("The number of registered commands is %d.\n", commands.size());
    }

    /**
     * Инициализирует список команд, которые будут выводиться в
     * качестве помощи в самом боте
     */
    private void createCommandsList() {
        try {
            execute(SetMyCommands.builder()
                    .commands(commands.stream().map(command ->
                            new BotCommand(command.getId(), command.getDescription()))
                            .collect(Collectors.toList()))
                    .build());
        } catch (TelegramApiException ex) {
            System.out.println("A major error has occurred: commands list wasn't created!");
            ex.printStackTrace();
        }
    }

    /**
     * Возвращает название бота
     * @return Название босса
     */
    @Override
    public String getBotUsername() {
        return "PurexBot";
    }

    /**
     * Возвращает токен бота, сгенерированный BotFather
     * @return Токен бота
     */
    @Override
    public String getBotToken() {
        return "1874777296:AAG1ZN-jfT_x_3EfwtZm_PLxI88LmMYB2YU";
    }

    /**
     * Вызывается при каком-либо обновлении (отправки боту
     * сообщения, колбэка и т.д.)
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        } else if (message.hasText()) {
            if (message.getText().startsWith("/")) {
                handleCommand(message);
            } else {
                handleText(message);
            }
        }
    }

    /**
     * Внутренний метод для обработки команды, отправленной боту пользователем
     * @param message Сообщение
     */
    private void handleCommand(Message message) {
        String text = message.getText().toLowerCase().replaceFirst("/", "");
        String[] split = text.split(" ");

        Runnable nullCommand = () -> {
            try {
                execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text(getPrefix("CMD") + "Введенной вами команды не существует!")
                        .replyMarkup(getHelpKeyboard())
                        .build());
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        };

        commands.stream()
                .filter(command -> command.getId().equals(split[0])
                        || command.getAliases().contains(split[0]))
                .findFirst()
                .ifPresentOrElse(command -> command.execute0(message.getFrom(),
                        message.getChat(), Arrays.copyOfRange(split, 1, split.length)),
                        nullCommand);
    }

    /**
     * Вызывается при отправке боту сообщения, не являющегося командой
     * Служит для координирования пользователей, которые не знают функций бота
     * @param message Сообщение
     */
    private void handleText(Message message) {
        try {
            execute(SendMessage.builder()
                    .chatId(message.getChatId().toString())
                    .text(String.format("%s%s, нажмите на кнопку, чтобы увидеть доступные вам команды.",
                            getPrefix("CMD"), message.getFrom().getFirstName()))
                    .replyMarkup(getHelpKeyboard())
                    .build());
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Создает клавиатуру, нажав на которую пользователю будет
     * выведен список доступных ему команд
     * @return Клавиатура
     */
    private InlineKeyboardMarkup getHelpKeyboard() {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text("Список команд")
                .callbackData(new JSONObject().put("id", "help").toString())
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(button))
                .build();
    }

    /**
     * Внутренний метод для обработки приходящего колбэк-запроса
     * @param query Запрос
     */
    private void handleCallbackQuery(CallbackQuery query) {
        JSONObject data = new JSONObject(query.getData());

        commands.stream()
                .filter(command -> command.getId().equals(data.getString("id")))
                .findFirst()
                .ifPresent(command -> command.handleCallbackQuery(query, data));
    }
}
