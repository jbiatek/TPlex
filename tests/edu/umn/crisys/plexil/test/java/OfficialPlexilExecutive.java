package edu.umn.crisys.plexil.test.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OfficialPlexilExecutive {

	public static final String PLEXIL_HOME = "PLEXIL_HOME";
	
	private File plx;
	private Optional<File> psx = Optional.empty();
	private Optional<File> libDir = Optional.empty();
	private Optional<File> debugFile = Optional.empty();
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
	
	public OfficialPlexilExecutive setDebugFile(File debugFile) {
		this.debugFile = Optional.of(debugFile);
		return this;
	}
	
	public OfficialPlexilExecutive setDebugFile(Optional<File> debugFile) {
		this.debugFile = debugFile;
		return this;
	}
	
	public OfficialPlexilExecutive addLibrary(File libPlx) {
		libs.add(libPlx);
		return this;
	}
	
	public boolean environmentSetCorrectly() {
		return System.getenv(PLEXIL_HOME) != null;
	}
	
	public BufferedReader generateLog() throws IOException {
		ProcessBuilder pb;
		if (environmentSetCorrectly()) {
			 pb = new ProcessBuilder(getPlexilTestCommand());
		} else {
			throw new RuntimeException("$PLEXIL_HOME must be set in order to run PLEXIL.");
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
		args.add(System.getenv(PLEXIL_HOME)+"/scripts/plexiltest");
		args.add("-q");
		args.add("-p");
		args.add(plx.getPath());
		if (psx.isPresent()) {
			args.add("-s");
			args.add(psx.get().getPath());
		}
		if (debugFile.isPresent()) {
			args.add("-d");
			args.add(debugFile.get().getPath());
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
