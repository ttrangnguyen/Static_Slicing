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
    private static Pattern javaFilePathPattern = Pattern.compile("^.+model_m_[^//]+/src/(.+)\\.java$");
    public static final HashMap<String, List<String[]>> ByFileSPCs = new HashMap<>();

    public static void readUniqueSPCsFromFile() {
        String defaultSPCsFilePath = "/Users/tuanngokien/Desktop/Software_Analysis/configurable_system/dataset/4wise-AJStats-FH-JML/_MultipleBugs_.NOB_1.ID_1/spc_0.log";
        String SPCsFilePath = System.getProperty("spc_path", defaultSPCsFilePath);
        HashSet<String> uniqueLines = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(SPCsFilePath))) {
            stream.forEach(uniqueLines::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String line : uniqueLines) {
            if (!line.contains(";")) {
                continue;
            }
            String[] metadata = line.split(";");
            List<String[]> SPCList = ByFileSPCs.computeIfAbsent(metadata[1].trim(), k -> new ArrayList<>());
            String[] SPC = metadata[0].split(",");
            String[] prunedSPC = Arrays.stream(SPC).filter(fs -> !fs.contains("#")).toArray(String[]::new);
            if (prunedSPC.length > 0) {
//                System.out.println(Arrays.toString(prunedSPC));
                SPCList.add(prunedSPC);
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
