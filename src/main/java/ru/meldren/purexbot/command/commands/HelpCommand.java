package ru.meldren.purexbot.command.commands;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.meldren.purexbot.PureXBot;
import ru.meldren.purexbot.command.Command;

/**
 * Created by Meldren on 25/11/2021
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HelpCommand extends Command {

    public HelpCommand(PureXBot bot) {
        super(bot, "help", "Выводит список всех команд с описанием.");
    }

    @Override
    public void execute(User user, Chat chat, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder(PureXBot.getPrefix("CMD"));

        sb.append("Список всех доступных вам команд:\n");
        getBot().getCommands().stream()
                .filter(command -> command.hasAccess(user))
                .forEach(command -> sb.append("• /")
                        .append(command.getId())
                        .append(" - ")
                        .append(command.getDescription())
                        .append("\n")
                );

        getBot().execute(SendMessage.builder()
                .chatId(chat.getId().toString())
                .text(sb.toString())
                .build());
    }

    @Override
    public void handleCallbackQuery(CallbackQuery query, JSONObject data) {
        try {
            execute(query.getFrom(), query.getMessage().getChat(), new String[] {});
        } catch (Exception ignored) {}
    }
}
