package slice;

import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingUtil {
    private static Pattern variantNamePattern = Pattern.compile("^.+/(model_m_[^/]+)/temp_src$");
    private static String featureMappingPrefix = "//__feature_mapping__";
    private static Pattern featureMappingPattern = Pattern.compile(String.format("%s \\[(?<feature>[^]]+)\\] \\[(?<beginLine>\\d+):(?<endLine>\\d+)\\]$", featureMappingPrefix));

    public static int IRIndexToLineNumber(IR ir, int instructionIndex) throws InvalidClassFileException {
        IBytecodeMethod method = (IBytecodeMethod) ir.getMethod();
        int bytecodeIndex = method.getBytecodeIndex(instructionIndex);
        int sourceLineNum = method.getLineNumber(bytecodeIndex);
        return sourceLineNum;
    }

    public static int IRIndexCastToLineNumber(Statement s) {
        if (s.getKind() == Statement.Kind.NORMAL) {
            int instructionIndex = ((NormalStatement) s).getInstructionIndex();
            return IRIndexCastToLineNumber(s.getNode(), instructionIndex);
        }
        return -1;
    }

    public static int IRIndexCastToLineNumber(CGNode n, int instructionIndex) {
        int lineNum = (n.getMethod()).getLineNumber(instructionIndex);
        return lineNum;
    }

    public static String normalizeClassName(String className) {
        Pattern p = Pattern.compile("^[^(]+(?=\\/\\w+\\(|$)");
        Matcher m = p.matcher(className);
        if (m.find()) {
            return m.group().replaceAll("^L", "").replaceAll("\\$[A-Za-z_]+", "").replaceAll("/", ".");
        }
        return null;
    }

    public static String extractVariantName(String srcFilePath) {
        Matcher matcher = variantNamePattern.matcher(srcFilePath);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new InvalidPathException(srcFilePath, "Can't extract class name from java file path");
    }

    public static HashMap<String, SPLFeatureSourceLine> getAllFeatureMappingLineFromFiles(String sourcePath, String className) {
        Path path = Paths.get(sourcePath);
        List<String> lines = null;
        HashMap<String, SPLFeatureSourceLine> featureSourceLineMapping = new HashMap<>();
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i <= lines.size() - 1; i++) {
            String line = lines.get(i).trim();
            Matcher matcher = featureMappingPattern.matcher(line);
            if (!matcher.find()) {
                continue;
            }
            Pattern p = Pattern.compile("(\\w+)\\s*\\(");
            Matcher m = p.matcher(lines.get(i + 1));
            String methodName = "";
            if (m.find()) {
                methodName = m.group(1);
            }
            int productBeginLine = i + 2;
            int beginLine = Integer.parseInt(matcher.group("beginLine"));
            int endLine = Integer.parseInt(matcher.group("endLine"));
            for (int currentLine = beginLine; currentLine <= endLine; currentLine++) {
                SPLFeatureSourceLine featureMethod = new SPLFeatureSourceLine(sourcePath, matcher.group("feature"), className, methodName, currentLine, productBeginLine + currentLine - beginLine);
                featureSourceLineMapping.put(featureMethod.getProductLineKey(), featureMethod);
            }
        }
        return featureSourceLineMapping;
    }

    public static boolean isExcludedByDefinedRules(Statement s){
        return (!s.isInPurifiedTestCaseClass() && isClassInitializeAttributeStatement(s)) || (s.isInPurifiedTestCaseClass() && !s.isPurifiedTestCaseStatement());
    }

    public static boolean isClassInitializeAttributeStatement(Statement s) {
        return MyECJJavaSourceAnalysisEngine.getFeatureLineInfo(s.getLineKey()) == null;
    }
}
