package de.keksuccino.linguji.linguji.backend.lib.lang;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.keksuccino.linguji.linguji.backend.lib.FileUtils;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.*;

@SuppressWarnings("unused")
public class Locale {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    private static final File CUSTOM_LOCALES_FILE = new File("custom_locales.json");
    private static final List<Locale> LOCALES = new ArrayList<>();

    public static final Locale GERMAN = Locale.createAndRegister("german", "German", "de", "deu");
    public static final Locale ENGLISH = Locale.createAndRegister("english", "English", "en", "eng");
    public static final Locale ARABIC = Locale.createAndRegister("arabic", "Arabic", "ar", "ara");
    public static final Locale AFRIKAANS = Locale.createAndRegister("afrikaans", "Afrikaans", "af", "afr");
    public static final Locale BENGALI = Locale.createAndRegister("bengali", "Bengali", "bn", "ben");
    public static final Locale RUSSIAN = Locale.createAndRegister("russian", "Russian", "ru", "rus");
    public static final Locale BELARUSIAN = Locale.createAndRegister("belarusian", "Belarusian", "be", "bel");
    public static final Locale BULGARIAN = Locale.createAndRegister("bulgarian", "Bulgarian", "bg", "bul");
    public static final Locale CZECH = Locale.createAndRegister("czech", "Czech", "cs", "ces");
    public static final Locale CHINESE_SIMPLIFIED = Locale.createAndRegister("chinese_simplified", "Chinese (Simplified)", "zh", "zho");
    public static final Locale DUTCH = Locale.createAndRegister("dutch", "Dutch", "nl", "nld");
    public static final Locale DANISH = Locale.createAndRegister("danish", "Danish", "da", "dan");
    public static final Locale FINNISH = Locale.createAndRegister("finnish", "Finnish", "fi", "fin");
    public static final Locale PERSIAN = Locale.createAndRegister("persian", "Persian", "fa", "fas");
    public static final Locale FRENCH = Locale.createAndRegister("french", "French", "fr", "fra");
    public static final Locale GREEK = Locale.createAndRegister("greek", "Greek", "el", "ell");
    public static final Locale HAITIAN = Locale.createAndRegister("haitian", "Haitian", "ht", "hat");
    public static final Locale HAWAIIAN = Locale.createAndRegister("hawaiian", "Hawaiian", "haw", "haw");
    public static final Locale HEBREW = Locale.createAndRegister("hebrew", "Hebrew", "he", "heb");
    public static final Locale HINDI = Locale.createAndRegister("hindi", "Hindi", "hi", "hin");
    public static final Locale CROATIAN = Locale.createAndRegister("croatian", "Croatian", "hr", "hrv");
    public static final Locale HUNGARIAN = Locale.createAndRegister("hungarian", "Hungarian", "hu", "hun");
    public static final Locale ICELANDIC = Locale.createAndRegister("icelandic", "Icelandic", "is", "isl");
    public static final Locale ITALIAN = Locale.createAndRegister("italian", "Italian", "it", "ita");
    public static final Locale JAPANESE = Locale.createAndRegister("japanese", "Japanese", "jp", "jpn");
    public static final Locale THAI = Locale.createAndRegister("thai", "Thai", "th", "tha");
    public static final Locale KOREAN = Locale.createAndRegister("korean", "Korean", "ko", "kor");
    public static final Locale NORWEGIAN = Locale.createAndRegister("norwegian", "Norwegian", "no", "nor");
    public static final Locale POLISH = Locale.createAndRegister("polish", "Polish", "pl", "pol");
    public static final Locale PORTUGUESE = Locale.createAndRegister("portuguese", "Portuguese", "pt", "por");
    public static final Locale SWEDISH = Locale.createAndRegister("swedish", "Swedish", "sv", "swe");
    public static final Locale UKRAINIAN = Locale.createAndRegister("ukrainian", "Ukrainian", "uk", "ukr");
    public static final Locale SPANISH = Locale.createAndRegister("spanish", "Spanish", "es", "spa");
    public static final Locale SPANISH_INTERNATIONAL = Locale.createAndRegister("spanish_international", "Spanish (International Sort)", "es-ES", "spa");
    public static final Locale SPANISH_MEXICO = Locale.createAndRegister("spanish_mexico", "Spanish (Mexico)", "es-MX", "spa");
    public static final Locale SPANISH_LATIN = Locale.createAndRegister("spanish_latin", "Spanish (Latin America)", "es-419", "spa");
    public static final Locale SLOVAK = Locale.createAndRegister("slovak", "Slovak", "sk", "slk");
    public static final Locale TURKISH = Locale.createAndRegister("turkish", "Turkish", "tr", "tur");

    private String name;
    private String display_name;
    private String iso;
    private String iso3;

    @NotNull
    public static Locale createAndRegister(@NotNull String name, @NotNull String display_name, @NotNull String iso, @NotNull String iso3) {
        Locale locale = new Locale(name, display_name, iso, iso3);
        LOCALES.add(locale);
        return locale;
    }

    public static void registerCustomLocals() {

        try {

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            if (!CUSTOM_LOCALES_FILE.isFile()) {
                CUSTOM_LOCALES_FILE.createNewFile();
                Locale[] example = new Locale[] { new Locale("example_name_lowercase_without_spaces", "Example Display Name", "ex", "exa"), new Locale("example_name_lowercase_without_spaces_2", "Example Display Name 2", "ex", "exa") };
                String exampleJson = gson.toJson(example);
                FileUtils.writeTextToFile(CUSTOM_LOCALES_FILE, exampleJson);
            }

            List<String> lines = FileUtils.readTextLinesFrom(CUSTOM_LOCALES_FILE);
            StringBuilder customLocalesJson = new StringBuilder();
            lines.forEach(customLocalesJson::append);

            Locale[] customLocales = Objects.requireNonNull((Locale[]) gson.fromJson(customLocalesJson.toString(), Locale.class.arrayType()));
            List<Locale> customLocalesList = new ArrayList<>(Arrays.asList(customLocales)); //needs to get added to a new list after asList(), otherwise removeIf() fails
            customLocalesList.removeIf(locale -> "example_name_lowercase_without_spaces".equals(locale.name) || "example_name_lowercase_without_spaces_2".equals(locale.name));
            LOCALES.addAll(customLocalesList);

        } catch (Exception ex) {
            LOGGER.error("Failed to register custom locals!", ex);
        }

    }

    public Locale() {
    }

    private Locale(String name, String display_name, String iso, String iso3) {
        this.name = name;
        this.display_name = display_name;
        this.iso = iso;
        this.iso3 = iso3;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDisplayName() {
        return display_name;
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
        ordered.sort((o1, o2) -> comparator.compare(o1.display_name, o2.display_name));
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
            if (locale.display_name.equals(displayName)) return locale;
        }
        return null;
    }

    @NotNull
    public static Locale[] values() {
        return LOCALES.toArray(new Locale[0]);
    }

}
