package com.company.src.SimController.Interfaces.Broker;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerAbstract;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.vms.Vm;

public class PythonBroker extends DatacenterBrokerAbstract {
    /**
     * Creates a DatacenterBroker giving a specific name.
     * Subclasses usually should provide this constructor and
     * and overloaded version that just requires the {@link CloudSim} parameter.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     * @param name       the DatacenterBroker name
     */

    private PythonBrokerInterface p;
    public PythonBroker(CloudSim simulation, String name) {
        super(simulation, name);
    }

    public void Submit_interface(PythonBrokerInterface p){
        this.p=p;
    }

    @Override
    protected Datacenter defaultDatacenterMapper(Datacenter lastDatacenter, Vm vm) {
        return p.defaultDatacenterMapper(lastDatacenter,vm);
    }

    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        return p.defaultVmMapper(cloudlet);
    }
}
