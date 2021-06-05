package slice;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileUtil {
    private static Pattern javaFilePathPattern = Pattern.compile("^.+model_m_[^//]+/temp_src/(.+)\\.java$");
    public static final HashMap<String, List<String>> ByFilePTSs = new HashMap<>();

    public static void readUniquePTSsFromFile() {
        String defaultPTSsFilePath = "/Users/tuanngokien/Desktop/Software_Analysis/configurable_system/tools/prog-slice/pts.report.log";
        String PTSsFilePath = System.getProperty("pts_path", defaultPTSsFilePath);
        HashSet<String> uniqueLines = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(PTSsFilePath))) {
            stream.forEach(uniqueLines::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String line : uniqueLines) {
            if (!line.contains(";")) {
                continue;
            }
            String[] metadata = line.split(";");
            List<String> assertionLineList = ByFilePTSs.computeIfAbsent(metadata[1].trim(), k -> new ArrayList<>());
            String[] currentAssertionLines = metadata[0].split(",");
            if (currentAssertionLines.length > 0) {
                assertionLineList.addAll(Arrays.asList(currentAssertionLines));
            }
        }
    }

    public static ArrayList<String> getExecutedFeatureLinesFromRelatingCoverageFile(String sourceCodeDir) throws IOException {
        String defaultCoverageFileName = "spectrum_failed_coverage.xml";
        String coverageFileName = System.getProperty("coverage_file_name", defaultCoverageFileName);
        Path coverageFilePath = Paths.get(sourceCodeDir, "..", "coverage", coverageFileName);
        FileInputStream fis = new FileInputStream(coverageFilePath.toFile());
        Document coverageDoc = Jsoup.parse(fis, null, "", Parser.xmlParser());
        Elements lines = coverageDoc.select("line[count]");
        ArrayList<String> featureLines = new ArrayList<>();
        for (Element line : lines) {
            int executedCount = Integer.parseInt(line.attr("count"));
            if (executedCount <= 0) {
                continue;
            }
            String featureClass = line.attr("featureClass");
            String featureLineNum = line.attr("featureLineNum");
            String featureLine = String.format("%s.%s", featureClass, featureLineNum);
            featureLines.add(featureLine);
        }
        return featureLines;
    }


    private static String extractRelativePathFromJavaFilePath(String javaFilePath) {
        Matcher matcher = javaFilePathPattern.matcher(javaFilePath);
        if (matcher.find()) {
            String match = matcher.group(1);
            return match;
        }
        throw new InvalidPathException(javaFilePath, "Can't extract class name from java file path");
    }

    public static String convertRelativeJavaFilePathToClass(String javaFilePath) {
        return extractRelativePathFromJavaFilePath(javaFilePath).replace("/", ".");
    }
}
