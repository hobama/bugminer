import java.io.BufferedReader;
import java.io.FileReader;
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
	
	//private final static String jarFile = "./jexp.jar";
	//private final static String jarFile = "F:/HKUST/recrash/eclipse-JDT-SDK-3.4.2/eclipse/plugins/org.eclipse.jdt.core_3.4.4.v_894_R34x.jar";
	private final static String jarFile = "./aspectjrt.jar";
	
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException{
		//JarFile jarF = new JarFile("C:/aspectj1.6/lib/aspectjrt.jar");
		//JarFile jarF = new JarFile("F:/HKUST/CSIT510 OO software development/jexp-1.0/jexp.jar");
		JarFile jarF = new JarFile(jarFile);
		Enumeration enums = jarF.entries();
		
	    //regular expression
		String regex = null;
		Pattern p = null;
		Matcher m = null;

		initScript();
		
		while(enums.hasMoreElements()){
			JarEntry entry = (JarEntry)enums.nextElement();
			if(!entry.isDirectory()){
		    	String name = entry.getName();
			    if(name.endsWith(".class")){
			    	//System.out.println(name);
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
            		methodParameters = "()";
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
	            	else if(param[j] ==  Class.class || param[j] == Object.class){
	            		paraValue = "new Integer(1)";
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
	            		//object with constructors
            			paraValue = "new " + paraSplit[paraSplit.length-1] + "(";
	            		for(int k = 0; k < para_c_c.length; k++){
	            			Class[] pccp = para_c_c[k].getParameterTypes();
	            			if(pccp.length ==0){
	            				para_flag = true;//indicates that we have a constructor with no arguments
	            				paraValue += ")";
	            				//System.out.println(paraValue);
	            				break;
	            			}
	            			for(int l = 0; l < pccp.length; l++){
	        	            	String[] paraSplit_l = pccp[l].toString().split(" ");
	        	            	//tempDriverName += "_" +paraSplit_l[paraSplit_l.length-1];
	        	            	String tempS = "";
	        	            	
	        	            	if(pccp[l] ==  int.class || pccp[l] ==  short.class || pccp[l] ==  long.class || pccp[l] ==  Integer.class || pccp[l] == Short.class || pccp[l] == Long.class){
	        	            		tempS = "1";
	        	            	}
	        	            	else if(pccp[l] ==  char.class || pccp[l] ==  byte.class || pccp[l] == Character.class || pccp[l] == String.class){
	        	            		tempS = "\"a\"";
	        	            	}
	        	            	else if(pccp[l] ==  boolean.class || pccp[l] == Boolean.class ){
	        	            		tempS = "true";
	        	            	}
	        	            	else if(pccp[l] ==  float.class || pccp[l] ==  double.class || pccp[l] == Float.class || pccp[l] == Double.class){
	        	            		tempS = "1.0";
	        	            	}
	        	            	else{
	        	            		tempS = "null";
	        	            	}

	        	            	if(l == 0){
	        	            		paraValue += tempS;
	        	            		if(param.length == 1){
	        	            			paraValue += ")";
	        	            		}
	        	            	}
	        	            	else if(l == param.length - 1){
	        	            		paraValue += "," + tempS + ")";
	        	            	}
	        	            	else{
	        	            		paraValue += "," + tempS;
	        	            	}
	            			}
	            		}
	            		if(!para_flag){
	            			generateFlag = false;
	            			break;
	            		}
	            	}
	            	
	            	if(j == 0){
	            		methodParameters = "(sym";
	            		tempjavaFileContent += "(" + paraValue;
	            		if(param.length == 1){
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
	            tempjavaFileContent += "\n		} catch(Exception e){\n			e.printStackTrace();\n		}\n	}\n}";

				String regEx="\\."; 
				Pattern pat=Pattern.compile(regEx);   
				Matcher mat=pat.matcher(tempDriverName);
				String replacedDriverName=mat.replaceAll("_");
				
				String javaFileContent =
					"import java.lang.Exception;\n"
					+ "import " + className + ";\n"
				   	+ "\npublic class " + replacedDriverName + " {\n"
				    + "	public static void main(String[] args){\n"
				   	+ "		try{\n"
				    + "			" + className + " newDriver = new " + className + "();\n"
				    + "			newDriver.";   
	            generateJavaFile(replacedDriverName, javaFileContent + tempjavaFileContent);
	            generatePropertiesFile(replacedDriverName, methodName, methodParameters);
	            compile(replacedDriverName);
            }
            //run the jpfRunner.sh to run the symbolic execution
            //runJPF();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	private static void generateJavaFile(String javaFile, String fileContent) throws IOException{
		FileWriter fw = new FileWriter(javaFile + ".java");
		fw.write(fileContent);
		fw.flush();
		fw.close();
		//System.out.println("File generated: " + javaFile + ".java");
	}
	
	private static void generatePropertiesFile(String className, String method, String methodParameters) throws IOException{
		FileWriter fw = new FileWriter(className + ".properties" );
		String propertiesText = 
			"vm.insn_factory.class = gov.nasa.jpf.symbc.SymbolicInstructionFactory\n"
			+ "jpf.listener = gov.nasa.jpf.symbc.SymbolicListener\n"
			+ "vm.classpath = .:" + jarFile + "\n"
			+ "vm.sourcepath+= ,${user.home}/tmp\n"
			+ "vm.storage.class=\n"
			+ "symbolic.method=" + method + methodParameters + "\n"
			+ "search.multiple_errors=true\n"
			+ "+vm.peer.packages=gov.nasa.jpf.symbc,gov.nasa.jpf.jvm\n"
			+ "log.level=warning\n"
			+ "jpf.report.console.finished=\n"
			+ className;
		fw.write(propertiesText);
		fw.flush();
		fw.close();
		//System.out.println("File generated: " + className + ".properties");
	}
	
	
	private static void compile(String file) throws IOException{
		String cmd = "javac -classpath " + jarFile + ":. " + file + ".java";
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
			//System.out.println(val + "  " + cmd);
			writeIntoFile("singelRun.sh",false,"#!/bin/bash\n/home/ryanzhu/trunk/bin/jpf -c " + file + ".properties " + file + "\n");
			writeIntoFile("jpfRunner.sh",true,"/home/ryanzhu/trunk/bin/jpf -c " + file + ".properties " + file + "\n");
			runSingleJPF(file);
		}
	}

	private static void runSingleJPF(String file) throws IOException{
		FileWriter fw = new FileWriter("run.sh");
		String shTxt = 
			"#!/bin/bash\n"
			+ "sh singelRun.sh > symbolicExecutionResult.txt\n";
		fw.write(shTxt);
		fw.flush();
		fw.close();
		String cmd = "sh run.sh";
		//execute a single run
		Process process = Runtime.getRuntime().exec(cmd);
		try	{ //wait the compiler to end
			process.waitFor();
		}
		catch (InterruptedException e){
		}
		int val = process.exitValue();
		if (val != 0){
			//throw new RuntimeException("compile error:" + "error code" + val);
		}
		else{
			//no error occurs
			//read from the symbolicExecutionResult.txt to check if ok
			//if ok, store the method for the next round test;else, store the infos into unhandledResult.txt
			FileReader fr = new FileReader("symbolicExecutionResult.txt");
			BufferedReader br = new BufferedReader (fr);
            String s,ss;
            boolean ok_flag = false;
    		String regEx_NoPathCondition="No path conditions for\\s*";//to see if it has no path conditions
    		String regEx_Exception="\\w*\\.\\w*Exception";//to see if it has exceptions
    		Pattern p_NoPathCondition=Pattern.compile(regEx_NoPathCondition); 
    		Pattern p_Exception=Pattern.compile(regEx_Exception); 
    		Matcher matcher_NoPathCondition,matcher_Exception;
    		
            while ((s = br.readLine() )!=null){
        		matcher_NoPathCondition=p_NoPathCondition.matcher(s); 
        		matcher_Exception=p_NoPathCondition.matcher(s); 
        		if(matcher_NoPathCondition.find()){
        			ok_flag = true;
        			ss = matcher_NoPathCondition.replaceAll("");
        			writeIntoFile("handledResult.txt",true,ss+"\n");
        			break;
        		}
        		if(matcher_Exception.find()){
        			ok_flag = false;
        			writeIntoFile("unhandledResult.txt",true,s+"\n");
        			break;
        		}
            	//System.out.println (s);
            }
            
            fr.close();
		}
	}
	
	//clear the script file
	private static void initScript() throws IOException {
		FileWriter fw = new FileWriter("jpfRunner.sh");
		fw.write("#!/bin/bash\n");
		fw.flush();
		fw.close();
		FileWriter fw2 = new FileWriter("singelRun.sh");
		fw2.write("");
		fw2.flush();
		fw2.close();
		FileWriter fw3 = new FileWriter("unhandledResult.txt");
		fw3.write("");
		fw3.flush();
		fw3.close();
		FileWriter fw4 = new FileWriter("handledResult.txt");
		fw4.write("");
		fw4.flush();
		fw4.close();
	}

	//append script content to the jpfRunner.sh
	private static void writeIntoFile(String fileName, boolean append, String fileContent) throws IOException{
		FileWriter fw = new FileWriter(fileName, append);
		fw.write(fileContent);
		fw.flush();
		fw.close();
	}

	private static void runJPF() throws IOException{
		FileWriter fw = new FileWriter("run.sh");
		String shTxt = 
			"#!/bin/bash\n"
			+ "sh jpfRunner.sh > symbolicExecutionResult.txt\n";
		fw.write(shTxt);
		fw.flush();
		fw.close();
		String cmd = "sh run.sh";
		//compile
		Process process = Runtime.getRuntime().exec(cmd);
		try	{ //wait the execution to end
			process.waitFor();
		}
		catch (InterruptedException e){
		}
		int val = process.exitValue();
		if (val != 0){
			process.destroy();
		}
		else{
			//no error occurs, write into script
		}
	}
} 