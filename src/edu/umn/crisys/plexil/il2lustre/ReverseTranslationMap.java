package edu.umn.crisys.plexil.il2lustre;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.umn.crisys.plexil.NameUtils;
import edu.umn.crisys.plexil.runtime.values.PString;

public class ReverseTranslationMap {

	private Map<String,PString> allExpectedStrings = new HashMap<>();
	private Map<String,String> lookups = new HashMap<>();
	private Map<String,String> commandHandleInputs = new HashMap<>();
	
	public String stringToEnum(PString v) {
		if (v.getString().equals("")) {
			allExpectedStrings.put(LustreNamingConventions.EMPTY_STRING, v);
			return LustreNamingConventions.EMPTY_STRING;
		}
		if (v.isUnknown()) {
			allExpectedStrings.put(LustreNamingConventions.UNKNOWN_STRING, v);
			return LustreNamingConventions.UNKNOWN_STRING;
		}
		
		String toReturn = NameUtils.clean(v.getString().replaceAll(" ", "_"));
		if (toReturn.equals("")) {
			throw new RuntimeException("Enumerated string was empty: "+v);
		}
		if (allExpectedStrings.containsKey(toReturn)) {
			PString prev = allExpectedStrings.get(toReturn);
			if (prev.equalTo(v).isNotTrue()) {
				throw new RuntimeException("Name clash: \""+v+"\" and \""+prev+"\"");
			}
		}
		// Okay, it's all good. Save this mapping.
		allExpectedStrings.put(toReturn, v);
		return toReturn;
	}
	
	public Set<String> getAllExpectedStringIds() {
		return allExpectedStrings.keySet();
	}
	
	public void addLookupMapping(String inputId, String lookupName) {
		lookups.put(inputId, lookupName);
	}
	
	public void addCommandHandleMapping(String rawHandleId, String commandName) {
		commandHandleInputs.put(rawHandleId, commandName);
	}
	
	public Optional<PString> getPStringFromEnumId(String enumId) {
		return Optional.ofNullable(allExpectedStrings.get(enumId));
	}
	
	public Optional<String> getLookupNameFromId(String inputId) {
		return Optional.ofNullable(lookups.get(inputId));
	}
	
	public Optional<String> getCommandNameFromHandleId(String handleId) {
		return Optional.ofNullable(commandHandleInputs.get(handleId));
	}
}
