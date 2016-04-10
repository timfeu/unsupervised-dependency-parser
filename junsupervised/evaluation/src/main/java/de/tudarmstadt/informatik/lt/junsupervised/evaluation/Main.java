package de.tudarmstadt.informatik.lt.junsupervised.evaluation;

import de.tudarmstadt.informatik.lt.junsupervised.JUnsupervisedParser;
import de.tudarmstadt.informatik.lt.junsupervised.UniversalPOSTagConverter;
import de.tudarmstadt.informatik.lt.junsupervised.baseline.LinearRankingParser;
import de.tudarmstadt.informatik.lt.junsupervised.baseline.OracleParser;
import de.tudarmstadt.informatik.lt.junsupervised.baseline.RandomizedRankingParser;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Writer;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class Main {
    public static enum RankingMode {
        NORMAL, RANDOMIZED, LINEAR, LINEAR_REVERSE, ORACLE
    }

    public static void main(String[] args) throws IOException, UIMAException, InterruptedException,
            ClassNotFoundException {
        OptionParser parser = new OptionParser();
        parser.accepts("input", "single input file from the CoNLL-X shared task, must be gold-annotated")
                .withRequiredArg().ofType(File.class).required();
        parser.accepts("script", "path to Perl evaluation script (cp-eval07.pl)").withRequiredArg().ofType(File
                .class).required();
        parser.accepts("keywords", "path to keywords file").withRequiredArg().ofType(File
                .class).required();
        parser.accepts("rankingMode", "Ranking mode, may be NORMAL or a baseline/skyline (RANDOMIZED, LINEAR, " +
                "LINEAR_REVERSE, ORACLE)")
                .withRequiredArg()
                .ofType(RankingMode.class).defaultsTo(RankingMode.NORMAL);
        parser.accepts("10TokensLimit", "If present, will only evaluate on sentences with at most 10 non-punctuation " +
                "tokens.");
        parser.accepts("linkFW2FW", "Whether to add" +
                " the function word link even if the target is also a function word").withRequiredArg().ofType(Boolean.class).defaultsTo(true);
        parser.accepts("removePunctuation", "If present together with 10TokensLimit, will remove punctuation " +
                "completely from the corpus *before* parsing");
        parser.accepts("useUniversalRules", "If true, uses universal dependency rules. Requires Universal POS tags " +
                "(use option unverisalRulesMap to enable the converter).")
                .withRequiredArg().ofType(Boolean.class).defaultsTo(false);
        parser.accepts("universalPosMap", "path to universal POS mapping as specified in https://github" +
                ".com/slavpetrov/universal-pos-tags. If you do not specify this option, the converter will not work")
                .withRequiredArg().ofType(File.class);
        parser.accepts("neighborLinkCount", "How many links to add between direct neighbors (number of directional " +
                "links, results in 2* bidirectional links between neighbors).").withRequiredArg().ofType(Integer
                .class).defaultsTo(1);
        parser.accepts("usePosVerb", "If true, uses knowledge from POS tags to add additional links to verbs")
                .withRequiredArg().ofType(Boolean.class).defaultsTo(true);
        parser.accepts("casePos", "If true, uses the casing of a token to determine its POS (lc=verb, uc=noun)")
                .withRequiredArg().ofType(Boolean.class).defaultsTo(false);
        parser.accepts("twoPass", "Whether to use the output of the dependency parsing step as additional links for a" +
                " second round of page rank.");

        parser.accepts("mweFile", "If set, uses a list of multi-word expressions together with their druid scores " +
                "during parsing. They will be preferred in the same vein as if they would match universal rules.")
                .withRequiredArg().ofType(String.class);
        parser.accepts("mweMaxTokens", "Maximum size of the search window for multi-word expressions")
                .withRequiredArg().ofType(Integer.class).defaultsTo(4);
        parser.accepts("mweMinScore", "Minimum druid score for a multi-word expression to be considered significant")
                .withRequiredArg().ofType(Float.class).defaultsTo(0.5f);
        parser.accepts("mweRemoveLinks", "If true, removes links between multi-word expressions alltogether")
                .withRequiredArg().ofType(Boolean.class).defaultsTo(false);

        parser.accepts("linkSamePosTag", "Whether to link neighboring words with the" +
                " same 2 letter POS tag prefix").withRequiredArg().ofType(Boolean.class).defaultsTo(true);
        parser.accepts("applyLinkRemovalToTwoStepNeighbors", "Whether to apply " +
                "link removal (MWE, DT, same POS) also to two step neighbors. May lead to disconnected graphs.")
                .withRequiredArg().ofType(Boolean.class).defaultsTo(true);

        parser.accepts("dtRemoveSimilar", "If true, removes links between similar terms according to a JoBimText " +
                "distributional thesaurus. Requires" +
                " dtConfigFile option set.").withRequiredArg().ofType(Boolean.class).defaultsTo(false);
        parser.accepts("dtConfigFile", "Path to a JoBimText distributional thesaurus " +
                "database configuration file in XML format. Only used if" +
                " dtRemoveSimilar is set to true.").withRequiredArg().ofType(String.class).defaultsTo("conf/dt.xml");
        parser.accepts("dtLemmatizerClass", "Lemmatizer annotator class for dtLemmatization. Omit if no lemmatization" +
                " is required").withRequiredArg().ofType(String.class).defaultsTo("");
        parser.accepts("language", "Language identifier, required for lemmatization").withRequiredArg().ofType(String
                .class).defaultsTo("en");

        parser.accepts("outputDir", "If set, uses this directory to write parser output to instead of a temporary " +
                "directory").withRequiredArg().ofType(File.class);
        parser.accepts("help", "shows this help message").forHelp();

        OptionSet options = parser.parse(args);

        if (options.has("help")) {
            parser.printHelpOn(System.out);
            return;
        }

        System.out.println(options.asMap());

        boolean useTempDir = !options.has("outputDir");

        Path outputDir = (useTempDir) ? Files.createTempDirectory
                ("junsupervised") : ((File) options.valueOf("outputDir")).toPath();
        File inputFile = (File) options.valueOf("input");
        File scriptFile = (File) options.valueOf("script");
        File keywordsFile = (File) options.valueOf("keywords");

        if (options.has("10TokensLimit") || options.has("casePos")) {
            Path preprocessedFile = outputDir.resolve("preprocessing/");

            ArrayList<AnalysisEngineDescription> engines = new ArrayList<>();
            if (options.has("10TokensLimit")) {
                engines.add(createEngineDescription(TenTokensFilter.class, TenTokensFilter.PARAM_REMOVE_PUNCTUATION,
                        options.has("removePunctuation")));
            }
            if ((Boolean) options.valueOf("casePos")) {
                engines.add(createEngineDescription(CasingVerbAnnotator.class));
            }
            engines.add(createEngineDescription(Conll2006Writer.class,
                    Conll2006Writer.PARAM_TARGET_LOCATION, preprocessedFile.toString()));

            SimplePipeline.runPipeline(createReaderDescription(Conll2006Reader.class,
                    Conll2006Reader.PARAM_SOURCE_LOCATION, inputFile.getPath(),
                    Conll2006Reader.PARAM_READ_POS, !(Boolean) options.valueOf("casePos")),
                    engines.toArray(new AnalysisEngineDescription[engines.size()]));
            inputFile = preprocessedFile.resolve(inputFile.getName() + ".conll").toFile();
        }

        ArrayList<AnalysisEngineDescription> engines = new ArrayList<>();

        if (options.has("dtLemmatizerClass")) {
            String lemmatizerClass = (String) options.valueOf("dtLemmatizerClass");
            if (!lemmatizerClass.isEmpty()) {
                engines.add(createEngineDescription((Class<AnalysisComponent>) Class.forName(lemmatizerClass)));
            }
        }

        if (options.has("universalPosMap")) {
            engines.add(createEngineDescription(UniversalPOSTagConverter.class,
                    UniversalPOSTagConverter.PARAM_PRINT_MAPPING, true,
                    UniversalPOSTagConverter.PARAM_MAPPING, ((File) options.valueOf("universalPosMap"))
                            .getAbsolutePath()));
        }

        switch ((RankingMode) options.valueOf("rankingMode")) {
            case NORMAL:
                engines.add(createParserDescription(JUnsupervisedParser.class, keywordsFile, options));
                break;

            case RANDOMIZED:
                engines.add(createParserDescription(RandomizedRankingParser.class, keywordsFile, options));
                break;

            case LINEAR:
                engines.add(createParserDescription(LinearRankingParser.class, keywordsFile, options));
                break;

            case LINEAR_REVERSE:
                engines.add(createParserDescription(LinearRankingParser.class, keywordsFile, options,
                        LinearRankingParser.PARAM_INVERT, true));
                break;

            case ORACLE:
                engines.add(createParserDescription(OracleParser.class, keywordsFile, options));
                break;

            default:
                throw new UnsupportedOperationException("Case not implemented");
        }

        engines.add(createEngineDescription(Conll2006Writer.class,
                Conll2006Writer.PARAM_TARGET_LOCATION, outputDir.toString()));


        SimplePipeline.runPipeline(
                createReaderDescription(Conll2006Reader.class,
                        Conll2006Reader.PARAM_READ_DEPENDENCY, ((RankingMode) options.valueOf("rankingMode")) ==
                                RankingMode.ORACLE,
                        Conll2006Reader.PARAM_SOURCE_LOCATION, inputFile.getPath(),
                        Conll2006Reader.PARAM_LANGUAGE, options.valueOf("language")),
                engines.toArray(new AnalysisEngineDescription[engines.size()]));

        Path predictionFile = outputDir.resolve(inputFile.getName() + ".conll");
        System.out.println("Output written to " + predictionFile.toString());


        Process process = new ProcessBuilder("perl", scriptFile.getPath(), "-q", "-p", "-g", inputFile
                .getPath(), "-s", predictionFile.toAbsolutePath().toString()).inheritIO().start();

        process.waitFor();

        if (useTempDir) {
            Files.delete(predictionFile.toAbsolutePath());
            Files.delete(outputDir.toAbsolutePath());
        }
    }

    private static AnalysisEngineDescription createParserDescription(Class<? extends JUnsupervisedParser>
                                                                             parserClass, File
                                                                             keywordsFile, OptionSet options,
                                                                     Object... additionalOptions) throws
            ResourceInitializationException {
        ArrayList<Object> configurationData = new ArrayList<>();
        configurationData.add(JUnsupervisedParser.PARAM_FUNCTION_WORDS_FILE);
        configurationData.add(keywordsFile.getPath());
        configurationData.add(JUnsupervisedParser.PARAM_APPLY_FUNCTION_WORD_LINKING_TO_FUNCTION_WORDS);
        configurationData.add(options.valueOf("linkFW2FW"));
        configurationData.add(JUnsupervisedParser.PARAM_USE_UNIVERSAL_RULES);
        configurationData.add(options.valueOf("useUniversalRules"));
        configurationData.add(JUnsupervisedParser.PARAM_USE_POS_VERB);
        configurationData.add(options.valueOf("usePosVerb"));
        configurationData.add(JUnsupervisedParser.PARAM_NEIGHBOR_LINK_COUNT);
        configurationData.add(options.valueOf("neighborLinkCount"));
        configurationData.add(JUnsupervisedParser.PARAM_TWO_PASS);
        configurationData.add(options.has("twoPass"));
        configurationData.add(JUnsupervisedParser.PARAM_LINK_SAME_POS_TAG);
        configurationData.add(options.valueOf("linkSamePosTag"));
        configurationData.add(JUnsupervisedParser.PARAM_APPLY_LINK_REMOVAL_TO_TWO_STEP_NEIGHBORS);
        configurationData.add(options.valueOf("applyLinkRemovalToTwoStepNeighbors"));
        if (options.has("mweFile")) {
            configurationData.add(JUnsupervisedParser.PARAM_MWE_FILE);
            configurationData.add(options.valueOf("mweFile"));
            configurationData.add(JUnsupervisedParser.PARAM_MWE_MAX_TOKENS);
            configurationData.add(options.valueOf("mweMaxTokens"));
            configurationData.add(JUnsupervisedParser.PARAM_MWE_SCORE);
            configurationData.add(options.valueOf("mweMinScore"));
            configurationData.add(JUnsupervisedParser.PARAM_MWE_REMOVE_LINKS);
            configurationData.add(options.valueOf("mweRemoveLinks"));
        }

        if (options.has("dtRemoveSimilar")) {
            configurationData.add(JUnsupervisedParser.PARAM_DT_REMOVE_SIMILAR);
            configurationData.add(options.valueOf("dtRemoveSimilar"));
            configurationData.add(JUnsupervisedParser.PARAM_DT_CONFIG_FILE);
            configurationData.add(options.valueOf("dtConfigFile"));
            configurationData.add(JUnsupervisedParser.PARAM_DT_USE_LEMMA);
            configurationData.add(!((String) options.valueOf("dtLemmatizerClass")).isEmpty());
        }
        Collections.addAll(configurationData, additionalOptions);

        return createEngineDescription(parserClass,
                configurationData.toArray());
    }
}
