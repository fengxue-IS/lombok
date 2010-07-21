package lombok.eclipse.agent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;

public class PatchFixes {
	public static int fixRetrieveStartingCatchPosition(int in) {
		return in;
	}
	
	public static final int ALREADY_PROCESSED_FLAG = 0x800000;	//Bit 24
	
	public static boolean checkBit24(Object node) throws Exception {
		int bits = (Integer)(node.getClass().getField("bits").get(node));
		return (bits & ALREADY_PROCESSED_FLAG) != 0;
	}
	
	/**
	 * XXX LIVE DEBUG
	 * 
	 * Once in a blue moon eclipse throws a NullPointerException while editing a file. Can't reproduce it while running eclipse in a debugger,
	 * but at least this way we patch the problem to be a bit more specific in the error that should then appear.
	 */
	public static boolean debugPrintStateOfScope(Object in) throws Exception {
		/* this.scope.enclosingSourceType().sourceName */
		Object scope = in.getClass().getField("scope").get(in);
		String msg = null;
		if (scope == null) msg = "scope itself is null";
		else {
			Object sourceTypeBinding = scope.getClass().getMethod("enclosingSourceType").invoke(scope);
			if (sourceTypeBinding == null) msg = "scope.enclosingSourceType() is null";
		}
		
		if (msg != null) throw new NullPointerException(msg);
		return false;
	}
	
	public static boolean skipRewritingGeneratedNodes(org.eclipse.jdt.core.dom.ASTNode node) throws Exception {
		return ((Boolean)node.getClass().getField("$isGenerated").get(node)).booleanValue();
	}
	
	public static void setIsGeneratedFlag(org.eclipse.jdt.core.dom.ASTNode domNode,
			org.eclipse.jdt.internal.compiler.ast.ASTNode internalNode) throws Exception {
		if (internalNode == null || domNode == null) return;
		boolean isGenerated = internalNode.getClass().getField("$generatedBy").get(internalNode) != null;
		if (isGenerated) {
			domNode.getClass().getField("$isGenerated").set(domNode, true);
			domNode.setFlags(domNode.getFlags() & ~ASTNode.ORIGINAL);
		}
	}
	
	public static void setIsGeneratedFlagForSimpleName(SimpleName name, Object internalNode) throws Exception {
		if (internalNode instanceof org.eclipse.jdt.internal.compiler.ast.ASTNode) {
			if (internalNode.getClass().getField("$generatedBy").get(internalNode) != null) {
				name.getClass().getField("$isGenerated").set(name, true);
			}
		}
	}
	
	public static IMethod[] removeGeneratedMethods(IMethod[] methods) throws Exception {
		List<IMethod> result = new ArrayList<IMethod>();
		for (IMethod m : methods) {
			if (m.getNameRange().getLength() > 0) result.add(m);
		}
		return result.size() == methods.length ? methods : result.toArray(new IMethod[0]);
	}
	
	public static SimpleName[] removeGeneratedSimpleNames(SimpleName[] in) throws Exception {
		Field f = SimpleName.class.getField("$isGenerated");
		
		int count = 0;
		for (int i = 0; i < in.length; i++) {
			if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) count++;
		}
		if (count == in.length) return in;
		SimpleName[] newSimpleNames = new SimpleName[count];
		count = 0;
		for (int i = 0; i < in.length; i++) {
			if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) newSimpleNames[count++] = in[i];
		}
		return newSimpleNames;
	}
}
