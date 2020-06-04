import AuxiliaryFunction;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class GeneratorStoreShift {
    public static void main(String[] args) throws Exception {

        String path_trans_class = args[0];  // The first parameter represents the path of the class to be transformed.
        String para_states = args[1];  // The second parameter represents the locations of states.
        String trans_method = args[2];  // The third  parameter represents the method name to be transformed

        String [] strings_states = para_states.split(",");
        int[] states = new int[strings_states.length];
        for(int i = 0; i < strings_states.length; i ++){
            states[i] = Integer.parseInt(strings_states[i]);
        }


        FileInputStream is = new FileInputStream(path_trans_class);
        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        StoreClassAdapter classAdapter = new StoreClassAdapter(cw, states, trans_method);
        cr.accept(classAdapter, 0);

        byte[] data = cw.toByteArray();
        File file = new File(path_trans_class);
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(data);
        fout.close();

    }
}

class StoreClassAdapter extends ClassVisitor {
    private String owner;
    private int[] states;
    private String trans_method;

    public StoreClassAdapter(ClassVisitor cv, int[] states, String trans_method) {
        super(Opcodes.ASM5, cv);
        this.states = states;
        this.trans_method = trans_method;
    }

    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        owner = name;
    }

    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {

        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        MethodVisitor wrappedMv = mv;
        if (mv != null) {

            if (name.equals(trans_method)) {
                wrappedMv = new StoreMethodAdapter(mv, owner, trans_method, states);
            }
        }
        return wrappedMv;
    }
}

class StoreMethodAdapter extends MethodVisitor implements Opcodes {
    private String trans_class = null; // name of the instrumented class, used to form the storage path
    private String trans_method = null; // name of the instrumented method, used to form the storage path
    private String className = null;
    private int cur_line;

    Map<Integer, String> map_varType = new HashMap<>();
    Map<Integer,Integer> map_loadType = new HashMap<>();

    int states[];
    int index = 0;
    public StoreMethodAdapter(MethodVisitor mv, String owner, String trans_method, int[] states) {
        super(ASM5, mv);
        this.trans_method = trans_method;
        String tokens[] = owner.split("/");
        this.trans_class = tokens[tokens.length - 1];
        File folder = new File(this.trans_class);
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        this.states = states;
    }

    public void visitTypeInsn(int opcode, String desc){
        mv.visitTypeInsn(opcode,desc);
        if(opcode == NEW ){
            className = desc;
        }
    }




    public void visitVarInsn(int opcode, int var){
        mv.visitVarInsn(opcode, var);
        if (opcode >= ISTORE && opcode <= ASTORE) {  // I,L,F,D,A
            if(cur_line < 339 && var <= 4||cur_line >= 339 && cur_line < 356 && var <= 5||cur_line >= 356 && var <= 6) {
                if(opcode == ASTORE) {
                    if (className != null) {
                        map_varType.put(var, className);
                        className = null;
                    }
                }
                map_loadType.put(var,opcode - ISTORE + ILOAD);
            }
        }
    }

    public void visitLineNumber(int line, Label start) {
        cur_line = line;
        mv.visitLineNumber(line, start);
        if (line == states[index]) {
            for (Map.Entry<Integer, Integer> entry : map_loadType.entrySet()) {
                int loc = entry.getKey();
                int opcode = entry.getValue();
                mv.visitVarInsn(opcode,loc);  // load
                if(opcode == ILOAD){
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                }
                else if(opcode == LLOAD){
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(L)Ljava/lang/Long;", false);
                }
                else if(opcode == FLOAD) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                }
                else if(opcode == DLOAD) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                }

                mv.visitLdcInsn(trans_class + "/" + trans_method + "_state_" + index);

                mv.visitMethodInsn(INVOKESTATIC, "AuxiliaryFunction", "store", "(Ljava/lang/Object;Ljava/lang/String;)V", false);

            }
            AuxiliaryFunction.store(map_loadType, trans_class + "/load_var_Type" + index);
            AuxiliaryFunction.store(map_varType,  trans_class + "/load_var_Type" + index);
            index ++ ;
        }

    }

}