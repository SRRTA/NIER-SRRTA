import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.*;
import java.util.*;
// AuxiliaryFunction class includes some auxliary functions
public class AuxiliaryFunction {
    public  static void store(Object value, String filename){
        // serialize the variables to json，and then store to file
        String employeeJson = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            StringWriter stringWriter = new StringWriter();
            JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(stringWriter);
            objectMapper.writeValue(jsonGenerator, value);
            jsonGenerator.close();
            employeeJson = stringWriter.toString();
            write(employeeJson,filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  static String serialize(Object value){
        // serialize the variables to json and return it
        String employeeJson = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            StringWriter stringWriter = new StringWriter();
            JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(stringWriter);
            objectMapper.writeValue(jsonGenerator, value);
            jsonGenerator.close();
            employeeJson = stringWriter.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return employeeJson;
    }

    public static int judge(String s1,String s2){
        if(s1.equals(s2)){
            return 1;
        }
        else{
            return 0;
        }
    }

    public static  Set<Integer> readset(String filename){
        // deserialize string to a set
        Set<Integer> set = new HashSet<>();
        String s = read1line(filename);
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            set = objectMapper.readValue(s,new TypeReference<Set<Integer>>() { });
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return set;
    }

    public static int isSubsetthis(Set<String> big, Set<String> small){
        //judge whether the "small" set is a subset of the "big" set
        if(big.containsAll(small)){
            return 1;
        }
        else{
            return 0;
        }
    }

}
