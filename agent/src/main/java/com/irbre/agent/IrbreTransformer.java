package com.irbre.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;


import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * Transformer that instruments classes to capture method entry and exceptions.
 */
public class IrbreTransformer implements ClassFileTransformer {

    private static void logInfo(String msg) {
        System.out.println("[IRBRE] " + msg);
    }

    private static void logError(String msg, Throwable t) {
        System.err.println("[IRBRE ERROR] " + msg);
        t.printStackTrace();
    }
    private final AgentConfiguration config;

    public IrbreTransformer(AgentConfiguration config) {
        this.config = config;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            if (className == null) return null;

            String dottedClassName = className.replace('/', '.');
            if (dottedClassName.startsWith("org.springframework.") ||
                    dottedClassName.startsWith("org.apache.") ||
                    dottedClassName.startsWith("com.fasterxml.jackson.") ||
                    dottedClassName.startsWith("org.slf4j.") ||
                    dottedClassName.startsWith("ch.qos.logback.") ||
                    dottedClassName.startsWith("java.") ||
                    dottedClassName.startsWith("javax.") ||
                    dottedClassName.startsWith("sun.") ||
                    dottedClassName.startsWith("com.sun.") ||
                    dottedClassName.startsWith("jdk.") ||
                    dottedClassName.startsWith("com.irbre.") ||
                    // Add any other framework you see in logs
                    dottedClassName.contains("$$EnhancerBySpringCGLIB$$") ||
                    dottedClassName.contains("$$FastClassBySpringCGLIB$$")) {
                return null;
            }

            // ✅ Only allow your app packages
            if (!dottedClassName.startsWith("com.example.")) {
                return null;
            }

            if (!config.shouldInstrument(dottedClassName)) return null;

            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

            ClassVisitor visitor = new IrbreClassVisitor(writer, dottedClassName);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            return writer.toByteArray();
        } catch (Throwable t) {
//            logger.error("Error transforming class: " + className, t);
//            return null;
            System.err.println("[IRBRE] Transform error in: " + className);
            t.printStackTrace();
            return null;
        }
    }

    private static class IrbreClassVisitor extends ClassVisitor {
        private final String className;

        public IrbreClassVisitor(ClassVisitor cv, String className) {
            super(Opcodes.ASM9, cv);
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            if (mv == null || name.equals("<init>") || name.equals("<clinit>") ||
                    (access & Opcodes.ACC_SYNTHETIC) != 0) {
                return mv;
            }

            return new IrbreMethodVisitor(mv, access, name, descriptor, className);
        }
    }

    private static class IrbreMethodVisitor extends AdviceAdapter {
        private final String methodName;
        private final String className;

        protected IrbreMethodVisitor(MethodVisitor mv, int access, String name,
                                     String descriptor, String className) {
            super(Opcodes.ASM9, mv, access, name, descriptor);
            this.methodName = name;
            this.className = className;
        }

        @Override
        protected void onMethodEnter() {
            // Call EventCollector.getInstance().onMethodEntry(className, methodName);
            mv.visitMethodInsn(INVOKESTATIC,
                    "com/irbre/agent/EventCollector",
                    "getInstance",
                    "()Lcom/irbre/agent/EventCollector;",
                    false);
            mv.visitLdcInsn(className);
            mv.visitLdcInsn(methodName);
            mv.visitMethodInsn(INVOKEVIRTUAL,
                    "com/irbre/agent/EventCollector",
                    "onMethodEntry",
                    "(Ljava/lang/String;Ljava/lang/String;)V",
                    false);
        }

        @Override
        protected void onMethodExit(int opcode) {
            // Handle exceptions automatically if opcode == ATHROW
        }
    }
}
