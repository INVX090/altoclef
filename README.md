# AltoClef Fork

Клиентский бот для Minecraft, который играет автоматически.

*Powered by Baritone.*

Форк [MiranCZ/altoclef](https://github.com/MiranCZ/altoclef) с дополнительными возможностями и исправлениями.

## Что нового по сравнению с MiranCLF/altoclef

### Геймплей

- **Паркур разрешён** — обычный, с установкой блоков, по диагонали
- **Инструменты работают до поломки** — кирки, мечи и лопаты используются пока не сломаются (раньше экономили прочность)
- **Посмертное поведение** — после смерти текущая задача отменяется, бот начинает выживание заново (не возвращается в точку смерти)
- **blockPlacementPenalty** — значение 3.0, не перезаписывается при каждом входе

### Terminator Mode

`@test terminate` — полноценный режим Terminator из оригинального altoclef:

- Без снаряжения: убегает от игроков, собирает еду и железные кирки
- Добывание: копает алмазы, собирает алмазное снаряжение
- Охота: ищет игроков в радиусе 900 блоков, атакует
- Отправляет сообщения: "Prepare to get punked, kid"

### Восстановленные команды

- `@punk <имя>` — атаковать конкретного игрока
- `@custom2 <задача>` — выполнить кастомную задачу из конфига

### Тестовые команды

- `@test terminate` — запустить Terminator

## Установка

### Требования

- Minecraft **1.21.1**
- [Fabric Loader](https://fabricmc.net/use/installer/)
- JDK **21**

### Шаги

1. Установи Fabric Loader для Minecraft 1.21.1
2. Скачай `.jar` из [релизов](https://github.com/INVX090/altoclef/releases)
3. Положи в `.minecraft/mods/`
4. Удали старые конфиги Baritone, если они есть (могут конфликтовать)
5. Запусти Minecraft с профилем Fabric

> [!WARNING]
> Не ставь другие моды одновременно с AltoClef — возможны конфликты.

## Команды

Все команды вводятся в чате с префиксом `@`:

| Команда | Описание |
|---------|----------|
| `@help` | Список всех команд |
| `@gamer` | Бот пытается побить игру |
| `@gamer2` | Побить игру + PvP режим (авто-атака игроков рядом) |
| `@get <предмет> <кол-во>` | Собрать предмет |
| `@goto <x> <y> <z>` | Идти к координатам |
| `@follow <игрок>` | Следовать за игроком |
| `@stop` | Остановить текущую задачу |
| `@pause` | Поставить на паузу |
| `@unpause` | Снять паузу |
| `@punk <игрок>` | Атаковать игрока |
| `@test terminate` | Режим Terminator |
| `@inventory` | Показать инвентарь |
| `@status` | Статус бота |
| `@food` | Собрать еду |
| `@meat` | Собрать мясо |
| `@scan <блок>` | Найти ближайший блок |
| `@coords` | Показать координаты |
| `@gamma <значение>` | Установить яркость |
| `@reload` | Перезагрузить настройки |
| `@idle` | Режим ожидания |
| `@deposit` | Сложить предметы в сундук |
| `@stash` | Положить всё в хранилище |
| `@equip <предмет>` | Экипировать предмет |
| `@give <предмет> <кол-во>` | Дать предмет |
| `@custom2 <задача>` | Кастомная задача из конфига |

## Как это работает

- [Гайд из wiki](https://github.com/MiranCZ/altoclef/wiki/1:-Documentation:-Big-Picture)
- [Видео объяснение](https://youtu.be/q5OmcinQ2ck?t=387)

## Сборка из исходников

Требуется JDK 21.

```bash
git clone https://github.com/INVX090/altoclef.git
cd altoclef
./gradlew :1.21.1:build
```

Готовый `.jar` появится в `versions/1.21.1/build/libs/`.

## Инструкция для разработчика (рабочая)

> Эта секция — памятка для будущих сборок и доработок. Не удалять.

### Стек

- Minecraft **1.21.1**, Fabric Loader `0.16.2`
- **JDK 21** (обязателен: `options.release = 21`)
- **Gradle 8.8** (wrapper), **Loom 1.7-SNAPSHOT**
- **Preprocess** plugin `com.replaymod.preprocess:c2041a3` (мультиверсия через `#if MC >= ...`)
- Baritone (`cabaletta:baritone-unoptimized-fabric:1.21.1`)
- Версия мода: `mod_version=0.19` в `gradle.properties`

### Сборка на CI (рекомендуемый путь)

Локальная сборка на Windows может падать (Loom/ZipFS). Рабочий путь — GitHub Actions:

```powershell
# 1. Коммит и пуш
git add -A
git commit -m "feat: ..."
git push origin main

# 2. CI соберёт :1.21.1:build (~3 мин)
#    Смотреть: github.com/INVX090/altoclef/actions

# 3. Скачать артефакт (jar упадёт в корень репо)
cd E:\altocleaf\altoclef-fork
gh run list --limit 1
gh run download <RUN_ID> --name altoclef-mod

# 4. Положить в папку модов
Copy-Item "altoclef-1.21.1-0.19.jar" "C:\Users\invx0\AppData\Roaming\.minecraft\mods\" -Force
```

### Локальная сборка (Windows)

```powershell
# Нужен JDK 21 в JAVA_HOME (сейчас его НЕТ, только JDK 8 и 17)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew.bat :1.21.1:build --no-daemon
```

### PvP режим (как работает)

- `@gamer2` = `Gamer2Command` → `BeatMinecraftTask` + `mobDefenseChain.setPvPMode(true)`
- `MobDefenseChain.findPvPTarget()` ищет ближайшего живого игрока (не креатив/спектатор, не сам бот) в **30 блоках**
- При обнаружении возвращает **priority 75** → прерывает текущую задачу → `KillPlayerTask`
- Когда игрок мёртв/вышел из зоны → priority падает → бот возвращается к задаче
- `doForceField()` автоматически добавляет игроков в KillAura (авто-атака на бегу)

### Ключевые файлы

| Файл | Роль |
|------|------|
| `src/.../chains/MobDefenseChain.java` | PvP-детекция, приоритеты, force field |
| `src/.../tasks/entity/KillPlayerTask.java` | Преследование конкретного игрока |
| `src/.../tasks/entity/AbstractKillEntityTask.java` | Механика атаки (кулдаун ≥ 0.8, без isOnGround) |
| `src/.../tasks/entity/AbstractDoToEntityTask.java` | Сближение; для PlayerEntity без raycast/isOnGround |
| `src/.../control/KillAura.java` | Авто-атака, щит, выбор оружия |
| `src/.../commands/Gamer2Command.java` | Команда `@gamer2` |

### Известные недоработки PvP

- KillAura поднимает щит вместо атаки против игроков (исключение PlayerEntity не добавлено)
- `findPvPTarget` не проверяет линию видимости (LOS) — бот бежит сквозь стены
- Нет проверки `hurtTime` (атака в инвулнерабельность впустую)
- Нет strafe-движения, ломания щитов противника, дальнего боя (лук)
- Нет hysteresis: игрок на границе 30 блоков вызывает дёрганье задача↔PvP

## Оригинал

- Оригинальный altoclef: [gaucho-matrero/altoclef](https://github.com/gaucho-matrero/altoclef) (архив)
- Fork MiranCZ: [MiranCZ/altoclef](https://github.com/MiranCZ/altoclef)

## Лицензия

[Лицензия оригинального проекта](LICENSE)
