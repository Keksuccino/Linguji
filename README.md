<a href="https://discord.gg/rhayah27GC"><img src="https://img.shields.io/discord/704163135787106365?style=flat&label=Discord&labelColor=%234260f5&color=%2382aeff" /></a> <a href="https://paypal.me/TimSchroeter"><img src="https://img.shields.io/badge/Donate%20via%20PayPal-%233d91ff?style=flat" /></a> <a href="https://www.patreon.com/keksuccino"><img src="https://img.shields.io/badge/Support%20me%20on%20Patreon-%23ff9b3d?style=flat" /></a>

# Linguji

_**lingua** : Italian word for "language"._<br>
_**jimaku** ( 字幕 / じまく ) : Japanese word for "subtitles"._

<br>

Linguji is a subtitle translator powered by AI!<br>
It automatically batch-translates subtitle files and even video files with subtitles!

## Key Features

- **Batch Translation:** Automatically translate multiple files at once without manually choosing the next file!
- **Supported Formats:**
  - **Subtitles:** Currently handles ASS files.
  - **Videos:** Works with MKV files containing ASS subtitles.
- **Translation Engines:** Linguji supports various translation engines, including Gemini Pro, DeepL, [DeepLX](https://github.com/OwO-Network/DeepLX), and Libre Translate. Choose the engine that best suits your needs.
- **Formatting Preservation:** One of Linguji's priorities is to preserve the original subtitle text formatting as good as possible during translation.
- **Advanced Error Handling:** Linguji strives to keep the translation process going by working around various errors. This includes adapting its translation process to avoid triggering the same error again, like adjusting Gemini's safety threshold after encountering profanity or safety-related blocks.
- **Fallback Translator:** The fallback translation engine is used when the primary engine fails to translate the given subtitles.

<br>
<img width="540" alt="Linguji UI" src="https://github.com/Keksuccino/Linguji/assets/35544624/ee9b3f3a-c841-48e4-b5c8-764dc8dda974">

# Getting Started

Download the [latest release](https://github.com/Keksuccino/Linguji/releases) for your operating system, then run the `linguji` executable inside.

To start translating subtitle files, simply **set a valid Gemini API key** and put all your subtitle files into the `input_subtitles` folder (by default) that should be in the same directory as the `linguji` executable.

Now simply hit the **Start Translation Process** button and see the magic happen!

If you need help getting Linguji to work or have any questions, please join my [Discord](https://discord.gg/rhayah27GC).

# Requirements

- Java 17

Linguji needs [FFMPEG](https://www.gyan.dev/ffmpeg/builds/) (full build) and [MkvToolNix](https://mkvtoolnix.download/downloads.html) for video file support.<br>
If you plan to translate video files, make sure to place all FFMPEG executables in the `ffmpeg` directory and all MkvToolNix executables in the `mkvtoolnix` directory. Both directories get created at the same path as the Linguji executable when running Linguji for the first time.

# Copyright & Licensing

Linguji © Copyright 2024 Keksuccino.<br>
Linguji is licensed under Apache-2.0.

Linguji uses a modified version of [JavaFX Dark Theme](https://github.com/antoniopelusi/JavaFX-Dark-Theme) by Antonio Pelusi.
