package edu.umn.crisys.spf;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_edu_umn_crisys_spf_CheckpointControl extends NativePeer {

  @MJI
  public void setCheckpointFromInside (MJIEnv env, int clsObjRef) {
	  CheckpointSearch.SINGLETON.setCheckpoint();
  }
}