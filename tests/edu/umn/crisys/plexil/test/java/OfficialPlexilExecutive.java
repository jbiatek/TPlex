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
	
	private File plx;
	private Optional<File> psx = Optional.empty();
	private Optional<File> libDir = Optional.empty();
	private List<File> libs = new ArrayList<>();
	
	public OfficialPlexilExecutive(File plx) {
		this.plx = plx;
	}
	
	public OfficialPlexilExecutive setLibDir(File dir) {
		this.libDir = Optional.of(dir);
		return this;
	}
	
	public OfficialPlexilExecutive setLibDir(Optional<File> dir) {
		this.libDir = dir;
		return this;
	}
	
	public OfficialPlexilExecutive setScript(File psxFile) {
		this.psx = Optional.of(psxFile);
		return this;
	}
	
	public OfficialPlexilExecutive setScript(Optional<File> psxFile) {
		this.psx = psxFile;
		return this;
	}
	
	public OfficialPlexilExecutive addLibrary(File libPlx) {
		libs.add(libPlx);
		return this;
	}
	
	public BufferedReader generateLog() throws IOException {
		ProcessBuilder pb;
		if (System.getenv("PLEXIL_HOME") == null) {
			System.out.println("$PLEXIL_HOME is missing. The JVM must not be"
					+ " running in the proper environment.");
			System.out.println("I'll try running `plexiltest` inside"
					+" " +System.getenv("SHELL") +" for you instead.");
			String command = getPlexilTestCommand()
					.stream()
					.map(arg -> arg.contains(" ") ? "\""+arg+"\"" : arg)
					.collect(Collectors.joining(" "));
			pb = new ProcessBuilder(System.getenv("SHELL"), "-c", command);
		} else {
			// The environment looks okay as-is.
			 pb = new ProcessBuilder(getPlexilTestCommand());
		}
		
		
		Process process = pb.start();
		InputStream pipe = process.getInputStream();
		return new BufferedReader(new InputStreamReader(pipe));
	}
	
	public String generateLogString() throws IOException {
		BufferedReader reader = generateLog();
		String theLog = "";
		String line = null;
		while ((line = reader.readLine()) != null) {
			theLog += line;
		}
		return theLog;
	}
	
	private List<String> getPlexilTestCommand() {
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
		libs.forEach(libFile -> {
			args.add("-l");
			args.add(libFile.getPath());
		});
		
		return args;
	}
	
}
