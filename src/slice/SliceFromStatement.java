package slice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
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
        FileUtil.readUniqueSPCsFromFile();
        for (String variantSourceDir : FileUtil.ByFileSPCs.keySet()) {
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
        sliceFromSeedStatements(sdg, seedStatements);
//        printSliceMap();
        calculateEachSourceFeatureInteractions(srcDir);
        saveFeatureInteractionsOutput(srcDir);
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
                System.out.printf("-> %s [%s]%n", s, MyECJJavaSourceAnalysisEngine.getFeatureLineInfo(s));
            }
            System.out.println("--------------");
        }
    }


    private static void calculateEachSourceFeatureInteractions(String srcDir) throws IOException {
        HashMap<String, HashSet<String>> featureInfluenceContainer = StatementContainer.featureForwardInfluenceContainer();
        List<String[]> SPCs = FileUtil.ByFileSPCs.get(srcDir);
        HashMap<String, AtomicInteger> interactionLineCounter = StatementContainer.interactionLineCounter;
        ArrayList<String> executedFeatureLines = FileUtil.getExecutedFeatureLinesFromRelatingCoverageFile(srcDir);

        for (String[] SPC : SPCs) {
            if (SPC.length <= 0) {
                continue;
            }
            //find (forward) statements that implemented the interactions between features
            Set<String> interactionLines = featureInfluenceContainer.get(SPC[0]);
            if (interactionLines == null) {
                continue;
            }
            for (int i = 1; i < SPC.length; i++) {
                Set<String> currentInfluencedLine = featureInfluenceContainer.get(SPC[i]);
                if (currentInfluencedLine == null) {
                    break;
                }
                interactionLines = Sets.intersection(interactionLines, currentInfluencedLine);
                if (interactionLines.size() <= 0) {
                    break;
                }
            }
            if (interactionLines.size() <= 0) {
                continue;
            }

            HashMap<String, Statement> lineKeyToStatementMapping = StatementContainer.getLineKeyToForwardStatementMapping();
            String[] interactionLineArray = interactionLines.toArray(new String[0]);

            //get backward sliced statements from each interaction line
            for (String lineKey : interactionLineArray) {
                Statement seedStatement = lineKeyToStatementMapping.get(lineKey);
                Set<String> backwardLines = StatementContainer.obtainBackwardSlicingStatements(seedStatement).stream().map(Statement::getLineKey).collect(Collectors.toSet());
                interactionLines = Sets.union(interactionLines, backwardLines);
            }

            //mapping final statements to feature-level line key and count how many each one appears in SPC interactions

            for (String line : interactionLines) {
                String featureLine;
                try {
                    featureLine = MyECJJavaSourceAnalysisEngine.getFeatureLineInfo(line).toString();
                } catch (NullPointerException e) {
                    continue;
                }
                if (!StatementContainer.isFeatureLineExecuted(featureLine)) {
                    continue;
                }
                interactionLineCounter.putIfAbsent(featureLine, new AtomicInteger(0));
                interactionLineCounter.get(featureLine).incrementAndGet();
            }
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

    private static void saveFeatureInteractionsOutput(String srcDir) {
        StringBuilder output = new StringBuilder();
        output.append(String.format("\"%s\": {", MappingUtil.extractVariantName(srcDir)));
        HashMap<String, AtomicInteger> interactionLineCounter = StatementContainer.interactionLineCounter;
        Iterator<String> lines = interactionLineCounter.keySet().iterator();
        while (lines.hasNext()) {
            String featureLine = lines.next();
            output.append(String.format("\"%s\": {\"num_interactions\": %s}", featureLine, interactionLineCounter.get(featureLine)));
            if (lines.hasNext()) {
                output.append(",");
            }
        }
        output.append("}");
        finalOutput.add(output.toString());
    }

    public static void sliceFromSeedStatements(SDG<?> sdg, List<Statement> seedStatements) throws CancelException {
        List<Statement> putStatementList = new ArrayList<>();
//        System.out.println("-------computeSlice-------");
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
                    || !MappingUtil.isClassInitializeAttributeStatement(s)) {
                continue;
            }
            allStatements.add(s);
//            System.out.println(String.format("[%s] %s", MappingUtil.IRIndexCastToLineNumber(s), s));
        }
        return allStatements;
    }
}
