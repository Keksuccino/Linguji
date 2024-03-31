package de.keksuccino.linguji.linguji.backend;

import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.Ffmpeg;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoInfo;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoStream;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.mkvtoolnix.MkvToolNix;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.translator.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;

import java.io.File;
import java.util.Objects;

public class Test {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static void main(String[] args) throws Exception {

//        AbstractTranslationEngine translator = Objects.requireNonNull(TranslationEngines.DEEPLX.createInstance());
//
//        //Some GenAI models fail to translate this because it contains _boobs_ and _pron_ (oh no!)
//        String text = """
//Hey, I'm back! Your sister's home!
//I told you to knock before barging into my room.
//What is it?
//I have a tiny favor to ask.
//I'd like you to return this student ID card.
//It's Shiraishi-kun's.
//Oh, you know him?
//Yeah.
//He was reading a porno mag.
//Huh?
//The girl on the cover had ginormous jugs. Oh, teenagers!
//He was glancing around nervously, and I bet his heart was going a mile a minute!
//Well then, make sure to get that back to him!
//Big boobs...
//Young
//Jump
//Where should
//I hang it up?
//Identification Card
//This document certifies that this person
//is a Haruka North High School student.
//Year 1 Class 1
//Name: Junta Shiraishi (16 years old)
//Date of birth: April 25th
//School name: Haruka North High School
//Shiraishi-kun, what's this?
//My student ID card!
//Why do you have that?
//Why do you think I do?
//                """;
//
//        LOGGER.info("TRANSLATED: \n" + translator.translate(text, new TranslationProcess()));

        Ffmpeg ffmpeg = Ffmpeg.buildDefault();
        VideoInfo info = ffmpeg.getVideoInfo(new File("output.mkv"));

        for (VideoStream sub : info.getSubtitles()) {
            LOGGER.info("########## SUB #" + sub.index + ": " + sub);
        }

//        MkvToolNix mkvToolNix = MkvToolNix.createDefault();
//        mkvToolNix.addSubtitleToMkv(new File("video.mkv"), new File("output.mkv"), new File("subtitle.ass"), Locale.GERMAN, true);

    }

}
