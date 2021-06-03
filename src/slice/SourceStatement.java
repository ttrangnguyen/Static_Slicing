package slice;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.debug.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class SourceStatement {
    public static List<Statement> findSeedStatementsByLineNumber(CGNode n, int LineNumber) throws Exception {
        IR ir = n.getIR();
        List<Statement> seedStatements = new ArrayList<>();
//        System.out.println(Arrays.toString(ir.getInstructions()));
        for (SSAInstruction instruction : ir.getInstructions()) {
            int iindex = instruction.iIndex();
//            System.out.println(String.format("[%s] %s [%s-%s]", MappingUtil.IRIndexCastToLineNumber(n, iindex), instruction, instruction.getNumberOfDefs(), instruction.getNumberOfUses()));
            if (MappingUtil.IRIndexCastToLineNumber(n, iindex) == LineNumber) {
                seedStatements.add(new NormalStatement(n, iindex));
            }
        }
        if (seedStatements.size() <= 0) {
            Assertions.UNREACHABLE("failed to find call to " + " in " + n);
        }
        return seedStatements;
    }

    public static void dumpSlice(Collection<Statement> slice) throws InvalidClassFileException {
        for (Statement s : slice) {
            IClass declaringClass = s.getNode().getMethod().getDeclaringClass();
            String className = MappingUtil.normalizeClassName(declaringClass.getName().toString());
            int lineNumber = MappingUtil.IRIndexCastToLineNumber(s);
            if (lineNumber >= 0) {
                String key = String.format("%s.%s", className, lineNumber);
//                System.out.println(String.format("[%s] %s", lineNumber, MyECJJavaSourceAnalysisEngine.getFeatureLineInfo(key)));
                System.out.println(String.format("[%s] [%s] %s ", lineNumber, className, s));
            }
        }
    }
}
