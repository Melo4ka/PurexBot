package ru.meldren.purexbot;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.List;

/**
 * Created by Meldren on 22/11/2021
 */
@UtilityClass
public class GoogleSheetsUtil {

    private static final String API_KEY = "API-KEY";

    /**
     * Создает приложение для взаимодействия с гугл-листами
     * @return Листы
     */
    private static Sheets getSheets() {
        NetHttpTransport transport = new NetHttpTransport.Builder().build();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpRequestInitializer httpRequestInitializer = request ->
                request.setInterceptor(intercepted -> intercepted.getUrl().set("key", API_KEY));

        return new Sheets.Builder(transport, jsonFactory, httpRequestInitializer)
                .setApplicationName("PureX Bot")
                .build();
    }


    /**
     * Получает значения листа по указанному диапазону
     * @param sheetId Идентификатор листа
     * @param range Диапазон получаемых значений
     * @return Массив значений
     */
    public static List<List<Object>> findRangedValues(String sheetId, String range) {
        List<List<Object>> values = null;
        try {
            values = getSheets()
                    .spreadsheets()
                    .values()
                    .get(sheetId, range)
                    .execute()
                    .getValues();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return values;
    }

    /**
     * Получает строку значений листа по указанному диапазону
     * @param sheetId Идентификатор листа
     * @param range Диапазон получаемых значений
     * @return Массив значений
     */
    public static List<Object> findLinedValues(String sheetId, String range) {
        List<List<Object>> values = findRangedValues(sheetId, range);

        return values == null ? null : values.get(0);
    }

    /**
     * Получает одиночное значение листа по указанной ячейке
     * @param sheetId Идентификатор листа
     * @param cell Идентификатор ячейки
     * @return Значение
     */
    public static Object findSingleValue(String sheetId, String cell) {
        List<List<Object>> values = findRangedValues(sheetId, cell);

        return values == null ? null : values.get(0).get(0);
    }
}
