import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.semmle.autobuild.AutoBuild;

public class Main {

	static String language;
	
	public enum OSType {
		Windows, MacOS, Linux, Other
	};

	public static void main(String args[]) throws Exception {
		List<String> newArgsList = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--language")) {
				i = i + 1;
				language = args[i];
			} else
				newArgsList.add(args[i]);
		}
		newArgsList.add("--no-indexing");
		String[] newArgs = newArgsList.toArray(new String[newArgsList.size()]);
		if(language != null && !language.isEmpty()) {
			if ("java".equals(language))
				buildJavaSource(newArgs);
			else if ("csharp".equals(language))
				buildCSSource(newArgs);
			else {
				System.out.println("ERROR: Invalid value for argument \"--language\"");
				System.exit(1);
			}
		} else {
			System.out.println("ERROR: Argument \"--language\" is missing!");
			System.exit(1);
		}
	}

	private static void buildJavaSource(String[] args) {
		try {
			AutoBuild b = new AutoBuild();
			try {
				System.exit(b.run(args));
				b.close();
			} catch (Throwable throwable) {
				try {
					b.close();
				} catch (Throwable throwable1) {
					throwable.addSuppressed(throwable1);
				}
				throw throwable;
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void buildCSSource(String[] args) {
		try {
			String sourcePath = "";
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--source-checkout-dir")) {
					sourcePath = args[i + 1];
					break;
				}
			}
			if(!sourcePath.isEmpty()) {
				System.out.println("CSHARP_SOURCE_PROJECT_PATH = " + sourcePath);
				String csharpRoot = System.getenv("CODEQL_EXTRACTOR_CSHARP_ROOT");
				if(csharpRoot != null && !csharpRoot.isEmpty()) {
					StringBuilder cmd = new StringBuilder("cmd /c ");
					cmd.append(csharpRoot).append(System.getProperty("file.separator"));
					cmd.append("tools").append(System.getProperty("file.separator"));
					if(getOperatingSystemType() == OSType.Windows)
						cmd.append("autobuild.cmd");
					else if(getOperatingSystemType() == OSType.Linux)
						cmd.append("autobuild.sh");
					else 
						throw new Exception("Operating System not supported!");
					System.out.println("Command --> " + cmd.toString());
					Process process = Runtime.getRuntime().exec(cmd.toString(), null, new File(sourcePath));
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
					int exitVal = process.waitFor();
					if (exitVal == 0) {
						reader.close();
						System.exit(0);
					}
				} else {
					System.out.println("ERROR: ENV \"CODEQL_EXTRACTOR_CSHARP_ROOT\" is missing!");
					System.exit(1);
				}
			} else {
				System.out.println("ERROR: Argument \"--source-checkout-dir\" is missing!");
				System.exit(1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static OSType getOperatingSystemType() {
		OSType detectedOS;
		String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
			detectedOS = OSType.MacOS;
		} else if (OS.indexOf("win") >= 0) {
			detectedOS = OSType.Windows;
		} else if (OS.indexOf("nux") >= 0) {
			detectedOS = OSType.Linux;
		} else {
			detectedOS = OSType.Other;
		}
		return detectedOS;
	}
}
