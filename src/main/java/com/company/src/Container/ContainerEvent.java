package com.company.src.Container;

import com.company.src.Automation.Orchestrator;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.events.CloudSimEvent;

/**
 * ContainerEvent is an event sent by {@link Orchestrator} to a datacenter in order to create or destroy an instance
 *
 * @author Ahmed Nader Hassan
 */

public class ContainerEvent {

    public static CloudSimEvent Create;

    // a cancel event
    public static CloudSimEvent Cancel;

    /**creates a cancel/destroy event
     *
     * @param c Cloudlet to be destroyed
     * @param broker the {@link Orchestrator} broker handling this Cloudlet c
     */
    public ContainerEvent(Cloudlet c , DatacenterBroker broker){
        this.Cancel = new CloudSimEvent(0,broker, CloudSimTags.CLOUDLET_FINISH,c);
        this.Create = new CloudSimEvent(0,broker, CloudSimTags.CLOUDLET_SUBMIT,c);
    }

}
