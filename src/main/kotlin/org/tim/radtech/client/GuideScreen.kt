package org.tim.radtech.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class GuideScreen : Screen(Component.literal("RadTech Guide")) {

    private var selectedCategory = 0
    private var scrollOffset = 0

    data class GuideTopic(val icon: String, val title: String, val header: String, val content: List<String>)

    private val topics = listOf(
        GuideTopic(
            "⚡", "Основи енергії", "ЯК ПРАЦЮЄ ЕЛЕКТРИКА В RADTECH",
            listOf(
                "§eВся енергія в моді базується на реальних фізичних величинах:",
                "",
                "§b• Напруга (Вольти, В): §fТиск струму в мережі.",
                "  Промисловий стандарт: §a380 Вольт§f.",
                "  Лінії передачі: §a10 000 Вольт§f.",
                "",
                "§e• Сила струму (Ампери, А): §fКількість електрики.",
                "  Саме струм нагріває кабелі та викликає пожежі!",
                "",
                "§6• Потужність (Вати, Вт): §fP = Напруга × Струм.",
                "  1 кВт = 1000 Вт, 1 МВт = 1 000 000 Вт.",
                "",
                "§cУВАГА: §fНе подавайте струм більше, ніж може витримати кабель, інакше він вибухне полум'ям!"
            )
        ),
        GuideTopic(
            "🔌", "Кабелі та струм", "ПРОВІДНИКИ ТА ПЕРЕВАНТАЖЕННЯ",
            listOf(
                "§fУсі кабелі мають ліміт сили струму (Ампер):",
                "",
                "§b1. Олов'яний: §fдо §e16 Ампер§f (початковий, дешевий)",
                "§62. Мідний: §fдо §e30 Ампер§f (стандартний для більшості машин)",
                "§73. Алюмінієвий: §fдо §e66 Ампер§f (для потужних магістралей)",
                "§f4. Залізний: §fдо §e120 Ампер§f (високонавантажений важкий дріт)",
                "",
                "§cГОЛІ КАБЕЛІ (БЕЗ ІЗОЛЯЦІЇ):",
                "§fДешевші у крафті, але при дотику §cб'ють гравця струмом на 2 серця§f!",
                "Обов'язково ізолюйте їх §aгумою§f зі слизу на верстаку."
            )
        ),
        GuideTopic(
            "🔥", "Теплогенератор", "ПЕРВИННЕ ДЖЕРЕЛО ЖИВЛЕННЯ",
            listOf(
                "§eТеплогенератор спалює будь-яке паливо (вугілля, дерево, лаву).",
                "",
                "§a• Вихідна напруга: §f380 В",
                "§a• Вихідний струм: §f12 Ампер",
                "§a• Загальна потужність: §f4 560 Вт (4.56 кВт)",
                "§a• Внутрішній буфер: §f800 000 Вт·год",
                "",
                "§eПорада: §fОдин генератор може живити рівно 2 промислові машини (по 6 А кожна) на максимумі потужності."
            )
        ),
        GuideTopic(
            "🔋", "Акумулятори", "НАКОПИЧЕННЯ ТА НЕБЕЗПЕКА ПЕРЕГРІВУ",
            listOf(
                "§fПромисловий акумулятор накопичує енергію та згладжує піки.",
                "",
                "§b• Ємність: §f4 000 000 Вт·год (4 МВт·год)",
                "§b• Робоча напруга: §f380 В",
                "§b• Безпечний струм: §aдо 32 Ампер",
                "§b• Максимальна пропускна здатність: §fдо 256 Ампер",
                "",
                "§cФІЗИКА ПЕРЕГРІВУ ТА ВИБУХУ:",
                "§fЯкщо струм через акумулятор перевищує §c32 А§f, він швидко нагрівається. При досягненні 80 тепла він стає червоним, починає диміти і кипіти.",
                "§4Якщо перегрів триває 8 секунд — акумулятор вибухає силою 0.4 TNT!"
            )
        ),
        GuideTopic(
            "🛡", "Автомати (Захист)", "АВТОМАТИЧНІ ВИМИКАЧІ МЕРЕЖІ",
            listOf(
                "§fАвтоматичний вимикач захищає мережу від аварій.",
                "",
                "§fВстановіть його на початку лінії живлення.",
                "У меню можна встановити поріг відсічки:",
                "§b[16 A]  [32 A]  [66 A]  [120 A]",
                "",
                "§eЯк працює:",
                "Коли струм перевищує обраний поріг, лунає гучне клацання, і автомат миттєво знеструмлює лінію, рятуючи кабелі від вигорання.",
                "Після усунення перевантаження просто натисніть кнопку §a[ВКЛ / СКИД]§f у меню."
            )
        ),
        GuideTopic(
            "⚙", "Прокатний стан", "ОБРОБКА МЕТАЛІВ І МАШИНИ",
            listOf(
                "§fПрокатний стан — ключова машина для створення кабелів.",
                "",
                "§eРецепти прокатного стану:",
                "§f• Злиток (мідь/олово/залізо/алюміній) ──► §bПластина",
                "§f• Олов'яна пластина ──► §e2x Голий олов'яний кабель",
                "§f• Мідна пластина ──► §e2x Голий мідний кабель",
                "§f• Алюмінієва пластина ──► §e2x Голий алюмінієвий кабель",
                "§f• Залізна пластина ──► §e2x Голий залізний кабель",
                "",
                "§aУсі машини (дробарка, пічка, прокатний стан) споживають 380 В і 6 Ампер."
            )
        ),
        GuideTopic(
            "☀", "Сонячна панель", "ЗЕЛЕНА ЕНЕРГІЯ",
            listOf(
                "§fСонячна панель генерує безкоштовну чисту енергію від неба.",
                "",
                "§a• Напруга: §f380 В",
                "§a• Максимальний струм: §fдо 4 Ампер (1.52 кВт)",
                "",
                "§eФактори генерації:",
                "§f• Ясний полудень: §a100% генерації",
                "§f• Дощ або хмари: §e25% генерації",
                "§f• Ніч або закрите небо: §c0% генерації",
                "Панель має буфер 200 000 Вт·год для накопичення."
            )
        ),
        GuideTopic(
            "⚡", "Трансформатор", "ЛІНІЇ ДАЛЕКОЇ ПЕРЕДАЧІ (10 кВ)",
            listOf(
                "§fПри передачі енергії на великі відстані струм викликає втрати й палить проводи.",
                "",
                "§bТрансформатор вирішує це підвищенням напруги!",
                "§f• Режим §bStep-Up§f: перетворює 380 В у §c10 000 В§f.",
                "  При 10 кВ струм стає мізерним (менше 1 А при тій самій потужності!), тому енергію можна передати тонким кабелем на кілометри.",
                "§f• Режим §eStep-Down§f: на базі перетворює 10 кВ назад у 380 В для живлення ваших верстатів."
            )
        ),
        GuideTopic(
            "📟", "Мультиметр", "ГОЛОВНИЙ ПРИЛАД ІНЖЕНЕРА",
            listOf(
                "§fМультиметр дозволяє миттєво перевірити будь-яку ділянку мережі.",
                "",
                "§eЯк користуватися:",
                "Візьміть мультиметр у руку та натисніть §bПКМ§f по будь-якому кабелю, генератору, акумулятору чи верстату.",
                "",
                "§fВін виведе точні дані:",
                "§a• Напругу в лінії",
                "§e• Поточний струм та навантаження у % від ліміту",
                "§6• Передану потужність",
                "§c• Стан перегріву та загрозу аварії"
            )
        )
    )

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Спочатку викликаємо розмиття світу гри ПОЗАДУ інтерфейсу
        this.renderBackground(g, mouseX, mouseY, partialTick)
        g.fillGradient(0, 0, width, height, 0xCC000000.toInt(), 0xEA04060C.toInt())

        val winW = minOf(420, width - 16)
        val winH = minOf(232, height - 16)
        val left = (width - winW) / 2
        val top = (height - winH) / 2
        val leftW = 132

        // Зовнішня заокруглена картка з м'яким неоновим світінням (стиль фотореференсу)
        renderGlowBox(g, left, top, winW, winH, 0x55A0C8FF, 0xFF080B10.toInt())

        // Ліва панель навігації (ширина 132)
        renderGlowBox(g, left + 8, top + 8, leftW, winH - 16, 0x3380B0F0, 0xFF0C1018.toInt())

        // Заголовок зліва
        g.drawString(font, "§6⚡ RAD§fTECH", left + 14, top + 14, 0xFFFFFF, false)
        g.drawString(font, "§8ІНЖЕНЕРНИЙ ДОВІДНИК", left + 14, top + 25, 0x778899, false)

        // Список категорій зліва
        val itemH = 17
        val btnW = leftW - 8
        for (i in topics.indices) {
            val t = topics[i]
            val by = top + 38 + i * itemH
            val isSelected = (i == selectedCategory)
            val isHovered = mouseX in (left + 12)..(left + 12 + btnW) && mouseY in by..(by + itemH - 2)

            if (isSelected) {
                // Вибрана картка світиться неоном
                renderGlowBox(g, left + 12, by, btnW, itemH - 2, 0x8840D0FF.toInt(), 0xFF14243A.toInt())
                g.drawString(font, "${t.icon} §f${t.title}", left + 16, by + 4, 0x55FFFF, false)
            } else {
                if (isHovered) {
                    g.fill(left + 12, by, left + 12 + btnW, by + itemH - 2, 0x2280C0FF.toInt())
                }
                g.drawString(font, "${t.icon} §7${t.title}", left + 16, by + 4, if (isHovered) 0xDDEEFF else 0x8A9BB5, false)
            }
        }

        // Права інформаційна картка (велика, в стилі фотографії з неоновою рамкою)
        val rightX = left + leftW + 14
        val rightW = winW - leftW - 22
        renderGlowBox(g, rightX, top + 8, rightW, winH - 16, 0x5560A0E0, 0xFF090D14.toInt())

        val current = topics[selectedCategory]

        // Заголовок розділу
        renderGlowBox(g, rightX + 6, top + 14, rightW - 12, 22, 0x334080D0, 0xFF121926.toInt())
        g.drawString(font, "${current.icon} §e${current.header}", rightX + 12, top + 21, 0xFFE066, false)

        // Розбиваємо всі рядки тексту за шириною правої картки, щоб текст ніколи не виходив за рамки
        val wrapWidth = rightW - 24
        val wrappedLines = mutableListOf<net.minecraft.util.FormattedCharSequence?>()
        for (paragraph in current.content) {
            if (paragraph.isEmpty()) {
                wrappedLines.add(null)
            } else {
                wrappedLines.addAll(font.split(Component.literal(paragraph), wrapWidth))
            }
        }

        val contentTop = top + 42
        val contentBottom = top + winH - 22
        val maxVisibleLines = (contentBottom - contentTop) / 10
        val maxScroll = maxOf(0, wrappedLines.size - maxVisibleLines)
        if (scrollOffset > maxScroll) scrollOffset = maxScroll
        if (scrollOffset < 0) scrollOffset = 0

        // Текстовий контент із автоматичним переносом рядків
        var ty = contentTop
        for (idx in scrollOffset until wrappedLines.size) {
            val seq = wrappedLines[idx]
            if (seq == null) {
                ty += 6
            } else {
                if (ty + 9 > contentBottom) break
                g.drawString(font, seq, rightX + 12, ty, 0xE0E6F0, false)
                ty += 11
            }
        }

        // Індикатор прокрутки (якщо текст довгий)
        if (maxScroll > 0) {
            val barTop = contentTop
            val barH = contentBottom - contentTop
            val thumbH = maxOf(14, barH * maxVisibleLines / wrappedLines.size)
            val thumbY = barTop + ((barH - thumbH) * scrollOffset / maxScroll)
            g.fill(rightX + rightW - 5, barTop, rightX + rightW - 3, contentBottom, 0x33406080)
            g.fill(rightX + rightW - 5, thumbY, rightX + rightW - 3, thumbY + thumbH, 0xAA40D0FF.toInt())
        }

        // Підказка закриття у правому нижньому кутку картки
        val closeHint = "§8[ESC - Закрити]"
        g.drawString(font, closeHint, rightX + rightW - font.width(closeHint) - 8, top + winH - 18, 0x667788, false)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val winW = minOf(420, width - 16)
        val winH = minOf(232, height - 16)
        val left = (width - winW) / 2
        val top = (height - winH) / 2
        val leftW = 132
        val btnW = leftW - 8

        val itemH = 17
        for (i in topics.indices) {
            val by = top + 38 + i * itemH
            if (mouseX >= left + 12 && mouseX <= left + 12 + btnW && mouseY >= by && mouseY < by + itemH - 2) {
                selectedCategory = i
                scrollOffset = 0
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (scrollY > 0) {
            scrollOffset = maxOf(0, scrollOffset - 1)
            return true
        } else if (scrollY < 0) {
            scrollOffset += 1
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    private fun renderGlowBox(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int, glowColor: Int, fillColor: Int) {
        // М'яке зовнішнє світіння (розмитий неоновий контур)
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, (glowColor and 0x00FFFFFF) or 0x18000000)
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, (glowColor and 0x00FFFFFF) or 0x35000000)
        // Тіло картки
        g.fill(x, y, x + w, y + h, fillColor)
        // Тонка неонова рамка
        g.fill(x, y, x + w, y + 1, glowColor)
        g.fill(x, y + h - 1, x + w, y + h, glowColor)
        g.fill(x, y, x + 1, y + h, glowColor)
        g.fill(x + w - 1, y, x + w, y + h, glowColor)
    }

    override fun isPauseScreen(): Boolean = false
}
