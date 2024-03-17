# Linguji

_**lingua** : Italian word for "language"._<br>
_**jimaku** ( 字幕 / じまく ) : Japanese word for "subtitles"._

<br>

Linguji is a Gemini-powered AI subtitle file translator.

It currently supports only ASS files and can batch-translate them from a source language to a target language while trying to preserve its text formatting as good as possible. It has an advanced error handling and tries to keep the translation process going by working around various types of errors and trying to adapt its translation process to not trigger the same error again, like changing the **safety threshold** after a prompt or answer was blocked for profanity/safety reasons.

<br>
<img width="639" alt="Screenshot_4" src="https://github.com/Keksuccino/Linguji/assets/35544624/e96e387e-980d-4869-8e0c-55c14e4c3442">

# Getting Started

Download the [latest release](https://github.com/Keksuccino/Linguji/releases) for your operating system, then run the `linguji` executable inside.

To start translating subtitle files, simply **set a valid Gemini API key** and put all your subtitle files into the `input_subtitles` folder (by default) that should be in the same directory as the `linguji` executable.

Now simply hit the **Start Translation Process** button and see the magic happen!

# Requirements

- Java 17

# Copyright & Licensing

Linguji © Copyright 2024 Keksuccino.<br>
Linguji is licensed under Apache-2.0.

Linguji uses a modified version of [JavaFX Dark Theme](https://github.com/antoniopelusi/JavaFX-Dark-Theme) by Antonio Pelusi.
