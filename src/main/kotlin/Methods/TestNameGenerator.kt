package Methods

import kotlin.random.Random

data class Adjective(
    val masculine: String,    // мужской род
    val feminine: String,     // женский род
    val neutral: String      // средний род
)

object TestNameGenerator {
    private val adjectives = listOf(
        // Позитивные характеристики
        Adjective("Увлекательный", "Увлекательная", "Увлекательное"),
        Adjective("Загадочный", "Загадочная", "Загадочное"),
        Adjective("Неожиданный", "Неожиданная", "Неожиданное"),
        Adjective("Интересный", "Интересная", "Интересное"),
        Adjective("Познавательный", "Познавательная", "Познавательное"),
        Adjective("Необычный", "Необычная", "Необычное"),
        Adjective("Занимательный", "Занимательная", "Занимательное"),
        Adjective("Удивительный", "Удивительная", "Удивительное"),
        Adjective("Весёлый", "Весёлая", "Весёлое"),
        Adjective("Креативный", "Креативная", "Креативное"),
        Adjective("Эксклюзивный", "Эксклюзивная", "Эксклюзивное"),
        Adjective("Особенный", "Особенная", "Особенное"),
        Adjective("Уникальный", "Уникальная", "Уникальное"),
        Adjective("Тайный", "Тайная", "Тайное"),
        Adjective("Специальный", "Специальная", "Специальное"),

        // Интеллектуальные характеристики
        Adjective("Логический", "Логическая", "Логическое"),
        Adjective("Мыслительный", "Мыслительная", "Мыслительное"),
        Adjective("Интеллектуальный", "Интеллектуальная", "Интеллектуальное"),
        Adjective("Развивающий", "Развивающая", "Развивающее"),
        Adjective("Образовательный", "Образовательная", "Образовательное"),
        Adjective("Умственный", "Умственная", "Умственное"),
        Adjective("Мудрый", "Мудрая", "Мудрое"),
        Adjective("Аналитический", "Аналитическая", "Аналитическое"),
        Adjective("Творческий", "Творческая", "Творческое"),

        // Эмоциональные характеристики
        Adjective("Захватывающий", "Захватывающая", "Захватывающее"),
        Adjective("Вдохновляющий", "Вдохновляющая", "Вдохновляющее"),
        Adjective("Впечатляющий", "Впечатляющая", "Впечатляющее"),
        Adjective("Волшебный", "Волшебная", "Волшебное"),
        Adjective("Магический", "Магическая", "Магическое"),
        Adjective("Чудесный", "Чудесная", "Чудесное"),
        Adjective("Фантастический", "Фантастическая", "Фантастическое"),
        Adjective("Невероятный", "Невероятная", "Невероятное"),
        Adjective("Потрясающий", "Потрясающая", "Потрясающее"),
        Adjective("Великолепный", "Великолепная", "Великолепное"),

        // Временные характеристики
        Adjective("Мгновенный", "Мгновенная", "Мгновенное"),
        Adjective("Быстрый", "Быстрая", "Быстрое"),
        Adjective("Молниеносный", "Молниеносная", "Молниеносное"),
        Adjective("Динамичный", "Динамичная", "Динамичное"),

        // Качественные характеристики
        Adjective("Первоклассный", "Первоклассная", "Первоклассное"),
        Adjective("Отборный", "Отборная", "Отборное"),
        Adjective("Избранный", "Избранная", "Избранное"),
        Adjective("Безупречный", "Безупречная", "Безупречное"),
        Adjective("Совершенный", "Совершенная", "Совершенное")
    )

    private val nouns = listOf(
        // Классические форматы
        Word("Викторина", WordGender.FEMININE),
        Word("Тест", WordGender.MASCULINE),
        Word("Квест", WordGender.MASCULINE),
        Word("Опрос", WordGender.MASCULINE),
        Word("Челлендж", WordGender.MASCULINE),
        Word("Испытание", WordGender.NEUTRAL),
        Word("Приключение", WordGender.NEUTRAL),
        Word("Головоломка", WordGender.FEMININE),
        Word("Задание", WordGender.NEUTRAL),
        Word("Проверка", WordGender.FEMININE),
        Word("Марафон", WordGender.MASCULINE),
        Word("Исследование", WordGender.NEUTRAL),
        Word("Путешествие", WordGender.NEUTRAL),
        Word("Эксперимент", WordGender.MASCULINE),

        // Образовательные форматы
        Word("Практикум", WordGender.MASCULINE),
        Word("Задачник", WordGender.MASCULINE),
        Word("Упражнение", WordGender.NEUTRAL),
        Word("Разминка", WordGender.FEMININE),
        Word("Тренажёр", WordGender.MASCULINE),
        Word("Симулятор", WordGender.MASCULINE),
        Word("Лабиринт", WordGender.MASCULINE),
        Word("Олимпиада", WordGender.FEMININE),
        Word("Турнир", WordGender.MASCULINE),
        Word("Состязание", WordGender.NEUTRAL),

        // Развлекательные форматы
        Word("Загадка", WordGender.FEMININE),
        Word("Ребус", WordGender.MASCULINE),
        Word("Игра", WordGender.FEMININE),
        Word("Приключение", WordGender.NEUTRAL),
        Word("Квиз", WordGender.MASCULINE),
        Word("Забава", WordGender.FEMININE),
        Word("Развлечение", WordGender.NEUTRAL),
        Word("Задачка", WordGender.FEMININE),
        Word("Головоломка", WordGender.FEMININE),
        Word("Феерия", WordGender.FEMININE),

        // Современные форматы
        Word("Батл", WordGender.MASCULINE),
        Word("Челлендж", WordGender.MASCULINE),
        Word("Брейн-ринг", WordGender.MASCULINE),
        Word("Интенсив", WordGender.MASCULINE),
        Word("Воркшоп", WordGender.MASCULINE)
    )


    private val additions = listOf(
        // Целевая аудитория
        Addition("для любознательных", false),
        Addition("для всех", false),
        Addition("для умников", false),
        Addition("для друзей", false),
        Addition("для профессионалов", false),
        Addition("для новичков", false),
        Addition("для экспертов", false),
        Addition("для энтузиастов", false),

        // Характеристики сложности
        Addition("со звёздочкой", false),
        Addition("с секретом", false),
        Addition("с подвохом", false),
        Addition("с сюрпризом", false),
        Addition("с изюминкой", false),
        Addition("с загадкой", false),
        Addition("повышенной сложности", false),
        Addition("для продвинутых", false),

        // Временные характеристики
        Addition("на скорость", false),
        Addition("на время", false),
        Addition("без ограничений", false),
        Addition("без границ", false),
        Addition("на результат", false),
        Addition("на рекорд", false),
        Addition("на максимум", false),
        Addition("на все 100", false),

        // Эмоциональные дополнения
        Addition("с улыбкой", false),
        Addition("от души", false),
        Addition("с настроением", false),
        Addition("с азартом", false),
        Addition("с вдохновением", false),
        Addition("с интересом", false),
        Addition("с увлечением", false),
        Addition("с радостью", false),

        // Особые характеристики
        Addition("высшего уровня", true),
        Addition("премиум класса", true),
        Addition("экстра формата", true),
        Addition("нового поколения", true),
        Addition("2.0", true),
        Addition("максимальной версии", true),
        Addition("расширенной версии", true),
        Addition("про версии", true)
    )


    enum class WordGender {
        MASCULINE, FEMININE, NEUTRAL
    }

    data class Word(
        val text: String,
        val gender: WordGender
    )

    data class Addition(
        val text: String,
        val requiresPreposition: Boolean
    )

    private fun getAdjective(word: Word): String {
        val randomAdj = adjectives.random()
        return when (word.gender) {
            WordGender.MASCULINE -> randomAdj.masculine
            WordGender.FEMININE -> randomAdj.feminine
            WordGender.NEUTRAL -> randomAdj.neutral
        }
    }

    private fun combineWords(adj: String?, noun: Word, addition: Addition?): String {
        val parts = mutableListOf<String>()

        // Добавляем прилагательное, если оно есть
        adj?.let { parts.add(it) }

        // Добавляем существительное
        parts.add(noun.text)

        // Добавляем дополнение, если оно есть
        addition?.let {
            if (it.requiresPreposition && !it.text.startsWith("с ") &&
                !it.text.startsWith("со ") && !it.text.startsWith("для ")) {
                parts.add("с")
            }
            parts.add(it.text)
        }

        return parts.joinToString(" ")
    }

    fun generateName(): String {
        val random = Random.Default
        val selectedNoun = nouns.random()

        return when (random.nextInt(5)) {
            0 -> combineWords(
                getAdjective(selectedNoun),
                selectedNoun,
                null
            )
            1 -> combineWords(
                null,
                selectedNoun,
                additions.random()
            )
            2 -> combineWords(
                getAdjective(selectedNoun),
                selectedNoun,
                additions.random()
            )
            3 -> "✨ ${combineWords(
                getAdjective(selectedNoun),
                selectedNoun,
                null
            )}"
            else -> "🌟 ${combineWords(
                null,
                selectedNoun,
                additions.random()
            )}"
        }
    }
}

