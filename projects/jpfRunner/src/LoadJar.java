import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadJar{
	public LoadJar(){  
	}
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException{
		//JarFile jarF = new JarFile("C:/aspectj1.6/lib/aspectjrt.jar");
		//JarFile jarF = new JarFile("F:/HKUST/CSIT510 OO software development/jexp-1.0/jexp.jar");
		JarFile jarF = new JarFile("../jexp.jar");
		Enumeration enums = jarF.entries();
		
	    //regular expression
		String regex = null;
		Pattern p = null;
		Matcher m = null;

		clearScript();
		
		while(enums.hasMoreElements()){
			JarEntry entry = (JarEntry)enums.nextElement();
			if(!entry.isDirectory()){
		    	String name = entry.getName();
			    if(name.endsWith(".class")){
			    	regex = "(\\$.*)?\\.class$";
			    	p = Pattern.compile(regex);   
					m = p.matcher(name);
					String replacedClassName = m.replaceAll("");
			    	regex = "/";
			    	p = Pattern.compile(regex);   
			    	m = p.matcher(replacedClassName);
					replacedClassName = m.replaceAll(".");
					//System.out.println(replacedClassName);
					loadClass(replacedClassName);
			    }
		    }
		}
		
/*
		String[] className = {"com.bc.jexp.impl.NamespaceImpl"};
		for (int i = 0; i < className.length;i++){
			loadClass(className[i]);
		}*/
	}
	
	private static void loadClass(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException{
		try{
			String DriverName = "Driver_" + className;
			Class c = Class.forName(className);
			Method methlist[] = c.getDeclaredMethods();
			String methodName = "";
			String methodParameters = "";
			
    		boolean para_flag, generateFlag;
            for (int i = 0; i < methlist.length;i++){
	        	Method m = methlist[i];
	        	methodName = m.getName();
	        	//System.out.println(methodName);
	        	String tempDriverName = DriverName + "_" + methodName;
			
	        	String tempjavaFileContent = methodName;
	        	String paraValue = "";
            	para_flag = false;
	        	generateFlag = true;
	            Class param[] = m.getParameterTypes();
            	if(param.length == 0){
            		tempjavaFileContent += "();";
            	}
	            for (int j = 0; j < param.length; j++){
	            	String[] paraSplit = param[j].toString().split(" ");
	            	tempDriverName += "_" +paraSplit[paraSplit.length-1];
	            	
	            	if(param[j] ==  int.class || param[j] ==  short.class || param[j] ==  long.class || param[j] ==  Integer.class || param[j] == Short.class ||param[j] == Long.class){
	            		paraValue = "1";
	            	}
	            	else if(param[j] ==  char.class || param[j] ==  byte.class || param[j] == Character.class || param[j] == String.class){
	            		paraValue = "\"a\"";
	            	}
	            	else if(param[j] ==  boolean.class || param[j] == Boolean.class ){
	            		paraValue = "true";
	            	}
	            	else if(param[j] ==  float.class || param[j] ==  double.class || param[j] == Float.class || param[j] == Double.class){
	            		paraValue = "1.0";
	            	}
	            	else{
	            		String regEx="^interface|\\[";//to see if it is an interface or array of objects
	            		Pattern p=Pattern.compile(regEx); 
	            		Matcher matcher=p.matcher(param[j].toString()); 
	            		if(matcher.find()){
	            			generateFlag = false;
	            			break;
	            		}
	            		Class para_c = Class.forName(paraSplit[paraSplit.length-1]);
	            		
	            		Constructor[] para_c_c = para_c.getConstructors();
	            		if(para_c_c.length ==0){
	            			generateFlag = false;
	            			break;
	            		}
	            		for(int k = 0; k < para_c_c.length; k++){
	            			Class[] pccp = para_c_c[k].getParameterTypes();
	            			if(pccp.length ==0){
	            				para_flag = true;//indicates that we have a constructor with no arguments
	            				paraValue = "new " + paraSplit[paraSplit.length-1] + "()";
	            				//System.out.println(paraValue);
	            				break;
	            			}
	            		}
	            		if(!para_flag){
	            			generateFlag = false;
	            			break;
	            		}
	            		//System.out.println(param[j]);
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
	            	continue;
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
	            compile(replacedDriverName);
            }
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	private static void generateJavaFile(String javaFile, String fileContent) throws IOException{
		FileWriter fw = new FileWriter(javaFile + ".java");
		fw.write(fileContent);
		fw.close();
		//System.out.println("File generated: " + javaFile + ".java");
	}
	
	private static void generatePropertiesFile(String className, String method, String methodParameters) throws IOException{
		FileWriter fw = new FileWriter(className + ".properties" );
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
		fw.write(propertiesText);
		fw.close();
		//System.out.println("File generated: " + className + ".properties");
	}
	
	
	private static void compile(String file) throws IOException{
		String cmd = "javac -classpath ../jexp.jar " + file + ".java";
		//String cmd = "javac -classpath D:/KZOOM/workspace/jpfRunner/src/jexp.jar " + file + ".java";
		//compile
		Process process = Runtime.getRuntime().exec(cmd);
		try	{ //wait the compiler to end
			process.waitFor();
		}
		catch (InterruptedException e){
		}
		int val = process.exitValue();
		if (val != 0){
			//System.out.println(val);
			//throw new RuntimeException("compile error:" + "error code" + val);
		}
		else{
			//no error occurs, write into script
			System.out.println(val + "  " + cmd);
            runJPF(file);
		}
	}

	private static void runJPF(String file) throws IOException{
		String cmd = "/home/ryanzhu/trunk/bin/jpf -c " + file + ".properties " + file;
		//compile
		Process process = Runtime.getRuntime().exec(cmd);
		try	{ //wait the compiler to end
			process.waitFor();
		}
		catch (InterruptedException e){
		}
		int val = process.exitValue();
		if (val != 0){
			//System.out.println(val);
			//throw new RuntimeException("compile error:" + "error code" + val);
		}
		else{
			//no error occurs, write into script
			System.out.println(val + "  " + cmd);
			writeIntoScript(cmd + "\n"+process.getOutputStream() + "\n");
		}
	}
	//clear the script file
	private static void clearScript() throws IOException {
		FileWriter fw = new FileWriter("jpfRunner.sh");
		fw.write("");
		fw.close();
	}

	//append script content to the jpfRunner.sh
	private static void writeIntoScript(String scriptContent) throws IOException{
		FileWriter fw = new FileWriter("jpfRunner.sh", true);
		fw.write(scriptContent);
		fw.close();
	}

} 