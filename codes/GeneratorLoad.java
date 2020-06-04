import AuxiliaryFunction;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.objectweb.asm.*;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleToIntFunction;

public class GeneratorLoadShift{
    public static void main(String[] args) throws Exception {
        
        String path_trans_class = args[0];  // The first parameter represents the path of the class to be transformed.
        String para_states = args[1];  // The second parameter represents the locations of states.
        String trans_method = args[2];  // The third  parameter represents the method name to be transformed
        String para_type = args[3]; // The fourth parameter represents the times of instrumentation（load_false,load_true)

        String [] strings_states= para_states.split(",");

        int[] states = new int[strings_states.length];
        for(int i = 0; i < strings_states.length; i ++){
            states[i] = Integer.parseInt(strings_states[i]);
        }
        
        FileInputStream is = new FileInputStream(class_file);
        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        boolean flag;

        if(para_type.equals("load_false")){
            flag = false; 
        }
       else if(para_type.equals("load_true")){
            flag = true;  
        }

        LoadClassAdapter classAdapter = new LoadClassAdapter(cw,flag,states,trans_method);
        cr.accept(classAdapter, 0);
        byte[] data = cw.toByteArray();
        File file = new File(class_file);
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(data);
        fout.close();
    }

}

class LoadClassAdapter extends ClassVisitor {
    private String owner;
    private boolean flag;
    int[] states;
    private String trans_method;
    public LoadClassAdapter(ClassVisitor cv, boolean flag, int[] states,String trans_method) {
        super(Opcodes.ASM5, cv);
        this.flag = flag;
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

        MethodVisitor mv = cv.visitMethod(access, name, desc, signature,exceptions);
        MethodVisitor wrappedMv = mv;
        if (mv != null) {

            if (name.equals(trans_method)) {

                wrappedMv = new LoadMethodAdapter(mv, owner, trans_method, flag, states);
            }
        }
        return wrappedMv;
    }


class LoadMethodAdapter extends MethodVisitor implements Opcodes {
    private String owner;
    private String trans_class = null;
    private String trans_method = null;

    boolean flag;
    Set<Integer> used = new HashSet<>();
    int cur_line;
    int[] states;
    int[] changes = new int[]{335,352,359,1000};  //The location of changes, adjust to your own reality

    int next_change = 0;
    int next_state = 0;
    int load_line;
    int s;  // normal mode：0  skip mode：-1  compare mode：1
    Label label_a;
    Label label_b;
    Label label_c;
    Label label_d;


    Map<Integer, String> map_varType = new HashMap<>();
    Map<Integer,Integer> map_loadType = new HashMap<>();

    public LoadMethodAdapter(MethodVisitor mv, String owner, String name, boolean flag, int[] states) {
        super(Opcodes.ASM5, mv);
        this.owner = owner;
        this.trans_method = trans_method;
        String tokens[] = owner.split("/");
        this.trans_class = tokens[tokens.length - 1];

        this.flag = flag;
        this.states = states;
    }
    public void visitCode() {
        mv.visitCode();



        if(states[next_state] > changes[next_change]){
            s = 0;
            return;
        }
        while (states[next_state] < changes[next_change]) {
            next_state++;
        }
        s = -1;
        load_line = states[next_state - 1];

        if(flag == false) {
            return;
        }
        label_a = new Label();
        mv.visitJumpInsn(GOTO, label_a);
    }

    public void visitLineNumber(int line, Label start) {
        mv.visitLineNumber(line, start);
        cur_line = line;
        if(s == -1){
            if(line == load_line){
                s = 0;

                String loadType, varType;
                try {
                    File file = new File(trans_class + "/load_var_Type" + (next_state - 1));
                    FileInputStream is = null;
                    is = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(isr);
                    loadType = in.readLine();

                    ObjectMapper objectMapper = new ObjectMapper();
                    map_loadType = objectMapper.readValue(loadType,new TypeReference<Map<Integer,Integer>>() { });
                    varType = in.readLine();
                    map_varType = objectMapper.readValue(varType,new TypeReference<Map<Integer,String>>() { });
                    in.close();
                    is.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(flag == false){
                    if(next_change > 0){  t();
                        nused.addAll(map_loadType.keySet());
                        nused.removeAll(used);
                        AuxiliaryFunction.store(nused,trans_class + "/nused_" + (next_state - 1));
                        used.clear();
                    }
                    return;
                }
                if(next_change > 0){
                    label_b = new Label();
                    mv.visitJumpInsn(GOTO, label_b);
                }
                mv.visitLabel(label_a);


                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitLdcInsn("Yes!");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

                mv.visitTypeInsn(NEW, "java/io/File");
                mv.visitInsn(DUP);
                mv.visitLdcInsn(trans_class + "/" + trans_method + "_state_" + (next_state - 1));
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitVarInsn(ASTORE, 14);
                mv.visitTypeInsn(NEW, "java/io/FileInputStream");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 14);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/FileInputStream", "<init>", "(Ljava/io/File;)V", false);
                mv.visitVarInsn(ASTORE, 15);
                mv.visitTypeInsn(NEW, "java/io/InputStreamReader");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 15);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/InputStreamReader", "<init>", "(Ljava/io/InputStream;)V", false);
                mv.visitVarInsn(ASTORE, 16);
                mv.visitTypeInsn(NEW, "java/io/BufferedReader");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 16);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/BufferedReader", "<init>", "(Ljava/io/Reader;)V", false);
                mv.visitVarInsn(ASTORE, 17);

                for (Map.Entry<Integer, Integer> entry :  map_loadType.entrySet()) {
                    int loc = entry.getKey();
                    int opcode = entry.getValue();

                    mv.visitVarInsn(ALOAD, 17);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "readLine", "()Ljava/lang/String;", false);
                    mv.visitVarInsn(ASTORE, 30);
                    if(used.contains(loc) == false){
                        continue;
                    }

                    if(opcode == ALOAD){
                        mv.visitTypeInsn(NEW, "org/codehaus/jackson/map/ObjectMapper");
                        mv.visitInsn(DUP);
                        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/jackson/map/ObjectMapper", "<init>", "()V", false);
                        mv.visitVarInsn(ASTORE, 20);

                        mv.visitVarInsn(ALOAD, 20);
                        mv.visitFieldInsn(GETSTATIC, "org/codehaus/jackson/map/DeserializationConfig$Feature", "FAIL_ON_UNKNOWN_PROPERTIES", "Lorg/codehaus/jackson/map/DeserializationConfig$Feature;");
                        mv.visitInsn(ICONST_0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/jackson/map/ObjectMapper", "configure", "(Lorg/codehaus/jackson/map/DeserializationConfig$Feature;Z)Lorg/codehaus/jackson/map/ObjectMapper;", false);
                        mv.visitInsn(POP);


                        mv.visitVarInsn(ALOAD, 20);
                        mv.visitVarInsn(ALOAD, 30);
                        if(map_varType.get(loc).equals("[Ljava/lang/String;")){
                            mv.visitLdcInsn(Type.getType("[Ljava/lang/String;"));
                            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/jackson/map/ObjectMapper", "readValue", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false);
                            mv.visitTypeInsn(CHECKCAST,"[Ljava/lang/String;");
                        }
                        else if(map_varType.get(loc).charAt(0) == '['){
                            String descriptor = map_varType.get(loc);
                            mv.visitLdcInsn(Type.getType(descriptor));
                            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/jackson/map/ObjectMapper", "readValue", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false);
                            mv.visitTypeInsn(CHECKCAST,descriptor);

                        }
                        else{
                            String descriptor = 'L' + map_varType.get(loc) + ';';
                            mv.visitLdcInsn(Type.getType(descriptor));
                            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/jackson/map/ObjectMapper", "readValue", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false);
                            mv.visitTypeInsn(CHECKCAST,map_varType.get(loc));
                        }
                    }
                    else if(opcode == ILOAD){

                        mv.visitVarInsn(ALOAD, 30);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
                    }
                    else if(opcode == LLOAD){
                        mv.visitVarInsn(ALOAD, 30);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "parseLong", "(Ljava/lang/String;)J", false);
                    }
                    else if(opcode == FLOAD){
                        mv.visitVarInsn(ALOAD, 30);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
                    }
                    else if(opcode == DLOAD){
                        mv.visitVarInsn(ALOAD, 30);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "parseDouble", "(Ljava/lang/String;)D", false);
                    }
                    mv.visitVarInsn(opcode - ILOAD + ISTORE, loc);
                }
                used.clear();
                mv.visitVarInsn(ALOAD, 17);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "close", "()V", false);
                mv.visitVarInsn(ALOAD, 15);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileInputStream", "close", "()V", false);

                if(next_change > 0) {
                    mv.visitLabel(label_b);
                }


            }

        }
        else if(s == 0) {
            if(line == changes[next_change]){
                next_change ++;
                s = 1;
            }
        }
        else if(s == 1){
            if(line == changes[next_change]){
                next_change ++;
                s = 1;
            }
            else if(line == states[next_state]){
                next_state ++;


                if(states[next_state] > changes[next_change]){
                    s = 0;
                    return;
                }
                int compare_state = next_state - 1;
                while(states[next_state] < changes[next_change]){
                    next_state ++;
                }
                load_line = states[next_state - 1];
                s = -1;
                if(flag == false){
                    return;
                }

                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitLdcInsn("Compare!");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);


                mv.visitTypeInsn(NEW, "java/io/File");
                mv.visitInsn(DUP);
                mv.visitLdcInsn(trans_class + "/" + trans_method + "_state_" + compare_state);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitVarInsn(ASTORE, 14);
                mv.visitTypeInsn(NEW, "java/io/FileInputStream");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 14);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/FileInputStream", "<init>", "(Ljava/io/File;)V", false);
                mv.visitVarInsn(ASTORE, 15);
                mv.visitTypeInsn(NEW, "java/io/InputStreamReader");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 15);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/InputStreamReader", "<init>", "(Ljava/io/InputStream;)V", false);
                mv.visitVarInsn(ASTORE, 16);
                mv.visitTypeInsn(NEW, "java/io/BufferedReader");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 16);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/BufferedReader", "<init>", "(Ljava/io/Reader;)V", false);
                mv.visitVarInsn(ASTORE, 17);

                mv.visitTypeInsn(NEW, "java/util/HashSet");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false);
                mv.visitVarInsn(ASTORE, 32);



                for (Map.Entry<Integer, Integer> entry :  map_loadType.entrySet()) {
                    int loc = entry.getKey();
                    int opcode = entry.getValue();

                    mv.visitVarInsn(opcode, loc);
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
                    mv.visitMethodInsn(INVOKESTATIC, "AuxiliaryFunction", "serialize", "(Ljava/lang/Object;)Ljava/lang/String;", false);
                    mv.visitVarInsn(ASTORE, 33);
                    mv.visitVarInsn(ALOAD, 17);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "readLine", "()Ljava/lang/String;", false);
                    mv.visitVarInsn(ALOAD, 33);
                    mv.visitMethodInsn(INVOKESTATIC, "AuxiliaryFunction", "judge", "(Ljava/lang/String;Ljava/lang/String;)I", false);

                    label_d = new Label();
                    mv.visitJumpInsn(IFNE, label_d);
                    mv.visitVarInsn(ALOAD, 32);
                    mv.visitLdcInsn(loc);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
                    mv.visitInsn(POP);
                    mv.visitLabel(label_d);

                }


                mv.visitVarInsn(ALOAD, 17);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "close", "()V", false);
                mv.visitVarInsn(ALOAD, 15);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileInputStream", "close", "()V", false);


                mv.visitLdcInsn(trans_class + "/nused_" + (next_state - 1));
                mv.visitMethodInsn(INVOKESTATIC, "AuxiliaryFunction", "readset", "(Ljava/lang/String;)Ljava/util/Set;", false);
                mv.visitVarInsn(ALOAD, 32);
                mv.visitMethodInsn(INVOKESTATIC, "AuxiliaryFunction", "isSubset", "(Ljava/util/Set;Ljava/util/Set;)I", false);

                label_a = new Label();
                mv.visitJumpInsn(IFNE, label_a);

            }
        }
    }



    public void visitVarInsn(int opcode, int var){
        mv.visitVarInsn(opcode, var);
        if(s == -1){
            if(cur_line < 339 && var <= 4||cur_line >= 339 && cur_line < 356 && var <= 5||cur_line >= 356 && var <= 6) {
                if (opcode >= ISTORE && opcode <= ASTORE || (opcode >= ILOAD && opcode <= ALOAD)) {

                    used.add(var);
                }
            }
        }
        else {
            if (opcode >= ISTORE && opcode <= ASTORE) {  // I,L,F,D,A
                if(cur_line < 339 && var <= 4||cur_line >= 339 && cur_line < 356 && var <= 5||cur_line >= 356 && var <= 6) {
                    map_loadType.put(var,opcode - ISTORE + ILOAD);
                }
            }

        }
    }
}








