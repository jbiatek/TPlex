package edu.umn.crisys.plexil.il2lustre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.umn.crisys.plexil.runtime.values.PString;
import edu.umn.crisys.plexil.runtime.values.StringValue;
import edu.umn.crisys.plexil.runtime.values.UnknownValue;
import edu.umn.crisys.util.NameUtils;

public class ReverseTranslationMap {

	private Map<String,StringValue> allExpectedStrings = new HashMap<>();
	private boolean expectUnknownString = true;
	private Map<String,String> lookups = new HashMap<>();
	private Map<String,String> commandHandleInputs = new HashMap<>();
	
	public ReverseTranslationMap() {
		// Add in the empty string to start.
		allExpectedStrings.put(LustreNamingConventions.EMPTY_STRING, 
				StringValue.get(""));
		// And always expect the unknown string. 
		// It was dumb to ever not expect it.
	}
	
	public String stringToEnum(PString v) {
		if (v.isUnknown()) {
			expectUnknownString = true;
			return LustreNamingConventions.UNKNOWN_STRING;
		}
		StringValue knownString = (StringValue) v;
		if (knownString.getString().equals("")) {
			return LustreNamingConventions.EMPTY_STRING;
		}

		
		String toReturn = NameUtils.clean(knownString.getString().replaceAll(" ", "_")+"/str");
		if (toReturn.equals("")) {
			throw new RuntimeException("Enumerated string was empty: "+knownString);
		}
		if (allExpectedStrings.containsKey(toReturn)) {
			PString prev = allExpectedStrings.get(toReturn);
			if (prev.equalTo(knownString).isNotTrue()) {
				throw new RuntimeException("Name clash: \""+knownString+"\" and \""+prev+"\"");
			}
		}
		// Okay, it's all good. Save this mapping.
		allExpectedStrings.put(toReturn, knownString);
		return toReturn;
	}
	
	public Set<String> getAllExpectedStringIds() {
		Set<String> toReturn = new HashSet<String>(allExpectedStrings.keySet());
		if (expectUnknownString) {
			toReturn.add(LustreNamingConventions.UNKNOWN_STRING);
		}
		return toReturn;
	}
	
	public void addLookupMapping(String inputId, String lookupName) {
		lookups.put(inputId, lookupName);
	}
	
	public void addCommandHandleMapping(String rawHandleId, String commandName) {
		commandHandleInputs.put(rawHandleId, commandName);
	}
	
	public Optional<PString> getPStringFromEnumId(String enumId) {
		if (enumId.equals(LustreNamingConventions.UNKNOWN_STRING)) {
			return Optional.of(UnknownValue.get());
		}
		return Optional.ofNullable(allExpectedStrings.get(enumId));
	}
	
	public Optional<String> getLookupNameFromId(String inputId) {
		return Optional.ofNullable(lookups.get(inputId));
	}
	
	public Optional<String> getIdFromLookupName(String lookupName) {
		return lookups.entrySet().stream()
				.filter(e -> e.getValue().equals(lookupName))
				.findFirst()
				.map(e -> e.getKey());
	}
	
	public Optional<String> getCommandNameFromHandleId(String handleId) {
		return Optional.ofNullable(commandHandleInputs.get(handleId));
	}
}
