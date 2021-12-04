package ru.meldren.purexbot.command.commands;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.meldren.purexbot.GoogleSheetsUtil;
import ru.meldren.purexbot.PureXBot;
import ru.meldren.purexbot.command.Command;
import ru.meldren.purexbot.permissions.PermissionGroup;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Meldren on 23/11/2021
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SheetCommand extends Command {

    Set<Sheet> sheets = new HashSet<>();

    public SheetCommand(PureXBot bot) {
        super(bot, "sheet", "Позволяет получить баллы из гугло-журналов.", "&ltДисциплина&gt", PermissionGroup.OWNER, "sh");

        new Sheet("informatics", "Информатика", "1fCANz4NEbC2ihSRCRwApspJerCvTrh9Lsj4m7EQ88IQ",
                sheetId -> new LinkedHashMap<>() {
                    {
                        put("Бонусы", GoogleSheetsUtil.findSingleValue(sheetId, "F27"));
                        put("Аннотации", GoogleSheetsUtil.findLinedValues(sheetId, "G27:N27"));
                        put("Посещаемость", GoogleSheetsUtil.findLinedValues(sheetId, "O27:V27"));
                        put("Лабораторные работы (№1-6)", GoogleSheetsUtil.findLinedValues(sheetId, "X27:AC27"));
                        put("<b>Тесты</b>", GoogleSheetsUtil.findLinedValues(sheetId, "AF27:AG27"));
                        put("<b>Экзамен</b>", GoogleSheetsUtil.findSingleValue(sheetId, "AK27"));
                    }
                });

        new Sheet("programming", "Программирование", "1cprCvL1uDB996_m-HjVe8bIEEMGmdpjgJxDUZjq0FeU",
                sheetId -> new LinkedHashMap<>() {
                    {
                        put("Лабораторные работы (№1-2)", GoogleSheetsUtil.findSingleValue(sheetId, "P3109!AD5"));
                        put("Лабораторные работы (№3-4)", GoogleSheetsUtil.findSingleValue(sheetId, "P3109!AE5"));
                        put("Контрольные работы", GoogleSheetsUtil.findSingleValue(sheetId, "P3109!AF5"));
                        put("Зачет", GoogleSheetsUtil.findSingleValue(sheetId, "P3109!AG5"));
                        put("<b>Итого</b>", GoogleSheetsUtil.findSingleValue(sheetId, "P3109!AH5"));
                    }
                });

        new Sheet("csb", "ОПД", "13Dxsm1LRPTsDCOmn9kyEwUEC9BWBsNWLOdRwChiZAkk",
                sheetId -> new LinkedHashMap<>() {
                    {
                        put("Лабораторная работа №1", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!Q4"));
                        put("Лабораторная работа №2", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!V4"));
                        put("Тест №1", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!W4"));
                        put("Тест №2", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!X4"));
                        put("Контрольная работа №1", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!Z4"));
                        put("Личные качества", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!AA4"));
                        put("<b>Допуск</b>", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!AB4"));
                        put("Зачет", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!AC4"));
                        put("<b>Итого</b>", GoogleSheetsUtil.findSingleValue(sheetId, "Р3109!AD4"));
                    }
                });

        new Sheet("english", "Иностранный язык", "1A6hiLkNG5Zl7TVgPzfBKxUlrpHZkaiVfjKxTeeJRrvE",
                sheetId -> new LinkedHashMap<>() {
                    {
                        put("Домашние работы (№1-4)", GoogleSheetsUtil.findLinedValues(sheetId, "BARS!D22:G22"));
                        put("Посещаемость (№1-2)", GoogleSheetsUtil.findLinedValues(sheetId, "BARS!H22:I22"));
                        put("Контрольные работы (№1-4)", GoogleSheetsUtil.findLinedValues(sheetId, "BARS!J22:M22"));
                        put("Личные качества", GoogleSheetsUtil.findSingleValue(sheetId, "BARS!Q22"));
                        put("Тестирование (WR, SP)", GoogleSheetsUtil.findLinedValues(sheetId, "BARS!N22:O22"));
                        put("Зачет", GoogleSheetsUtil.findSingleValue(sheetId, "BARS!AC4"));
                        put("<b>Итого</b>", GoogleSheetsUtil.findSingleValue(sheetId, "BARS!R22"));
                    }
                });
    }

    @Override
    protected Map<String, Object> help() {
        return Collections.singletonMap("Дисциплины", sheets.stream()
                .map(sheet -> sheet.name)
                .collect(Collectors.joining(", ", "", ".")));
    }

    private String getInfo(String discipline) {
        StringBuilder sb = new StringBuilder(PureXBot.getPrefix("CMD"));

        sheets.stream().filter(sheet -> sheet.name.startsWith(discipline.toLowerCase())).limit(1).forEach(sheet -> {
            sb.append("Ваши баллы по дисциплине \"").append(sheet.discipline).append("\":\n");
            sheet.values.apply(sheet.sheetId).entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .forEach(e -> {
                        String key = e.getKey();
                        String value = e.getValue().toString().replaceAll("[\\[\\]]", "");
                        sb.append(key).append(": ").append(value);
                        if (key.toLowerCase().contains("итого")) {
                            try {
                                double points = Double.parseDouble(value.replace(',', '.'));
                                sb.append(" (").append(points > 90 ? "5️⃣" : points > 74 ? "4️⃣" : points >= 60 ? "3️⃣" : "2️⃣").append(")");
                            } catch (NumberFormatException ignored) {}
                        }
                        sb.append("\n");
                    });
        });

        return sb.toString();
    }

    /**
     * Создает клавиатуру, нажав на которую пользователю будет
     * выведена обновленная информация по дисциплине
     * @param discipline Дисциплина
     * @return Клавиатура
     */
    private InlineKeyboardMarkup getKeyboard(String discipline) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text("Обновить")
                .callbackData(new JSONObject()
                        .put("id", getId())
                        .put("discipline", discipline)
                        .toString())
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(button))
                .build();
    }

    @Override
    public void execute(User user, Chat chat, String[] args) throws Exception {
        if (args.length < 1 ||
                sheets.stream().noneMatch(sheet -> sheet.name.startsWith(args[0].toLowerCase()))) {
            printHelp(chat);
        } else {
            getBot().execute(SendMessage.builder()
                    .chatId(chat.getId().toString())
                    .text(getInfo(args[0]))
                    .parseMode("html")
                    .replyMarkup(getKeyboard(args[0]))
                    .build());
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery query, JSONObject data) {
        Message message = query.getMessage();
        String discipline = data.getString("discipline");
        String updatedText = getInfo(discipline);

        String alert;
        if (!updatedText.replaceAll("<.+?>", "").trim().equals(message.getText())) {
            try {
                getBot().execute(EditMessageText.builder()
                        .chatId(message.getChatId().toString())
                        .messageId(message.getMessageId())
                        .text(updatedText)
                        .parseMode("html")
                        .replyMarkup(getKeyboard(discipline))
                        .build());
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }

            alert = "Обновлено! :)";
        } else {
            alert = "Ваши результаты не изменились!";
        }

        try {
            getBot().execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(query.getId())
                    .text(alert)
                    .showAlert(true)
                    .build());
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    protected class Sheet {

        String name, sheetId, discipline;
        Function<String, LinkedHashMap<String, Object>> values;

        public Sheet(String name, String discipline, String sheetId, Function<String, LinkedHashMap<String, Object>> values) {
            this.name = name;
            this.sheetId = sheetId;
            this.discipline = discipline;
            this.values = values;
            sheets.add(this);
        }
    }
}
