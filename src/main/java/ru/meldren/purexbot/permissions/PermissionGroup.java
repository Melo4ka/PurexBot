package ru.meldren.purexbot.permissions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * Created by Meldren on 22/11/2021
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public enum PermissionGroup {

    DEFAULT("Пользователь"),
    VIP("VIP"),
    OWNER("Владелец");

    String name;
    Set<String> ids;

    /**
     * Позволяет создать группу, при обладании которой
     * пользователю будет доступно больше возможностей
     * @param name Название группы
     * @param ids Идентификаторы пользователей в Telegram,
     *            которые будут иметь доступ к этой группе
     */
    PermissionGroup(String name, String... ids) {
        this.name = name;
        this.ids = Set.of(ids);
    }
}
