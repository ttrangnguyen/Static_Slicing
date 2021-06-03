package slice;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;

public class BytecodeStatement {
	public static Statement findCallTo(CGNode n, String methodName) {
		IR ir = n.getIR();
//		System.out.println(ir.toString());
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			if (s instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction call = (SSAInvokeInstruction) s;
				if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
					
					IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
					Assertions.productionAssertion(indices.size() == 1, 
							"expected 1 but got " + indices.size());
					return new NormalStatement(n, indices.intIterator().next());
				}
			}
		
		}
		Assertions.UNREACHABLE("Failed to find call to " + methodName + " in " + n);	
		return null;
	}

	public static Statement findSeedStatementByLineNumber(CGNode n, int LineNumber) throws InvalidClassFileException {
		IR ir = n.getIR();
		for (int i = 0 ; i < ir.getInstructions().length ; i ++) {
			System.out.println(MappingUtil.IRIndexToLineNumber(ir , i));
			if (MappingUtil.IRIndexToLineNumber(ir , i) == LineNumber) {
				System.out.println(MappingUtil.IRIndexToLineNumber(ir , i) + " | " + n.toString());
				return new NormalStatement(n , i);
			}
		}
		Assertions.UNREACHABLE("failed to find call to "  + " in " + n);
		return null;
	}
	
	public static Statement findReturn(CGNode n) throws InvalidClassFileException {
		IR ir = n.getIR();
//		System.out.println(ir.toString());
		SSAInstruction previousS = null;
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			System.out.println(s.toString() + " | " + MappingUtil.IRIndexCastToLineNumber(n, s.iIndex()));

//			if (IRIdexToLineNumber(n.getIR(), s.iIndex()) <= 37){
//				previousS = s;
//			}else{
//				return new NormalStatement(n, previousS.iIndex());
//			}

//			if (s instanceof SSAReturnInstruction) {
//                System.out.println(s.toString());
//				SSAReturnInstruction call = (SSAReturnInstruction) s;
//				System.out.println(new NormalStatement(n, ((SSAReturnInstruction) s).iindex));
				return new NormalStatement(n, s.iIndex());
//			}
		}
		return new NormalStatement(n, previousS.iIndex());
	}

	public static void dumpSlice(Collection<Statement> slice, String callname) throws InvalidClassFileException {
		for (Statement s : slice) {
			if (s.getKind() == Statement.Kind.NORMAL)
				System.out.println(s.toString());
			CGNode node = s.getNode();
			//come from wala net:
			//http://wala.sourceforge.net/wiki/index.php/UserGuide:MappingToSourceCode#From_Slices_to_source_line_numbers

			if (s.getKind() == Statement.Kind.NORMAL) {
				int bcIndex, instructionIndex = ((NormalStatement) s).getInstructionIndex();
				IMethod met = s.getNode().getMethod();
				try {
					bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
					try {
						int src_line_number = s.getNode().getMethod().getLineNumber(bcIndex);
						System.err.println("Source line number = " + src_line_number);
					} catch (Exception e) {
						System.err.println("Bytecode index no good");
						System.err.println(e.getMessage());
					}
				} catch (Exception e) {
					System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
					System.err.println(e.getMessage());
				}
			}
		}
	}
}
