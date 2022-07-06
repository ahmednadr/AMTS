package com.company.src.SimController;

import com.company.src.Automation.Orchestrator;
import com.company.src.ResourcesMonitor.BW_Monitor;
import com.company.src.ResourcesMonitor.CPU_Monitor;
import com.company.src.SimController.Interfaces.Broker.PythonBroker;
import com.company.src.SimController.Interfaces.Container.PythonContainer;
import com.company.src.SimController.Interfaces.Listeners.Listener;
import com.company.src.WorkLoad.WorkLoad;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.listeners.EventInfo;
import py4j.GatewayServer;

import java.util.ArrayList;
import java.util.List;

public class SimBuilder {
    static GatewayServer gatewayServer;
    List<Listener> listeners = new ArrayList<Listener>();

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }
    public static void main(String[] args) {
        gatewayServer = new GatewayServer(new SimBuilder());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

    public Host Create_Host (int host_pe,int host_mips,int host_ram,int host_bw,int host_storage){
        final var peList = new ArrayList<Pe>(host_pe);
        for (int i = 0; i < host_pe; i++) {
            peList.add(new PeSimple(host_mips));
        }
        return new HostSimple(host_ram, host_bw, host_storage, peList);
    }

    public Vm Create_Vm(int mips,int pes,int ram,int bw ,int storage){
        return new VmSimple(mips,pes).setBw(bw).setRam(ram).setSize(storage);
    }

    public CloudSim Create_sim(){
        var x=new CloudSim();
        x.addOnClockTickListener(this::onClockTick);
        return x;
    }

    public ArrayList<Host> Create_HostList(){
        return new ArrayList<Host>();
    }

    public DatacenterSimple Create_Datacenter(CloudSim sim,ArrayList<Host> hosts){
       return new DatacenterSimple(sim, hosts);
    }

    public CPU_Monitor Create_CPUMonitor (CloudSim sim,boolean history , String path ,double ReportBW){
        return new CPU_Monitor(sim,history,path,ReportBW);
    }
    public BW_Monitor Create_BWMonitor (CloudSim sim, boolean history , String path ,  double ReportBW){
        return new BW_Monitor(sim,history,path,ReportBW);
    }

    public Orchestrator Create_Orchestrator(CloudSim sim , double max,double min,int Concurrency,double MS){
        return new Orchestrator(sim, min,max,Concurrency,MS);
    }

    public PythonBroker Create_PythonBroker(CloudSim sim, String name){
        return new PythonBroker(sim,name);
    }
    public PythonContainer Create_PythonContainer(int pes, double minUtilization, double maxUtilization, int ConcurrencyValue){
        return new PythonContainer( pes,  minUtilization,  maxUtilization,  ConcurrencyValue);
    }

    public WorkLoad Create_Workload(String path){
        return new WorkLoad(path);
    }

    public void stop(){
        gatewayServer.shutdown();
    }

    public void onClockTick(EventInfo e) {
        listeners.get(0).onClockTick(e);
    }
}
