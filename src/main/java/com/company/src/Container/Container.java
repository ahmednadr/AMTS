package com.company.src.Container;

import com.company.src.Automation.Orchestrator;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.events.CloudSimEvent;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;

public abstract class Container
        implements  Comparable<Container>
{


    private Cloudlet MainProcess;
    /** list of sub processes belonging to this container
     * */
    private ArrayList<Cloudlet> Cloudlets;

    protected double MinUtilization;
    protected double MaxUtilization;
    private UtilizationModelDynamic BWdynamic;
    private UtilizationModelDynamic CPUdynamic;
    protected int PEs;

    public Container setState(ContainerState state) {
        this.state=state;
        return this;
    }

    protected int ConcurrencyValue;

    public ContainerState state = ContainerState.FULL;

    private boolean isRunning;

    private Vm vm;
    private Orchestrator broker;


    public Cloudlet getMainProcess() {
        return MainProcess;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setBroker(Orchestrator broker) {
        this.broker = broker;
    }

    /**
     * should be only used by {@link Orchestrator}
     * when running container for the first time
     * @param vm
     */
    public void setVm(Vm vm) throws ContianerAlreadyRunningException {
        if(isRunning)
            throw new ContianerAlreadyRunningException();
        this.vm = vm;
        Cloudlets.add(MainProcess);
        isRunning=true;
    }
    public Vm getVm() {
       return this.vm;
    }


    public Container (int pes ,double minUtilization ,double maxUtilization ,int ConcurrencyValue){

        this.MaxUtilization=maxUtilization;
        this.MinUtilization=minUtilization;
        this.PEs=pes;
        this.ConcurrencyValue=ConcurrencyValue;
        Cloudlets=new ArrayList<>();

         CPUdynamic = new UtilizationModelDynamic(minUtilization,maxUtilization)
                .setUtilizationUpdateFunction(this::CPUUpdateUtilization);
         BWdynamic = new UtilizationModelDynamic(minUtilization,maxUtilization)
                .setUtilizationUpdateFunction(this::BWUpdateUtilization);

        this.MainProcess=new CloudletSimple(-1,pes)
                .setUtilizationModelCpu(CPUdynamic)
                .setUtilizationModelRam(UtilizationModel.NULL)
                .setUtilizationModelBw(BWdynamic);

    }
    /**
     * implementation of {@link Comparable} interface compare two containers based on cpu utilization
     * */
    public int compareTo(Container c){
        if(this.CPUdynamic.getUtilization() > c.CPUdynamic.getUtilization())
            return 1;
        if(this.CPUdynamic.getUtilization() < c.CPUdynamic.getUtilization())
            return -1;
        return 0;
    }
/**
 * function to be called every sim clock tick to update container cpu utilization
 * */
    public abstract Double CPUUpdateUtilization(UtilizationModelDynamic utilizationModelDynamic) ;
    /**
     * function to be called every sim clock tick to update container bw utilization
     * */
    public abstract Double BWUpdateUtilization(UtilizationModelDynamic utilizationModelDynamic) ;

    public abstract Container Clone();

    /**
     * run new sub process (task/cloudlet)
     * @param MI number of million instructions for this process
     * @param PEs number of cores required by the process
     * */
    public void NewCloudlet (int MI , int PEs) throws ContainerNotRunningException { // not sure public or private
        if (!isRunning)
            throw new ContainerNotRunningException();
        Cloudlet c = new CloudletSimple(MI,PEs);
        c.setVm(vm);
        broker.submitCloudlet(c);
        Cloudlets.add(c);
    }
    /**
     * stops the container and any sub process
     * */
    public void Destroy () throws ContainerNotRunningException {
        if (!isRunning)
            throw new ContainerNotRunningException();
        var broker =vm.getBroker();
        if (Cloudlets.size()==0)
            return;
        for (Cloudlet c : Cloudlets) {
            CloudSimEvent CancelEvent = new ContainerEvent(c,broker).Cancel;
            broker.processEvent(CancelEvent);
        }
    }


}
