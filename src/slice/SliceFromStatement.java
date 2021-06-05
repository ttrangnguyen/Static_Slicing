package slice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.CancelException;

import static com.ibm.wala.ipa.slicer.PDG.statement2SSAInstruction;

public class SliceFromStatement {

    public static void sliceSPLSystem() throws Exception {
        FileUtil.readUniquePTSsFromFile();
        for (String variantSourceDir : FileUtil.ByFilePTSs.keySet()) {
            System.out.printf("Slicing path [%s]%n", variantSourceDir);
            sliceFromSource(variantSourceDir);
        }
        printFinalFeatureInteractions();
    }

    public static void sliceFromSource(String srcDir) throws Exception {
        init(srcDir);
        System.setProperty("wala.jdt.quiet", "true");
        File f = new File(srcDir);
        MyECJJavaSourceAnalysisEngine engine = new MyECJJavaSourceAnalysisEngine();
        engine.addSystemDependencies();

        engine.addSourceModule(new SourceDirectoryTreeModule(f));
        engine.setExclusionsFile("Java60RegressionExclusions.txt");
        engine.buildAnalysisScope();
        CallGraph cg = engine.buildDefaultCallGraph();

        ModRef<InstanceKey> modRef = ModRef.make();
        final PointerAnalysis<? super InstanceKey> pa = engine.getPointerAnalysis();
        SDG<?> sdg = new SDG<>(cg, pa, modRef, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NO_EXCEPTIONAL_EDGES, null);
        sdg.eagerConstruction();

        sliceProgram(sdg, srcDir);
        reset();
    }

    private static void sliceProgram(SDG<?> sdg, String srcDir) throws CancelException, IOException {
        List<Statement> seedStatements = getAllStatements(sdg);
//        System.out.println(seedStatements.stream().map(Statement::getLineKey).collect(Collectors.toList()));
        sliceFromSeedStatements(sdg, seedStatements);
//        printSliceMap();
        isolateStatementsBySliceBasedMethod(srcDir);
        saveSliceBasedOutput(srcDir);
    }

    public static void init(String srcDir) throws IOException {
        StatementContainer.initExecutedFeatureLine(srcDir);
    }

    public static void reset() {
        StatementContainer.clear();
    }

    private static void printSliceMap() {
        for (Map.Entry<String, HashSet<String>> entry : StatementContainer.sliceEntryIterable()) {
            System.out.println("--------------");
            System.out.printf("[%s]%n", entry.getKey());
            for (String s : entry.getValue()) {
                System.out.printf("-> %s%n", s);
//                System.out.printf("-> %s [%s]%n", s, MyECJJavaSourceAnalysisEngine.getFeatureLineInfo(s));
            }
            System.out.println("--------------");
        }
    }


    private static void isolateStatementsBySliceBasedMethod(String srcDir) throws IOException {
        HashMap<String, HashSet<Statement>> lineByKeyBackwardSliceContainer = StatementContainer.lineByKeyBackwardSliceContainer();
        List<String> assertionLines = FileUtil.ByFilePTSs.get(srcDir);
//        System.out.println(lineByKeyBackwardSliceContainer);
        for (String assertionLine : assertionLines) {
            String assertionLineClassName = assertionLine.split("\\.\\d+$")[0];

            // backward from marked assertion statement
            System.out.println(assertionLine + " " + lineByKeyBackwardSliceContainer.get(assertionLine));
            HashSet<Statement> influenceStatements = new HashSet<>(lineByKeyBackwardSliceContainer.get(assertionLine));

            // forward from test former statements for avoid missing buggy statements
            HashSet<Statement> influencedByTestStatements = new HashSet<>();
            for (Statement s : influenceStatements) {
                if (s.isPurifiedTestCaseStatement()) {
                    String testStatement = s.getLineKey();
                    String testStatementClassName = testStatement.split("\\.\\d+$")[0];
                    if (!testStatementClassName.equals(assertionLineClassName)) {
                        continue;
                    }
                    influencedByTestStatements.addAll(StatementContainer.obtainForwardSlicingStatements(s));
                }
            }
            influenceStatements.addAll(influencedByTestStatements);
            StatementContainer.addFeatureLineToFinalSlicedBaseContainer(new HashSet<>(influenceStatements.stream().filter(s -> !s.isPurifiedTestCaseStatement()).map(Statement::getFeatureLineKey).collect(Collectors.toList())));
        }
    }

    private static List<String> finalOutput = new ArrayList<>();

    private static void printFinalFeatureInteractions() throws IOException {
        String outputData = String.format("{%s}", String.join(", ", finalOutput));
        String slicingOutputPath = System.getProperty("slicing_output_path");
        if (slicingOutputPath != null) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(slicingOutputPath));
            writer.write(outputData);
            writer.close();
        } else {
            System.out.println(outputData);
        }
    }

    private static void saveSliceBasedOutput(String srcDir) {
        StringBuilder output = new StringBuilder();
        output.append(String.format("\"%s\": [", MappingUtil.extractVariantName(srcDir)));
        HashSet<String> finalSliceBasedContainer = StatementContainer.getFinalSliceBasedContainer();
        Iterator<String> lines = finalSliceBasedContainer.iterator();
        while (lines.hasNext()) {
            String featureLine = lines.next();
            output.append(String.format("\"%s\"", featureLine));
            if (lines.hasNext()) {
                output.append(",");
            }
        }
        output.append("]");
        finalOutput.add(output.toString());
    }

    public static void sliceFromSeedStatements(SDG<?> sdg, List<Statement> seedStatements) throws CancelException {
        List<Statement> putStatementList = new ArrayList<>();
        for (Statement statement : seedStatements) {
            StatementContainer.addForwardSlicingStatements(statement, Slicer.computeForwardSlice(sdg, statement));
            StatementContainer.addBackwardSlicingStatements(statement, Slicer.computeBackwardSlice(sdg, statement));
            SSAInstruction inst = statement2SSAInstruction(statement.getNode().getIR().getInstructions(), statement);
            if (inst instanceof SSAPutInstruction && StatementContainer.isStatementExecuted(statement)) {
                putStatementList.add(statement);
            }
        }


//        System.out.println("-------computeAttributeSlice-------");
        slicePutAttributeStatements(putStatementList);


    }

    public static void slicePutAttributeStatements(List<Statement> putStatementList) {

        HashMap<Statement, HashSet<Statement>> slicedGetStatementMapping = new HashMap<>();
        for (Statement putStatement : putStatementList) {
            List<FieldReference> slicedPutField = new ArrayList<>();
            HashSet<Statement> slicedStatements = slicePutStatement(putStatement, slicedPutField);
            StatementContainer.addPropagatedForwardSlicedStatement(putStatement, slicedStatements);
            slicedGetStatementMapping.put(putStatement, slicedStatements);
        }

        //inverted add statements for backward slicing [get stmt] -> [put stmt]
        for (Statement putStatement : slicedGetStatementMapping.keySet()) {
            HashSet<Statement> backwardSlicedStatements = new HashSet<Statement>() {{
                add(putStatement);
            }};
            for (Statement getStatement : slicedGetStatementMapping.get(putStatement)) {
                StatementContainer.addPropagatedBackwardSlicedStatement(getStatement, backwardSlicedStatements);
            }
        }

    }

    public static HashSet<Statement> slicePutStatement(Statement putStatement, List<FieldReference> slicedPutField) {
        HashSet<Statement> finalForwardStatements = new HashSet<>();
        SSAInstruction inst = statement2SSAInstruction(putStatement.getNode().getIR().getInstructions(), putStatement);
        if (inst instanceof SSAPutInstruction) {
            SSAPutInstruction putInst = (SSAPutInstruction) inst;
            FieldReference fieldRef = putInst.getDeclaredField();
            if (!(slicedPutField.contains(fieldRef))) {
                ArrayList<Statement> getFieldStatements = StatementContainer.obtainGetFieldStatements(fieldRef);
                slicedPutField.add(fieldRef);
                for (Statement getFieldStatement : getFieldStatements) {
                    HashSet<Statement> currentForwardStatements = StatementContainer.obtainForwardSlicingStatements(getFieldStatement);
                    finalForwardStatements.addAll(currentForwardStatements);
                }

                HashSet<Statement> propagateFinalForwardStatements = new HashSet<>();
                for (Statement slicedStatement : finalForwardStatements) {
                    HashSet<Statement> currentForwardStatements = slicePutStatement(slicedStatement, slicedPutField);
                    propagateFinalForwardStatements.addAll(currentForwardStatements);
                }
                finalForwardStatements.addAll(propagateFinalForwardStatements);
            }
        }
        return finalForwardStatements;
    }


    private static List<Statement> getAllStatements(SDG<?> sdg) {
        List<Statement> allStatements = new ArrayList<>();
        for (Statement s : sdg) {
            IMethod method = s.getNode().getMethod();
            if (s.getKind() != Statement.Kind.NORMAL
                    || !method.getDeclaringClass().getClassLoader().getReference().equals(JavaSourceAnalysisScope.SOURCE)
                    || MappingUtil.isExcludedByDefinedRules(s)) {
                continue;
            }
            allStatements.add(s);
//            System.out.println(String.format("[%s] %s", MappingUtil.IRIndexCastToLineNumber(s), s));
        }
        return allStatements;
    }
}
