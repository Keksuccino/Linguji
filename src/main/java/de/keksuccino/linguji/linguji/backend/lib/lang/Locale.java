package de.keksuccino.linguji.linguji.backend.lib.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public enum Locale {

    GERMAN("german", "German", "de", "deu"),
    ENGLISH("english", "English", "en", "eng"),
    ARABIC("arabic", "Arabic", "ar", "ara"),
    AFRIKAANS("afrikaans", "Afrikaans", "af", "afr"),
    BENGALI("bengali", "Bengali", "bn", "ben"),
    RUSSIAN("russian", "Russian", "ru", "rus"),
    BELARUSIAN("belarusian", "Belarusian", "be", "bel"),
    BULGARIAN("bulgarian", "Bulgarian", "bg", "bul"),
    CZECH("czech", "Czech", "cs", "ces"),
    CHINESE_SIMPLIFIED("chinese_simplified", "Chinese (Simplified)", "zh", "zho"),
    DUTCH("dutch", "Dutch", "nl", "nld"),
    DANISH("danish", "Danish", "da", "dan"),
    FINNISH("finnish", "Finnish", "fi", "fin"),
    PERSIAN("persian", "Persian", "fa", "fas"),
    FRENCH("french", "French", "fr", "fra"),
    GREEK("greek", "Greek", "el", "ell"),
    HAITIAN("haitian", "Haitian", "ht", "hat"),
    HAWAIIAN("hawaiian", "Hawaiian", "haw", "haw"),
    HEBREW("hebrew", "Hebrew", "he", "heb"),
    HINDI("hindi", "Hindi", "hi", "hin"),
    CROATIAN("croatian", "Croatian", "hr", "hrv"),
    HUNGARIAN("hungarian", "Hungarian", "hu", "hun"),
    ICELANDIC("icelandic", "Icelandic", "is", "isl"),
    ITALIAN("italian", "Italian", "it", "ita"),
    JAPANESE("japanese", "Japanese", "jp", "jpn"),
    THAI("thai", "Thai", "th", "tha"),
    KOREAN("korean", "Korean", "ko", "kor"),
    NORWEGIAN("norwegian", "Norwegian", "no", "nor"),
    POLISH("polish", "Polish", "pl", "pol"),
    PORTUGUESE("portuguese", "Portuguese", "pt", "por"),
    SWEDISH("swedish", "Swedish", "sv", "swe"),
    UKRAINIAN("ukrainian", "Ukrainian", "uk", "ukr"),
    SPANISH("spanish", "Spanish", "es", "spa"),
    SPANISH_INTERNATIONAL("spanish_international", "Spanish (International Sort)", "es-ES", "spa"),
    SPANISH_MEXICO("spanish_mexico", "Spanish (Mexico)", "es-MX", "spa"),
    SPANISH_LATIN("spanish_latin", "Spanish (Latin America)", "es-419", "spa"),
    SLOVAK("slovak", "Slovak", "sk", "slk");

    private final String name;
    private final String displayName;
    private final String iso;
    private final String iso3;

    Locale(String name, String displayName, String iso, String iso3) {
        this.name = name;
        this.displayName = displayName;
        this.iso = iso;
        this.iso3 = iso3;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public String getIso() {
        return iso;
    }

    @NotNull
    public String getIso3() {
        return iso3;
    }

    @NotNull
    public static List<Locale> getOrderedAlphabeticallyByDisplayName() {
        List<Locale> ordered = Arrays.asList(Locale.values());
        Comparator<String> comparator = Comparator.naturalOrder();
        ordered.sort((o1, o2) -> comparator.compare(o1.displayName, o2.displayName));
        return ordered;
    }

    @Nullable
    public static Locale getByName(@NotNull String name) {
        for (Locale locale : Locale.values()) {
            if (locale.name.equals(name)) return locale;
        }
        return null;
    }

    @Nullable
    public static Locale getByDisplayName(@NotNull String displayName) {
        for (Locale locale : Locale.values()) {
            if (locale.displayName.equals(displayName)) return locale;
        }
        return null;
    }

}
