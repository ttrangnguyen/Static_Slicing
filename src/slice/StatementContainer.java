package slice;

import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.types.FieldReference;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StatementContainer {

    private static HashMap<FieldReference, ArrayList<Statement>> getFieldMap = new HashMap<>();
    private static HashMap<Statement, HashSet<Statement>> forwardSliceMap = new HashMap<>();
    private static HashMap<Statement, HashSet<Statement>> backwardSliceMap = new HashMap<>();
    public static HashMap<String, AtomicInteger> interactionLineCounter = new HashMap<>();
    public static HashSet<String> executedFeatureLineSet = new HashSet<>();


    public static void clear() {
        executedFeatureLineSet.clear();
        getFieldMap.clear();
        forwardSliceMap.clear();
        backwardSliceMap.clear();
        interactionLineCounter.clear();
    }

    public static void initExecutedFeatureLine(String sourceDir) throws IOException {
        executedFeatureLineSet = new HashSet<>(FileUtil.getExecutedFeatureLinesFromRelatingCoverageFile(sourceDir));
    }

    public static boolean isFeatureLineExecuted(String featureLine) {
        return executedFeatureLineSet.contains(featureLine);
    }

    public static boolean isStatementExecuted(Statement statement) {
        return isFeatureLineExecuted(MyECJJavaSourceAnalysisEngine.getFeatureLineInfo(statement.getLineKey()).toString());
    }

    public static ArrayList<Statement> obtainGetFieldStatements(FieldReference fieldRef) {
        ArrayList<Statement> getFieldStatements = getFieldMap.get(fieldRef);
        if (getFieldStatements != null) {
            return getFieldStatements;
        } else {
            return new ArrayList<>();
        }
    }

    public static void addGetField(FieldReference fieldRef, Statement statement) {
        ArrayList<Statement> getFieldStatements = getFieldMap.get(fieldRef);
        if (getFieldStatements == null) {
            getFieldStatements = initGetFieldStatementList(fieldRef);
        }
        if(!isStatementExecuted(statement)){
            return;
        }
        getFieldStatements.add(statement);
    }

    private static ArrayList<Statement> initGetFieldStatementList(FieldReference fieldRef) {
        ArrayList<Statement> newGetFieldStatementList = new ArrayList<>();
        getFieldMap.put(fieldRef, newGetFieldStatementList);
        return newGetFieldStatementList;
    }

    public static Set<Map.Entry<String, HashSet<String>>> sliceEntryIterable() {
        ///hey
        //you
        //missed
        //me

        HashMap<String, HashSet<String>> mergedSliceMap = new HashMap<>();
        for (Map.Entry<Statement, HashSet<Statement>> entry : forwardSliceMap.entrySet()) {
            String lineKey = entry.getKey().getLineKey();
            HashSet<String> currentMergeLines = mergedSliceMap.computeIfAbsent(lineKey, k -> new HashSet<>());
            currentMergeLines.addAll(entry.getValue().stream().map(Statement::getLineKey).collect(Collectors.toList()));
        }
        return mergedSliceMap.entrySet();
    }


    public static HashMap<String, HashSet<String>> featureForwardInfluenceContainer() {

        HashMap<String, HashSet<String>> mergedSliceMap = new HashMap<>();
        for (Map.Entry<Statement, HashSet<Statement>> entry : forwardSliceMap.entrySet()) {
            String lineKey = entry.getKey().getLineKey();
            SPLFeatureSourceLine featureSourceLine = MyECJJavaSourceAnalysisEngine.getFeatureLineInfo(lineKey);
            if (featureSourceLine == null) {
                continue;
            }
//            System.out.println(entry.getKey().getNode().getMethod().getDeclaringClass().getSourceFileName() + " " + lineKey);
            HashSet<String> currentMergeLines = mergedSliceMap.computeIfAbsent(featureSourceLine.getFeatureName(), k -> new HashSet<>());
            currentMergeLines.addAll(entry.getValue().stream().map(Statement::getLineKey).collect(Collectors.toList()));
        }
        return mergedSliceMap;
    }

    public static HashMap<String, Statement> getLineKeyToForwardStatementMapping() {
        HashMap<String, Statement> lineKeyToStatementMapping = new HashMap<>();
        for (Statement statement : forwardSliceMap.keySet()) {
            lineKeyToStatementMapping.put(statement.getLineKey(), statement);
        }
        return lineKeyToStatementMapping;
    }

    public static HashSet<Statement> obtainForwardSlicingStatements(Statement seedStatement) {
        return obtainSlicingStatements(forwardSliceMap, seedStatement);
    }

    public static HashSet<Statement> obtainBackwardSlicingStatements(Statement seedStatement) {
        return obtainSlicingStatements(backwardSliceMap, seedStatement);
    }

    public static HashSet<Statement> obtainSlicingStatements(HashMap<Statement, HashSet<Statement>> sliceMap, Statement seedStatement) {
        return sliceMap.get(seedStatement);
    }

    public static void addForwardSlicingStatements(Statement seedStatement, Collection<Statement> slicedStatements) {
        addSlicingStatements(forwardSliceMap, seedStatement, slicedStatements);
    }

    public static void addBackwardSlicingStatements(Statement seedStatement, Collection<Statement> slicedStatements) {
        addSlicingStatements(backwardSliceMap, seedStatement, slicedStatements);
    }

    private static void addSlicingStatements(HashMap<Statement, HashSet<Statement>> sliceMap, Statement seedStatement, Collection<Statement> slicedStatements) {
        HashSet<Statement> slicingStatements = sliceMap.get(seedStatement);
        if (slicingStatements == null) {
            slicingStatements = initSlicingStatementSet(sliceMap, seedStatement);
        }
        if(!isStatementExecuted(seedStatement)){
            return;
        }
        slicingStatements.addAll(slicedStatements);
    }

    public static void addPropagatedForwardSlicedStatement(Statement seedStatement, Collection<Statement> slicedStatements) {
        addPropagatedStatements(forwardSliceMap, seedStatement, slicedStatements);
    }

    public static void addPropagatedBackwardSlicedStatement(Statement seedStatement, Collection<Statement> slicedStatements) {
        addPropagatedStatements(backwardSliceMap, seedStatement, slicedStatements);
    }

    private static void addPropagatedStatements(HashMap<Statement, HashSet<Statement>> sliceMap, Statement seedStatement, Collection<Statement> slicedStatements) {
        addSlicingStatements(sliceMap, seedStatement, slicedStatements);
        for (Map.Entry<Statement, HashSet<Statement>> entry : sliceMap.entrySet()) {
            if (entry.getValue().contains(seedStatement)) {
                entry.getValue().addAll(slicedStatements);
            }
        }
    }

    private static HashSet<Statement> initSlicingStatementSet(HashMap<Statement, HashSet<Statement>> sliceMap, Statement seedStatement) {
        HashSet<Statement> newSlicingStatementSet = new HashSet<>();
        sliceMap.put(seedStatement, newSlicingStatementSet);
        return newSlicingStatementSet;
    }
}
