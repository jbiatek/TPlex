package edu.umn.crisys.plexil.test.java;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.umn.crisys.plexil.main.Main;

public class CompileRegressionsWithMain {

	public static void main(String[] args) {
		File resources = new File("tests/edu/umn/crisys/plexil/test/resources");
		File dest = new File("tests");

		File[] files = resources.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".plx") || name.endsWith(".psx");
			}
		});
		
		List<String> mainArgs = new ArrayList<String>();
		mainArgs.add("--output-dir");
		mainArgs.add(dest.getAbsolutePath());
		mainArgs.add("--package");
		mainArgs.add("generated");
		for (File file : files ) {
			mainArgs.add(file.getAbsolutePath());
		}
		
		Main.main(mainArgs.toArray(new String[]{}));
	}

}
