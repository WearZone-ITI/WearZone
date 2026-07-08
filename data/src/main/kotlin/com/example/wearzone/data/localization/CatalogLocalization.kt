package com.example.wearzone.data.localization

object CatalogLocalization {
    private val categoryArabicMap = mapOf(
        "men" to "رجال",
        "women" to "نساء",
        "kids" to "أطفال",
        "children" to "أطفال",
        "shoes" to "أحذية",
        "footwear" to "أحذية",
        "bags & accessories" to "شنط وإكسسوارات",
        "bags and accessories" to "شنط وإكسسوارات",
        "dresses" to "فساتين",
        "dress" to "فساتين",
        "t-shirts" to "تيشيرتات",
        "t shirt" to "تيشيرتات",
        "t-shirt" to "تيشيرتات",
        "shirts" to "قمصان",
        "shirt" to "قمصان",
        "casual shirts" to "قمصان كاجوال",
        "formal shirts" to "قمصان رسمية",
        "hoodies" to "هوديز",
        "hoodie" to "هوديز",
        "jackets" to "جاكيتات",
        "jacket" to "جاكيتات",
        "pants" to "بناطيل",
        "trousers" to "بناطيل",
        "jeans" to "جينز",
        "shorts" to "شورتات",
        "skirts" to "تنانير",
        "skirt" to "تنانير",
        "tops" to "توبات",
        "top" to "توبات",
        "bags" to "شنط",
        "bag" to "شنط",
        "accessories" to "إكسسوارات",
        "sneakers" to "سنيكرز",
        "sneaker" to "سنيكرز",
        "apparel" to "ملابس",
        "new arrivals" to "وصل حديثًا",
    )

    fun isArabic(languageCode: String?): Boolean =
        languageCode.orEmpty().trim().lowercase().startsWith("ar")

    fun selectLocalized(
        english: String?,
        arabic: String?,
        languageCode: String?,
        fallback: String? = english,
    ): String {
        val englishValue = english.cleanText().ifBlank { fallback.cleanText() }
        val arabicValue = arabic.cleanText()
        return if (isArabic(languageCode)) {
            arabicValue.ifBlank { englishValue }
        } else {
            englishValue.ifBlank { arabicValue }
        }
    }

    fun arabicCategoryFor(value: String?): String =
        categoryArabicMap[value.normalizedKey()].orEmpty()

    fun localizedCategoryTitle(title: String, languageCode: String?): String =
        selectLocalized(
            english = title,
            arabic = arabicCategoryFor(title),
            languageCode = languageCode,
            fallback = title,
        )
}

fun String?.cleanText(): String = this
    .orEmpty()
    .replace(Regex("<\\s*br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("<\\s*/p\\s*>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("<\\s*/li\\s*>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("<\\s*li[^>]*>", RegexOption.IGNORE_CASE), "- ")
    .replace(Regex("<[^>]*>"), "")
    .replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#039;", "'")
    .replace("&#39;", "'")
    .replace(Regex("[ \\t]+"), " ")
    .replace(Regex("\\n\\s+"), "\n")
    .replace(Regex("\\n{3,}"), "\n\n")
    .trim()

private fun String?.normalizedKey(): String = this
    .orEmpty()
    .trim()
    .replace(Regex("[-_]+"), " ")
    .replace(Regex("\\s+"), " ")
    .lowercase()
