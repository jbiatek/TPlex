package edu.umn.crisys.plexil.il.action;


public interface PlexilAction {
    public <P,R> R accept(ILActionVisitor<P, R> visitor, P param);
}
