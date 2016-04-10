package de.tudarmstadt.informatik.lt.sogaardparser.extractor;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Keyword extractor performing TextRank on raw text to extract function words of a language.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.accepts("input", "file with space-separated words, each sentence on a new line").withRequiredArg().ofType(File.class).required();
        parser.accepts("output", "file to write newline-separated function words to").withRequiredArg().ofType(File.class).required();
        parser.accepts("count", "number of keywords to extract (default: 50)").withRequiredArg().ofType(Integer.class).defaultsTo(50);
        parser.accepts("help", "shows this help message").forHelp();

        OptionSet options = parser.parse(args);

        if (options.has("help")) {
            parser.printHelpOn(System.out);
            return;
        }

        KeywordExtractor.extract(((File) options.valueOf("input")).toPath(), ((File) options.valueOf("output")).toPath(),
                (Integer) options.valueOf("count"));
    }
}
