import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.runtime.CFlow;
import org.aspectj.runtime.internal.CFlowStack;

import com.bc.jexp.EvalException;
import com.bc.jexp.ParseException;
import com.bc.jexp.Symbol;
import com.bc.jexp.Term;
import com.bc.jexp.impl.AbstractFunction;
import com.bc.jexp.impl.DefaultNamespace;
import com.bc.jexp.impl.NamespaceImpl;
import com.bc.jexp.impl.ParserImpl;
import com.bc.jexp.impl.Tokenizer;
import com.bc.jexp.impl.SymbolFactory;
import com.bc.jexp.Variable;
import com.bc.jexp.impl.UserFunction;
import com.bc.jexp.EvalEnv;

public class LoadJar{
	public LoadJar(){  
	}
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException{
		//JarFile jarF = new JarFile("jexp.jar");
		//for(){}
		String[] className = {"com.bc.jexp.impl.NamespaceImpl"};
		for (int i = 0; i < className.length;i++){
			loadClass(className[i]);
		}
	}
	
	public static void loadClass(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException{
		try{
			String DriverName = "Driver_" + className;
			Class c = Class.forName(className);
			Method methlist[] = c.getDeclaredMethods();
			String methodName = "";
			String methodParameters = "";
			
            for (int i = 0; i < methlist.length;i++){
	        	Method m = methlist[i];
	        	methodName = m.getName();
	        	System.out.println(methodName);
	        	String tempDriverName = DriverName + "_" + methodName;
			
	        	String tempjavaFileContent = methodName;
	        	String paraValue = "";
	        	boolean generateFlag = true;
	            Class param[] = m.getParameterTypes();
            	if(param.length == 0){
            		break;
            	}
	            for (int j = 0; j < param.length; j++){
	            	String[] paraSplit = param[j].toString().split(" ");
	            	tempDriverName += "_" +paraSplit[1];
	            	System.out.println(paraSplit[1]);
	            	
	            	/*Class p = Class.forName(paraSplit[1]);
	            	//System.out.println(p.toString());
					//p.newInstance();
					if(p  ==  Integer.class || p == Short.class || p == Long.class){
						paraValue = "1";
					}
					else if(p == Character.class || p == String.class){
						paraValue = "'1'";
					}
					else if(p == Boolean.class ){
						paraValue = "true";
					}
					else if(p == Float.class || p == Double.class){
						paraValue = "1.0";
					}
					else{
						//Constructor[] cc = p.getConstructors();
						
						Constructor cc = p.getConstructor();
						TypeVariable[] t = cc.getTypeParameters();
						if(t.length == 0){
							paraValue = cc.newInstance(t).toString();
						}
						else{
							break;
						}
						paraValue = "1";
					}*/
					
	            	if(param[j] ==  Integer.class || param[j] == Short.class ||param[j] == Long.class){
	            		paraValue = "1";
	            	}
	            	else if(param[j] == Character.class || param[j] == String.class){
	            		paraValue = "\"1+1\"";
	            	}
	            	else if(param[j] == Boolean.class ){
	            		paraValue = "true";
	            	}
	            	else if(param[j] == Float.class || param[j] == Double.class){
	            		paraValue = "1.0";
	            	}
	            	else{
	            		//paraValue = param[j].newInstance().toString();
	            	}
	            	
	            	if(j == 0){
	            		methodParameters = "(sym";
	            		tempjavaFileContent += "(" + paraValue;
	            		if(j == param.length - 1){
	            			methodParameters += ")";
	            			tempjavaFileContent += ");";
	            		}
	            	}
	            	else if(j == param.length - 1){
	            		methodParameters += "#sym)";
	            		tempjavaFileContent += "," + paraValue + ");";
	            	}
	            	else{
	            		methodParameters += "#sym";
	            		tempjavaFileContent += "," + paraValue;
	            	}
	            }
	            if(!generateFlag)
	            	break;
	            tempjavaFileContent += "\n	}\n}";

				String regEx="\\."; 
				Pattern pat=Pattern.compile(regEx);   
				Matcher mat=pat.matcher(tempDriverName);   
				String replacedDriverName=mat.replaceAll("_");
				
				String javaFileContent =
					"import java.io.IOException;\n"
					+ "import " + className + ";\n"
				   	+ "\npublic class " + replacedDriverName + " {\n"
				    + "	public static void main(String[] args) throws Exception{\n"
				    + "		" + className + " newDriver = new " + className + "();\n"
				    + "		newDriver.";   
	            generateJavaFile(replacedDriverName, javaFileContent + tempjavaFileContent);
	            generatePropertiesFile(replacedDriverName, methodName, methodParameters);
            }
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	public static void generateJavaFile(String javaFile, String fileContent) throws IOException{
		FileOutputStream out = new FileOutputStream(javaFile + ".java");
		out.write(fileContent.getBytes());
		out.close();
		System.out.println("File generated: " + javaFile + ".java");
	}
	
	public static void generatePropertiesFile(String className, String method, String methodParameters) throws IOException{
		FileOutputStream out = new FileOutputStream(className + ".properties" );
		String propertiesText = 
			"vm.insn_factory.class = gov.nasa.jpf.symbc.SymbolicInstructionFactory\n"
			+ "jpf.listener = gov.nasa.jpf.symbc.SymbolicListener\n"
			+ "vm.classpath = .:./jexp.jar\n"
			+ "vm.sourcepath+= ,${user.home}/tmp\n"
			+ "vm.storage.class=\n"
			+ "symbolic.method=" + method + methodParameters + "\n"
			+ "search.multiple_errors=true\n"
			+ "+vm.peer.packages=gov.nasa.jpf.symbc,gov.nasa.jpf.jvm\n"
			+ "log.level=warning\n"
			+ "jpf.report.console.finished=\n"
			+ className;
		out.write(propertiesText.getBytes());
		out.close();
		System.out.println("File generated: " + className + ".properties");
	}
	
	/*
	public static void invokeCompiler(File javafile) throws IOException{
		String[] cmd = { _compiler, "-classpath", _classpath, javafile.getName()};
		//compile
		Process process = Runtime.getRuntime().exec(cmd);
		try	{ //wait the compiler to end
		   process.waitFor();
		}
		catch (InterruptedException e){
		}
		int val = process.exitValue();
		if (val != 0){
		   throw new RuntimeException("compile error:" + "error code" + val);
		}
	}
	*/

} 