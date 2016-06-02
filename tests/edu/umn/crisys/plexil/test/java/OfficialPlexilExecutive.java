package edu.umn.crisys.plexil.test.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OfficialPlexilExecutive {

	public static final File DEBUG_CFG = new File(RegressionTest.RESOURCES, "Debug.cfg");
	
	public static BufferedReader generateLogFor(File plx, Optional<File> psx, Optional<File> libDir) throws IOException {
		ProcessBuilder pb;
		if (System.getenv("PLEXIL_HOME") == null) {
			System.out.println("$PLEXIL_HOME is missing. The JVM must not be"
					+ " running in the proper environment.");
			System.out.println("I'll try running `plexiltest` inside"
					+" " +System.getenv("SHELL") +" for you instead.");
			String command = getPlexilTestCommand(plx, psx, libDir)
					.stream()
					.map(arg -> arg.contains(" ") ? "\""+arg+"\"" : arg)
					.collect(Collectors.joining(" "));
			pb = new ProcessBuilder(System.getenv("SHELL"), "-c", command);
		} else {
			// The environment looks okay as-is.
			 pb = new ProcessBuilder(getPlexilTestCommand(plx, psx, libDir));
		}
		
		
		Process process = pb.start();
		InputStream pipe = process.getInputStream();
		return new BufferedReader(new InputStreamReader(pipe));
	}
	
	public static String generateLogStringFor(File plx, Optional<File> psx, Optional<File> libDir) throws IOException {
		BufferedReader reader = generateLogFor(plx, psx, libDir);
		String theLog = "";
		String line = null;
		while ((line = reader.readLine()) != null) {
			theLog += line;
		}
		return theLog;
	}
	
	public static List<String> getPlexilTestCommand(File plx, Optional<File> psx, Optional<File> libDir) {
		List<String> args = new ArrayList<>();
		args.add("plexiltest");
		args.add("-q");
		args.add("-d");
		args.add(DEBUG_CFG.getPath());
		args.add("-p");
		args.add(plx.getPath());
		if (psx.isPresent()) {
			args.add("-s");
			args.add(psx.get().getPath());
		}
		if (libDir.isPresent()) {
			args.add("-L");
			args.add(libDir.get().getPath());
		}
		return args;
	}
	
}
