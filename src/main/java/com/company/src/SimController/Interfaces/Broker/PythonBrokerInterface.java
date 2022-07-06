package com.company.src.SimController.Interfaces.Broker;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.vms.Vm;

public interface PythonBrokerInterface {
    /** an interface to be implemented by pythonBroker inheriting class
     *
     * */
    Vm defaultVmMapper(Cloudlet cloudlet);
    Datacenter defaultDatacenterMapper(Datacenter lastDatacenter, Vm vm);
}
