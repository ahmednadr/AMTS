package com.company.src.ResourcesMonitor;

import org.cloudsimplus.listeners.EventInfo;
import org.cloudbus.cloudsim.vms.Vm;


public interface ResourceMonitor {

     void add(Vm vm);

     double getCurrentTotalPercentage();

}
