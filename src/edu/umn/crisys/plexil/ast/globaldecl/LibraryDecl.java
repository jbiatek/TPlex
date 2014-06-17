package edu.umn.crisys.plexil.ast.globaldecl;

public class LibraryDecl {

	private String id;
	private PlexilInterface iface;
	
	public LibraryDecl(String libId, PlexilInterface iface) {
		this.id = libId;
		this.iface = iface;
	}
	
	public String getPlexilId() {
		return id;
	}
	
	public PlexilInterface getDeclaredInterface() {
		return iface;
	}
}
