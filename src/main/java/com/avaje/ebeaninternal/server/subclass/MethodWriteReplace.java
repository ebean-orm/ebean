package com.avaje.ebeaninternal.server.subclass;

import com.avaje.ebean.enhance.agent.ClassMeta;
import com.avaje.ebean.enhance.agent.EnhanceConstants;
import com.avaje.ebean.enhance.asm.ClassVisitor;
import com.avaje.ebean.enhance.asm.Label;
import com.avaje.ebean.enhance.asm.MethodVisitor;
import com.avaje.ebean.enhance.asm.Opcodes;

/**
 * Add a writeReplace method to support optional serialization to vanilla beans.
 * 
 * <pre><code>
 * private Object writeReplace() throws ObjectStreamException {
 * 	return ebeanIntercept.writeReplaceIntercept();
 * }
 * </code></pre>
 */
public class MethodWriteReplace implements Opcodes, EnhanceConstants {

	/**
	 * Add a writeReplace() method.
	 */
	public static void add(ClassVisitor cv, ClassMeta classMeta) {

		MethodVisitor mv = cv.visitMethod(ACC_PRIVATE, "writeReplace", "()Ljava/lang/Object;",
				null, new String[] { "java/io/ObjectStreamException" });

		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(1, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, classMeta.getClassName(), INTERCEPT_FIELD, L_INTERCEPT);
		mv.visitMethodInsn(INVOKEVIRTUAL, C_INTERCEPT, "writeReplaceIntercept","()Ljava/lang/Object;");

		mv.visitInsn(ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L"+classMeta.getClassName()+";", null, l0, l1, 0);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

	}
}
