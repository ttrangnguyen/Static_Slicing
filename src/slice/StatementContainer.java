package slice;

import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.types.FieldReference;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StatementContainer {

    private static HashMap<FieldReference, ArrayList<Statement>> getFieldMap = new HashMap<>();
    private static HashMap<Statement, HashSet<Statement>> forwardSliceMap = new HashMap<>();
    private static HashMap<Statement, HashSet<Statement>> backwardSliceMap = new HashMap<>();
    public static HashSet<String> finalSliceBasedContainer = new HashSet<>();
    public static HashSet<String> executedFeatureLineSet = new HashSet<>();


    public static void clear() {
        executedFeatureLineSet.clear();
        getFieldMap.clear();
        forwardSliceMap.clear();
        backwardSliceMap.clear();
        finalSliceBasedContainer.clear();
    }

    public static void initExecutedFeatureLine(String sourceDir) throws IOException {
        executedFeatureLineSet = new HashSet<>(FileUtil.getExecutedFeatureLinesFromRelatingCoverageFile(sourceDir));
    }

    public static boolean isFeatureLineExecuted(String featureLine) {
        return executedFeatureLineSet.contains(featureLine);
    }

    public static boolean isStatementExecuted(Statement statement) {
        if (statement.isInPurifiedTestCaseClass()){
            return true;
        }
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

    public static void addFeatureLineToFinalSlicedBaseContainer(HashSet<String> featureLines){
        finalSliceBasedContainer.addAll(featureLines);
    }

    public static HashSet<String> getFinalSliceBasedContainer(){
        return finalSliceBasedContainer;
    }

    public static void addGetField(FieldReference fieldRef, Statement statement) {
        ArrayList<Statement> getFieldStatements = getFieldMap.get(fieldRef);
        if (getFieldStatements == null) {
            getFieldStatements = initGetFieldStatementList(fieldRef);
        }
        if (!isStatementExecuted(statement)) {
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
        for (Map.Entry<Statement, HashSet<Statement>> entry : backwardSliceMap.entrySet()) {
            Statement s = entry.getKey();
            String lineKey = entry.getKey().getLineKey();
            HashSet<String> currentMergeLines = mergedSliceMap.computeIfAbsent(lineKey, k -> new HashSet<>());
            currentMergeLines.addAll(entry.getValue().stream().map(Statement::getLineKey).collect(Collectors.toList()));
        }
        return mergedSliceMap.entrySet();
    }

    public static HashMap<String, HashSet<Statement>> lineByKeyForwardSliceContainer() {
        return lineByKeySliceContainer(forwardSliceMap);
    }

    public static HashMap<String, HashSet<Statement>> lineByKeyBackwardSliceContainer() {
        return lineByKeySliceContainer(backwardSliceMap);
    }

    public static HashMap<String, HashSet<Statement>> lineByKeySliceContainer(HashMap<Statement, HashSet<Statement>> sliceMap) {
        HashMap<String, HashSet<Statement>> mergedSliceMap = new HashMap<>();
        for (Map.Entry<Statement, HashSet<Statement>> entry : sliceMap.entrySet()) {
            String lineKey = entry.getKey().getLineKey();
            HashSet<Statement> currentMergeLines = mergedSliceMap.computeIfAbsent(lineKey, k -> new HashSet<>());
            currentMergeLines.addAll(entry.getValue());
        }
        return mergedSliceMap;
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
        if (!isStatementExecuted(seedStatement)) {
            return;
        }
        slicingStatements.addAll(slicedStatements.stream().filter(StatementContainer::isStatementExecuted).collect(Collectors.toList()));
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
