package com.provans.flowers.data

import com.provans.flowers.model.*

object FlowerData {
    
    val categories = listOf(
        Category(1, "Все", "🌸"),
        Category(2, "Букеты", "💐"),
        Category(3, "Розы", "🌹"),
        Category(4, "Тюльпаны", "🌷"),
        Category(5, "Пионы", "🌺"),
        Category(6, "Композиции", "🎁"),
        Category(7, "В горшках", "🪴")
    )
    
    val occasions = listOf(
        Occasion(1, "Любой"),
        Occasion(2, "День рождения"),
        Occasion(3, "8 Марта"),
        Occasion(4, "14 Февраля"),
        Occasion(5, "Свадьба"),
        Occasion(6, "Юбилей"),
        Occasion(7, "Прощай"),
        Occasion(8, "Извинись"),
        Occasion(9, "Скучаю")
    )
    
    val flowers = mutableListOf(
        Flower(
            id = 1,
            name = "Красные розы Эквадор",
            description = "25 красных роз высшего качества из Эквадора. Длина стебля 60 см. Идеальны для романтического подарка.",
            price = 3500,
            oldPrice = 4200,
            imageUrl = "https://placehold.co/400x400/ff6b6b/ffffff?text=Красные+розы",
            category = categories[2],
            occasion = occasions[3],
            colors = listOf("Красный"),
            rating = 4.8f,
            reviewCount = 127,
            isBestseller = true
        ),
        Flower(
            id = 2,
            name = "Белые пионы",
            description = "Нежный букет из 7 белых пионов. Символ чистоты и искренности чувств.",
            price = 4800,
            imageUrl = "https://placehold.co/400x400/f8f9fa/333333?text=Белые+пионы",
            category = categories[4],
            occasion = occasions[4],
            colors = listOf("Белый"),
            rating = 4.9f,
            reviewCount = 89,
            isNew = true
        ),
        Flower(
            id = 3,
            name = "Розовые тюльпаны",
            description = "25 розовых тюльпанов в крафт-упаковке. Весенний букет для хорошего настроения.",
            price = 2200,
            oldPrice = 2800,
            imageUrl = "https://placehold.co/400x400/ff69b4/ffffff?text=Тюльпаны",
            category = categories[3],
            occasion = occasions[2],
            colors = listOf("Розовый"),
            rating = 4.7f,
            reviewCount = 203,
            isBestseller = true
        ),
        Flower(
            id = 4,
            name = "Микс из 101 розы",
            description = "Шикарный букет из 101 розы микс (красные, белые, розовые). Для самых особенных моментов.",
            price = 12000,
            imageUrl = "https://placehold.co/400x400/ff1493/ffffff?text=101+роза",
            category = categories[2],
            occasion = occasions[5],
            colors = listOf("Красный", "Белый", "Розовый"),
            rating = 5.0f,
            reviewCount = 45
        ),
        Flower(
            id = 5,
            name = "Орхидея в горшке",
            description = "Белая орхидея Фаленопсис в керамическом горшке. Долговечный подарок для дома или офиса.",
            price = 3200,
            imageUrl = "https://placehold.co/400x400/fff0f5/333333?text=Орхидея",
            category = categories[6],
            occasion = occasions[1],
            colors = listOf("Белый"),
            rating = 4.6f,
            reviewCount = 78
        ),
        Flower(
            id = 6,
            name = "Жёлтые подсолнухи",
            description = "Букет из 9 подсолнухов. Солнечное настроение и летняя атмосфера.",
            price = 2800,
            imageUrl = "https://placehold.co/400x400/ffd700/333333?text=Подсолнухи",
            category = categories[1],
            occasion = occasions[1],
            colors = listOf("Жёлтый"),
            rating = 4.8f,
            reviewCount = 156,
            isBestseller = true
        ),
        Flower(
            id = 7,
            name = "Лавандовый букет",
            description = "Нежный букет из лаванды с добавлением эвкалипта. Успокаивающий аромат и красивый внешний вид.",
            price = 1900,
            imageUrl = "https://placehold.co/400x400/e6e6fa/333333?text=Лаванда",
            category = categories[1],
            occasion = occasions[8],
            colors = listOf("Фиолетовый"),
            rating = 4.5f,
            reviewCount = 67
        ),
        Flower(
            id = 8,
            name = "Композиция в коробке",
            description = "Стильная цветочная композиция в подарочной коробке. Розы, эустома, зелень.",
            price = 4500,
            imageUrl = "https://placehold.co/400x400/ff69b4/ffffff?text=Композиция",
            category = categories[5],
            occasion = occasions[2],
            colors = listOf("Розовый", "Белый"),
            rating = 4.9f,
            reviewCount = 112,
            isNew = true
        ),
        Flower(
            id = 9,
            name = "Гортензия голубая",
            description = "Ветка голубой гортензии 50 см. Одиночное украшение или дополнение к букету.",
            price = 1500,
            imageUrl = "https://placehold.co/400x400/87ceeb/ffffff?text=Гортензия",
            category = categories[1],
            occasion = occasions[1],
            colors = listOf("Голубой"),
            rating = 4.4f,
            reviewCount = 34
        ),
        Flower(
            id = 10,
            name = "Букет \"Прощай\"",
            description = "Сдержанный букет из хризантем и эвкалипта. Для сложных моментов жизни.",
            price = 2400,
            imageUrl = "https://placehold.co/400x400/808080/ffffff?text=Прощай",
            category = categories[1],
            occasion = occasions[6],
            colors = listOf("Белый", "Зелёный"),
            rating = 4.7f,
            reviewCount = 23
        )
    )
    
    val reviews = listOf(
        Review(1, 1, "Анна М.", 5.0f, "Прекрасные розы! Доставили вовремя, свежие и красивые. Мама очень довольна!", "15.02.2026"),
        Review(2, 1, "Сергей К.", 4.5f, "Хорошее качество, но упаковка могла быть лучше.", "10.02.2026"),
        Review(3, 3, "Мария Л.", 5.0f, "Тюльпаны просто чудо! Держатся уже неделю.", "08.02.2026"),
        Review(4, 6, "Иван П.", 4.8f, "Подсолнухи — это любовь! Доставка быстрая.", "05.02.2026")
    )
    
    fun getFlowerById(id: Int): Flower? {
        return flowers.find { it.id == id }
    }
    
    fun getFlowersByCategory(categoryId: Int): List<Flower> {
        if (categoryId == 1) return flowers
        return flowers.filter { it.category.id == categoryId }
    }
    
    fun getFlowersByOccasion(occasionId: Int): List<Flower> {
        if (occasionId == 1) return flowers
        return flowers.filter { it.occasion.id == occasionId }
    }
    
    fun getBestsellers(): List<Flower> = flowers.filter { it.isBestseller }
    fun getNewArrivals(): List<Flower> = flowers.filter { it.isNew }
    fun getDiscounted(): List<Flower> = flowers.filter { it.oldPrice != null }
}
