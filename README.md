# Last Stand

Мод для Minecraft 1.21.1 (NeoForge): вместо мгновенной смерти игрок переходит в состояние «нокдауна» — ползает с тикающим таймером, а товарищи могут его поднять бинтом.

**[Русский](#русский)** | **[English](#english)**

---

## Русский

### Возможности

- **Состояние «нокдаун»**: смертельный урон не убивает сразу — игрок падает и ползает в позе плавания с обратным отсчётом до смерти.
- **Воскрешение бинтом**: другой игрок может поднять лежачего, удерживая ПКМ с бинтом. Поднятие занимает **случайные 30–60 секунд** (загадывается при каждом падении).
- **Прогресс сохраняется**: если поднятие прервали (отпустил клавишу, отошёл, отвернулся), накопленный прогресс не сгорает — можно продолжить позже, хоть другим игроком.
- **Таймер умирающего замораживается**, пока его поднимают; под таймером отображается «Вас поднимает \<ник\> — осталось X с».
- **Прогресс-бар у воскрешающего** над хотбаром: заполнение и точное оставшееся время с сотыми секунды.
- **Настоящая причина смерти**: при истечении таймера смерть происходит от первоначального источника урона — корректное сообщение об убийце и причине.
- **Эскалация по смертям**: каждая **настоящая** смерть увеличивает длительность следующего нокдауна — 1, 5, 10, 15, 20, 25, 30 минут. Успешное воскрешение счётчик не трогает. Окно обнуляется через 24 часа реального времени.
- **Мгновенная смерть для отдельных типов урона**: настраиваемый список в конфиге; по умолчанию — падение в пустоту, лава, утопление и `/kill`.
- **Мобы не агрятся** на лежачего игрока: существующие цели сбрасываются, новые не выбираются.
- **Кинематографичный экран**: пульсирующая кровавая виньетка, таймер `ММ:СС.мс` по центру, звук сердцебиения, тряска камеры, скрытие HUD, принудительный вид от первого лица (F5 заблокирован).
- **Персистентность**: состояние нокдауна и прогресс поднятия сохраняются при выходе из мира и перезаходе.

### Предмет: Бинт

| Свойство | Значение |
|---|---|
| Крафт | 2 бумаги + 1 белая шерсть (бесформенный) → 4 бинта |
| Стак | 4 |
| Вкладка креатива | Бой (Combat) |
| Применение | ПКМ по лежачему игроку → удерживать до конца поднятия |

### Команды

Требуют уровень прав 2 (оператор):

- `/laststand status <игрок>` — состояние, счётчик смертей, оставшееся время
- `/laststand down <игрок>` — принудительно уложить игрока в нокдаун
- `/laststand reset <игрок>` — поднять игрока из нокдауна

### Конфигурация

Файл `config/laststand-common.toml` (сервер):

```toml
# Типы урона, убивающие сразу, минуя нокдаун
instant_kill_damage_types = ["minecraft:out_of_world", "minecraft:lava", "minecraft:drown", "minecraft:generic_kill"]

# Диапазон случайного времени поднятия (секунды)
revive_seconds_min = 30
revive_seconds_max = 60
```

### Требования

- Minecraft **1.21.1**
- NeoForge **21.1.x**
- Клиент и сервер — мод нужен на обеих сторонах (сервер синхронизирует состояние клиентам)

### Сборка из исходников

```bash
./gradlew build        # собрать jar → build/libs/laststand-<version>.jar
```

Для разработки:

```bash
./gradlew runClient    # одиночный клиент (ник Dev)
./gradlew runServer    # выделенный сервер (run-server/)
./gradlew runClient2   # клиент Dev1 → авто-подключение к localhost:25565
./gradlew runClient3   # клиент Dev2 → авто-подключение к localhost:25565
```

Sodium и Iris подключены только для dev-окружения (`localRuntime`) и не требуются конечным пользователям.

### Лицензия

All Rights Reserved.

---

## English

**Last Stand** is a Minecraft 1.21.1 (NeoForge) mod that replaces instant death with a "downed" state: the player crawls on a countdown while teammates can pick them up with a bandage.

### Features

- **Downed state**: normally lethal damage knocks the player down instead of killing them — they crawl in a swimming pose with a death countdown.
- **Bandage revive**: another player can pick a downed player up by holding right-click with a bandage. Reviving takes a **random 30–60 seconds** (rolled each time someone goes down).
- **Progress is kept**: if reviving is interrupted (button released, moved away, looked away), accumulated progress is not lost — it can be resumed later, even by a different player.
- **The downed player's timer freezes** while being revived; below the timer they see "\<name\> is reviving you — X s left".
- **Reviver progress bar** above the hotbar: fill bar plus precise remaining time down to hundredths of a second.
- **Real death cause preserved**: when the timer expires, the player dies from the original damage source — the kill message and killer attribution stay correct.
- **Death escalation**: each *actual* death lengthens the next downed timer — 1, 5, 10, 15, 20, 25, 30 minutes. A successful revive does not increase the counter. The counter resets after a 24-hour real-time window.
- **Instant-kill damage types**: a configurable list bypasses the downed state; defaults are void fall, lava, drowning, and `/kill`.
- **Mobs ignore downed players**: existing targets are released and new ones are never acquired.
- **Cinematic screen**: pulsing blood vignette, centered `MM:SS.mmm` timer, heartbeat sound, camera shake, HUD suppression, forced first-person view (F5 is locked).
- **Persistence**: the downed state and revive progress survive logging out and world saves.

### Item: Bandage

| Property | Value |
|---|---|
| Crafting | 2 paper + 1 white wool (shapeless) → 4 bandages |
| Stack size | 4 |
| Creative tab | Combat |
| Usage | Hold right-click on a downed player until the revive completes |

### Commands

Requires permission level 2 (operator):

- `/laststand status <player>` — state, death counter, remaining time
- `/laststand down <player>` — forcibly put a player into the downed state
- `/laststand reset <player>` — pick a player up from the downed state

### Configuration

File `config/laststand-common.toml` (server):

```toml
# Damage types that kill outright, skipping the downed state
instant_kill_damage_types = ["minecraft:out_of_world", "minecraft:lava", "minecraft:drown", "minecraft:generic_kill"]

# Random revive duration range (seconds)
revive_seconds_min = 30
revive_seconds_max = 60
```

### Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.x**
- Required on both client and server (the server syncs state to clients)

### Building from source

```bash
./gradlew build        # builds the jar → build/libs/laststand-<version>.jar
```

For development:

```bash
./gradlew runClient    # solo client (name: Dev)
./gradlew runServer    # dedicated server (run-server/)
./gradlew runClient2   # client Dev1 → auto-connects to localhost:25565
./gradlew runClient3   # client Dev2 → auto-connects to localhost:25565
```

Sodium and Iris are wired for the dev environment only (`localRuntime`) and are not required by end users.

### License

All Rights Reserved.
