/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.subclass;


/**
 * Used to generate a subclass based on a bean.
 * <p>
 * It does not have the fields or private methods of the read class. It replaces
 * the method code with calls to super instead. It may need to add hashCode()
 * and equals() methods to make sure a reference is loaded prior to either of
 * these methods being called. It uses writeReplace() to modify the
 * serialisation.
 * </p>
 */
public class SubClassGenerator {// extends ClassAdapter implements Opcodes, GenConstants {

//    private static final Logger logger = LogFactory.get(SubClassGenerator.class);
//    
//    boolean isInterceptFieldAdded = false;
//
//    boolean isAddClonable = true;
//    
//    boolean superHasEquals = false;
//    
//    ClassInfo info;
//
//    MethodInfo methodInfo;
//
//    boolean hasSuperClass;
//    
//    /**
//     * Create with the ClassInfo.
//     */
//    public SubClassGenerator(ClassVisitor cv, ClassInfo info) {
//        super(cv);
//        this.info = info;
//        this.methodInfo = info.getMethodInfo();
//    }
//
//    /**
//     * Create the class definition replacing the className and super class.
//     */
//    public void visit(int version, int access, String name, String signature, String superName,
//            String[] interfaces) {
//
//        // Note: These have slashes rather than periods!!
//        String className = name+info.getSuffix();
//        String superClassName;
//        if ("java/lang/Object".endsWith(superName)){
//        	superClassName = name;
//        } else {
//        	hasSuperClass = true;
//        	superClassName = name;//superName+info.getSuffix();
//        }
// 
//        info.setClassName(className);
//        info.setSuperClassName(superClassName);
//        
//         
//        // Note: interfaces can be an empty array but not null
//        int n = 1 + interfaces.length;
//        String[] c = new String[n];
//        System.arraycopy(interfaces, 0, c, 0, interfaces.length);
//        
//        // Add the EntityBean interface
//        c[c.length - 1] = ENTITYBEAN;
//
//        super.visit(version, access, className, signature, superClassName, c);
//    }
//
//    /**
//     * The ebeanIntercept field is added once but thats all. Note the other
//     * fields are defined in the superclass.
//     */
//    public FieldVisitor visitField(int access, String name, String desc, String signature,
//            Object value) {
//
//        if (!isInterceptFieldAdded) {
//        	
//            FieldVisitor f0 = cv.visitField(ACC_PRIVATE + ACC_VOLATILE, IDENTITY_FIELD_NAME, "Ljava/lang/Object;", null, null);
//            f0.visitEnd();
//            
//            FieldVisitor f1 = cv.visitField(0, INTERCEPT_FIELD_NAME, L_INTERCEPT, null, null);
//            f1.visitEnd();
//            
//            isInterceptFieldAdded = true;
//            return null;
//        }
//
//        return null;
//    }
//
//    /**
//     * Replace the method code with calls to super. Add the intercept code as
//     * required.
//     */
//    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
//            String[] exceptions) {
//        
//        boolean isPrivate = ((access & Opcodes.ACC_PRIVATE) != 0);
//        boolean isStatic = ((access & Opcodes.ACC_STATIC) != 0);
//        if (isPrivate || isStatic) {
//            // no intercept on static or private methods
//            return null;
//        }
//        // the key to look up in methodInfo
//        String methodKey = name + ":" + desc;
//
//        if (hasSuperClass){
//        	if (logger.isLoggable(Level.FINER)){
//        		String msg = "existing methods "+info.getClassName()+" "+methodKey;
//        		logger.finer(msg);
//        	}
//        }
//        
//        VisitMethodParams params = new VisitMethodParams(cv, access, name, desc, signature, exceptions);
//
//        if (methodInfo.isSet(methodKey)) {
//            // for persistent properties excluding assoc Many's & id
//            // ie. Old values not created for id or assoc many.
//            return new ProxySetterMethod(params, info, methodInfo);
//        }
//
//        if (methodInfo.isGet(methodKey)) {
//            // for persistent properties excluding id properties.
//            // ie. reference loading not fired for id properties.
//            return new ProxyGetterMethod(params, info);
//        }
//
//        if ("<init>".equals(name)) {
//            return new ProxyConstructor(params, info);
//        }
//
//        if ("hashCode:()I".equals(methodKey)) {
//            return new ProxyMethod(params, info);
//        }
//
//        if ("clone:()Ljava/lang/Object;".equals(methodKey)) {
//            // SuperClass has a clone() method
//            isAddClonable = false;
//            return new MethodClone(params, info);
//        }
//        
//        if ("toString:()Ljava/lang/String;".equals(methodKey)) {
//            // No intercept on toString() as used by debuggers etc
//            return null;
//        }
//        if ("hashCode:()I".equals(methodKey)) {
//            return null;
//        }
//        if ("equals:(Ljava/lang/Object;)Z".equals(methodKey)) {
//        	superHasEquals = true;
//            return null;
//        }
//        
//        return null;
//    }
//
//    /**
//     * Add methods to get and set the entityBeanIntercept. Also add the
//     * writeReplace method to control serialisation.
//     */
//    public void visitEnd() {
//
//        if (isAddClonable){
//            // super has not overwritten the clone() method.
//            // we will add the clone() method in case a super of the super has clone()
//            String[] exceptions = new String[] { "java/lang/CloneNotSupportedException" };
//            VisitMethodParams params = new VisitMethodParams(cv, ACC_PUBLIC, "clone", "()Ljava/lang/Object;", null, exceptions);
//            MethodClone methodClone = new MethodClone(params, info);
//            methodClone.visitCode();            
//        }
//        
//        MethodInfo methodInfo = info.getMethodInfo();
//        if (methodInfo.isEmbedded()){
//        	// don't override equals etc when it is an embedded bean
//        	// Either EmbeddedId or a Embeddable
//        	
//        } else if (methodInfo.overrideEquals(superHasEquals)) {
//        	// we want to generate a equals() hashCode() and ebeanGetIndentity()
//        	// methods so that the generated subclass has built in equals() support.
//        	
//        	if (methodInfo.getIdGetter() == null) {
//        		if (methodInfo.isSqlSelectBased()){
//        			// This could be common for reporting type beans based on
//        			// sql-select that use group by type queries.
//        		} else {
//	        		String m = "Can not generate equals for ["+info.getClassName();
//	        		m += "]. Concatinated id?";
//	        		logger.warning(m);
//        		}
//        	} else {
//        		        	
//	        	if (generateEbeanGetIdentityMethod()){
//		        	// add equals()
//		        	MethodEquals.add(cv, info);
//		        	
//		        	// add hashCode()
//		        	MethodHashCode.add(cv, info);
//	        	}
//        	}
//        }
//        
//        // add additional getters from super class inheritance
//        List<MethodDesc> additionalGetters = methodInfo.getAdditionalGetters();
//        for (MethodDesc methodDesc : additionalGetters) {
//            VisitMethodParams params = new VisitMethodParams(cv, ACC_PUBLIC, methodDesc);
//            ProxyGetterMethod getter = new ProxyGetterMethod(params, info);
//            getter.visitCode();
//		}
//        
//        // add additional setters from super class inheritance
//        List<MethodDesc> additionalSetters = methodInfo.getAdditionalSetters();
//        for (MethodDesc methodDesc : additionalSetters) {
//            VisitMethodParams params = new VisitMethodParams(cv, ACC_PUBLIC, methodDesc);
//            ProxySetterMethod setter = new ProxySetterMethod(params, info, methodInfo);
//            setter.visitCode();
//		}
//
//        // add set get methods for ebeanIntecept
//        MethodGetSetIntercept.add(cv, info);
//
//        // add a writeReplace method to control serialisation
//        MethodWriteReplace.add(cv, info);
//
//        super.visitEnd();
//    }
//
//    private boolean generateEbeanGetIdentityMethod() {
//    	String idGetterDesc = methodInfo.getIdGetterDesc();
//		if (idGetterDesc.equals("()I")) {
//			// int version of ebeanGetIndentity() 
//			MethodEbeanGetIdentityInt.add(cv, info);
//			return true;
//			
//		} else if (idGetterDesc.equals("()J")) {
//			// long version of ebeanGetIndentity() 
//			MethodEbeanGetIdentityLong.add(cv, info);
//			return true;
//			
//		} else if (idGetterDesc.length() > 5) {
//        	// Object version of ebeanGetIndentity()
//        	MethodEbeanGetIdentity.add(cv, info);
//        	return true;
//        	
//		} else {
//			String m = "Can not generate equals for ["+info.getClassName();
//    		m += "] due to type of id property: "+idGetterDesc;
//    		logger.warning(m);
//    		return false;
//		}
//    }
    
}
