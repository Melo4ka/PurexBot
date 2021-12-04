package ru.meldren.purexbot.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.meldren.purexbot.PureXBot;
import ru.meldren.purexbot.permissions.PermissionGroup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class Command {

    String id, description, usage;
    Set<String> aliases;
    PermissionGroup group;
    PureXBot bot;

    public Command(PureXBot bot, String id, String description, String usage, PermissionGroup group, String... aliases) {
        this.id = id;
        this.description = description;
        this.group = group;
        this.usage = String.format("/%s%c%s", id, usage == null ? Character.MIN_VALUE : ' ', usage);
        this.aliases = Set.of(aliases);
        this.bot = bot;
    }

    public Command(PureXBot bot, String id, String description, String usage, String... aliases) {
        this(bot, id, description, usage, PermissionGroup.DEFAULT, aliases);
    }

    public Command(PureXBot bot, String id, String description, String... aliases) {
        this(bot, id, description, null, PermissionGroup.DEFAULT, aliases);
    }

    public Command(PureXBot bot, String id, String description, PermissionGroup group, String... aliases) {
        this(bot, id, description, null, PermissionGroup.DEFAULT, aliases);
    }

    /**
     * Отправляет пользователю сообщение с описанием и синтаксисом введенной команды
     * @param chat Чат, в который будет отправлено сообщение
     */
    public void printHelp(Chat chat) {
        StringBuilder sb = new StringBuilder(PureXBot.getPrefix("CMD"));

        sb.append("Справка по команде /").append(getId()).append(":\n");
        sb.append("• Описание: ").append(getDescription()).append("\n");
        sb.append("• Использование: ").append(getUsage()).append("\n");
        help().forEach((k, v) -> sb.append("• ").append(k).append(": ").append(v).append("\n"));

        try {
            getBot().execute(SendMessage.builder()
                    .chatId(chat.getId().toString())
                    .text(sb.toString())
                    .parseMode("html")
                    .build());
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Добавляет дополнительную информацию в справку по команде
     * @return Дополнительные строки, выводящиеся в справке по команде
     */
    protected Map<String, Object> help() {
        return new HashMap<>();
    }

    /**
     * Проверяет, есть ли у исполнителя права на использование команды
     * @param user Исполнитель команды
     * @return True, если доступ имеется, иначе False
     */
    public boolean hasAccess(User user) {
        PermissionGroup group = Arrays.stream(PermissionGroup.values())
                .filter(group1 -> group1.getIds().contains(user.getId().toString()))
                .findFirst().orElse(PermissionGroup.DEFAULT);

        return group.compareTo(getGroup()) >= 0;
    }

    /**
     * Реализованный метод класса BotCommand, в котором обрабатывается команда, введенная пользователем
     * @param user Исполнитель команды
     * @param chat Чат бота и пользователя
     * @param args Аргументы, переданные с командой
     */
    public final void execute0(User user, Chat chat, String[] args) {
        if (!hasAccess(user)) {
            try {
                getBot().execute(SendMessage.builder()
                        .chatId(chat.getId().toString())
                        .text(String.format("%sЭту команду можно использовать только с группы <b>%s</b>.",
                                PureXBot.getPrefix("CMD"), getGroup().getName()))
                        .parseMode("html")
                        .build());
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        } else if (args.length > 0 && args[0].equalsIgnoreCase("help") && !getId().equals("help")) {
            printHelp(chat);
        } else {
            try {
                execute(user, chat, args);
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    getBot().execute(SendMessage.builder()
                            .chatId(chat.getId().toString())
                            .text(String.format("%s%s, произошла ошибка при выполнении команды.\nПриносим свои извинения :(",
                                    PureXBot.getPrefix("CMD"), user.getFirstName()))
                            .parseMode("html")
                            .build());
                } catch (TelegramApiException ex2) {
                    ex2.printStackTrace();
                }
            }
        }
    }

    /**
     * Вызывается для вторичной (основной) обработки команды
     * @param user Исполнитель команды
     * @param chat Чат бота и пользователя
     * @param args Аргументы, переданные с командой
     */
    public abstract void execute(User user, Chat chat, String[] args) throws Exception;

    /**
     * Вызывается для обработки колбэк-запроса
     * @param query Запрос, содержащий в себе данные
     * @param data Json-объект, хранящий в себе аргументы запроса
     */
    public void handleCallbackQuery(CallbackQuery query, JSONObject data) {
    }
}
