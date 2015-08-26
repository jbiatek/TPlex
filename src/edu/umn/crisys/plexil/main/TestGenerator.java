package edu.umn.crisys.plexil.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class TestGenerator {

	public static enum GenerationMethod {
		RANDOM
	}
	
	@Parameter(names = "--method")
	public GenerationMethod generationMethod = GenerationMethod.RANDOM;
	
	
	public String usage;
	
	public static void main(String... args) {
		TestGenerator m = new TestGenerator();
		
		JCommander cmd = new JCommander(m);
		cmd.setColumnSize(75);
		
		StringBuilder usage = new StringBuilder();
		cmd.usage(usage);
		m.usage = usage.toString();

		try {
			cmd.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			return;
		}
		
		m.execute();

		
	}
	
	
	public void execute() {
		
	}
}
