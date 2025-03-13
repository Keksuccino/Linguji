<a href="https://discord.gg/rhayah27GC"><img src="https://img.shields.io/discord/704163135787106365?style=flat&label=Discord&labelColor=%234260f5&color=%2382aeff" /></a> <a href="https://paypal.me/TimSchroeter"><img src="https://img.shields.io/badge/Donate%20via%20PayPal-%233d91ff?style=flat" /></a> <a href="https://www.patreon.com/keksuccino"><img src="https://img.shields.io/badge/Support%20me%20on%20Patreon-%23ff9b3d?style=flat" /></a>

# Linguji

_**lingua** : Italian word for "language"._<br>
_**jimaku** ( 字幕 / じまく ) : Japanese word for "subtitles"._

<br>

Linguji is a subtitle translator powered by AI!<br>
It automatically batch-translates subtitle files and even video files with subtitles!

## Key Features

- **Batch Translation:** Automatically translate multiple files at once without manually choosing the next file!
- **GUI:** Everything in Linguji can be done in a GUI, so no need for manually editing configs or typing commands.
- **Supported Formats:**
  - **Subtitles:** Currently handles ASS and SRT (SubRip) files.
  - **Videos:** Works with MKV files containing ASS and SRT subtitles.
- **Translation Engines:** Linguji supports various translation engines, including Gemini Pro, DeepL, [DeepLX](https://github.com/OwO-Network/DeepLX), and Libre Translate. Choose the engine that best suits your needs.
- **Formatting Preservation:** One of Linguji's priorities is to preserve the original subtitle text formatting as good as possible during translation.
- **Advanced Error Handling:** Linguji strives to keep the translation process going by working around various errors. This includes adapting its translation process to avoid triggering the same error again, like adjusting Gemini's safety threshold after encountering profanity or safety-related blocks.
- **Fallback Translator:** The fallback translation engine is used when the primary engine fails to translate the given subtitles.

<br>

[<img width="634" src="https://github.com/Keksuccino/Linguji/assets/35544624/1ca53281-df10-4684-bea5-f21bd71b005d">](https://www.youtube.com/watch?v=JTlyWOfve20)

# Getting Started

Download the [latest release](https://github.com/Keksuccino/Linguji/releases) for your operating system, then run the `linguji` executable inside.

To start translating subtitle files, simply **set a valid Gemini API key** and put all your subtitle files and video files into the `input_subtitles` folder (by default) that should be at the same path as the `linguji` executable.

Now simply hit the **Start Translation Process** button and see the magic happen!

If you need help getting Linguji to work or have any questions, please join my [Discord](https://discord.gg/rhayah27GC).

# Add Custom Translatable Languages

Your language is missing from the languages menu? No problem!<br>
You can add custom languages by adding them to the `custom_locales.json` file that gets created at the same path as Linguji's exectuable when running it for the first time.

For example, lets add a custom language to the file:

```
[
  {
    "name": "example_name_lowercase_without_spaces",
    "display_name": "Example Display Name",
    "iso": "ex",
    "iso3": "exa"
  },
  {
    "name": "example_name_lowercase_without_spaces_2",
    "display_name": "Example Display Name 2",
    "iso": "ex",
    "iso3": "exa"
  },
  {
    "name": "trigedasleng",
    "display_name": "Trigedasleng",
    "iso": "ts",
    "iso3": "tri"
  }
]
```

We added the fictional language `trigedasleng` below the existing two example entries.<br>
Don't forget the **comma** after every `}`, except the very last one!

When adding **real languages**, make sure to use the correct **ISO** and **ISO-3** language codes!<br>
There's a pretty good [list with language codes](https://www.loc.gov/standards/iso639-2/php/code_list.php) you can use for that.<br>
The first code (3-letter-code) is the ISO-3 code, the second one (2-letter-code) is the ISO code.<br>
If there are two or more ISO-3 codes for the same language, use the one with the `(T)` at the end, but don't include the `(T)`.

Keep an eye on the **debug log** when using custom languages, to see if the translation engines support it!

# Requirements

- Java 17

- Linguji needs [FFMPEG](https://www.gyan.dev/ffmpeg/builds/) (full build) and [MkvToolNix](https://mkvtoolnix.download/downloads.html) for video file support.<br>
If you plan to translate video files, make sure to place all FFMPEG executables in the `ffmpeg` directory and all MkvToolNix executables in the `mkvtoolnix` directory. Both directories get created at the same path as the Linguji executable when running Linguji for the first time.

# Copyright & Licensing

Linguji © Copyright 2025 Keksuccino.<br>
Linguji is licensed under Apache-2.0.

Linguji uses a modified version of [JavaFX Dark Theme](https://github.com/antoniopelusi/JavaFX-Dark-Theme) by Antonio Pelusi.
