package edu.umn.crisys.util;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

public class Util {
	
	private static SecurityManager oldSecurityManager = null;
	private static boolean sysExitBeingSuppressed = false;
	
	public static class SystemExitSuppressedException extends SecurityException {
		private static final long serialVersionUID = -3036213945357116554L;

		public SystemExitSuppressedException() {
			super("System.exit() supressed");
		}
	}

	public static synchronized void suppressSystemExit() {
		if (sysExitBeingSuppressed) {
			// We're already active. Someone is screwing up.
			throw new RuntimeException("System exit is already being suppressed!");
		}
		oldSecurityManager = System.getSecurityManager();
		final SecurityManager sm = new SecurityManager() {
			@Override
			public void checkPermission(Permission permission) {
				if (permission.getName().contains("exitVM")) {
					throw new SystemExitSuppressedException();
				}
			}
		};
		System.setSecurityManager(sm);
		sysExitBeingSuppressed = true;
	}

	public static synchronized void returnSystemExitToNormal() {
		if (! sysExitBeingSuppressed) {
			// It's already normal. Someone is screwing up.
			throw new RuntimeException("System exit is already normal!");
		}
		System.setSecurityManager(oldSecurityManager);
		sysExitBeingSuppressed = false;
	}

	@SafeVarargs
	public static <T> Set<T> asHashSet(T... items) {
		HashSet<T> ret = new HashSet<>();
		for (T item : items) {
			ret.add(item);
		}
		return ret;
	}
	
	public static <T> Set<T> merge(Set<T> s1, Set<T> s2) {
		HashSet<T> newSet = new HashSet<>();
		newSet.addAll(s1);
		newSet.addAll(s2);
		return newSet;
	}
	
}
