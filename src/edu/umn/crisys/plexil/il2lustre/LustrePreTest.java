package edu.umn.crisys.plexil.il2lustre;

import java.io.File;

import jkind.lustre.Program;
import jkind.lustre.visitors.PrettyPrintVisitor;
import edu.umn.crisys.plexil.ast.PlexilPlan;
import edu.umn.crisys.plexil.ast2il.PlexilPlanToILPlan;
import edu.umn.crisys.plexil.il.Plan;
import edu.umn.crisys.plexil.main.optimizations.AssumeTopLevelPlan;
import edu.umn.crisys.plexil.main.optimizations.PruneUnusedVariables;
import edu.umn.crisys.plexil.main.optimizations.RemoveDeadTransitions;
import edu.umn.crisys.plexil.plx2ast.PlxParser;

public class LustrePreTest {

	public static void main(String[] args) throws Exception {
		PlexilPlan ast = PlxParser.parseFile(
				//new File("tests/edu/umn/crisys/plexil/test/resources/DriveToTarget.plx"));
				new File("cas2.plx"));
		Plan il = PlexilPlanToILPlan.translate(ast);
		AssumeTopLevelPlan.optimize(il);
		RemoveDeadTransitions.optimize(il);
		PruneUnusedVariables.optimize(il);
		
		
		Program lustre = PlanToLustre.toLustre(il, ast);
		
		PrettyPrintVisitor pp = new PrettyPrintVisitor();
		lustre.accept(pp);
		
		System.out.println(pp.toString());
		System.out.flush();
	}

}
