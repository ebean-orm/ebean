package com.avaje.ebeaninternal.server.subclass;

import com.avaje.ebean.enhance.agent.ClassMeta;
import com.avaje.ebean.enhance.agent.EnhanceConstants;
import com.avaje.ebean.enhance.agent.VisitMethodParams;
import com.avaje.ebean.enhance.asm.ClassVisitor;
import com.avaje.ebean.enhance.asm.Label;
import com.avaje.ebean.enhance.asm.MethodVisitor;
import com.avaje.ebean.enhance.asm.Opcodes;

public class SubClassConstructor implements Opcodes, EnhanceConstants{

    public static void addDefault(ClassVisitor cv, ClassMeta meta) {
        VisitMethodParams params = new VisitMethodParams(cv, Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        add(params, meta);
    }
    
	public static void add(VisitMethodParams params, ClassMeta meta) {
		
		String className = meta.getClassName();
		String superClassName = meta.getSuperClassName();
		
		if (params.forcePublic()){
		    if (meta.isLog(0)){
		        meta.log(" forcing ACC_PUBLIC ");
		    }
		}
		
		MethodVisitor mv = params.visitMethod();		
		//mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(17, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "()V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(18, l1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitTypeInsn(NEW, C_INTERCEPT);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, C_INTERCEPT, "<init>", "(Ljava/lang/Object;)V");
		mv.visitFieldInsn(PUTFIELD, className, INTERCEPT_FIELD, L_INTERCEPT);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLineNumber(19, l2);
		mv.visitInsn(RETURN);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLocalVariable("this", "L"+className+";", null, l0, l3, 0);
		mv.visitMaxs(4, 1);
		mv.visitEnd();
	}
}
