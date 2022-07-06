package com.company.src;

import com.company.src.ResourcesMonitor.BW_Monitor;
import com.company.src.ResourcesMonitor.CPU_Monitor;
import com.company.src.WorkLoad.WorkLoad;
import org.cloudbus.cloudsim.core.CloudSim;
import com.company.src.Automation.Orchestrator;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.listeners.EventInfo;

import java.util.ArrayList;
import java.util.List;

public class javasim {

    private static final int hosts = 2;
    private static final int host_pe = 6;
    private static final int host_mips = 4000;
    private static final int host_ram = 32_768;      //MB
    private static final int host_bw = 10000;        //Mb/s
    private static final int host_storage = 1000000; //MB

    private static final int VMS = 2;
    private static final int VM_pes = 6;

    private static final double TIME_TO_TERMINATE_SIMULATION = 604600.0;

    private static CloudSim sim;
    private static CPU_Monitor cpu_monitor;
    private static List<Vm> vmList;

    public static void main(String[] args) {
        var c = new javasim();
        c.Start("/Users/test/Desktop/Ahmed Nader/bachelor/AMTS/src/dataset/ysb.csv","/Users/test/Desktop");
    }
    public void Start(String path ,String path2 ){

        sim = new CloudSim();
        sim.terminateAt(TIME_TO_TERMINATE_SIMULATION);
        WorkLoad s= new WorkLoad(path);

        createDataCenter();
        cpu_monitor = new CPU_Monitor(sim,true,path2,1);
        BW_Monitor bw_monitor = new BW_Monitor(sim,true,path2,1);
        vmList = createVms(cpu_monitor,bw_monitor);

        Orchestrator o = new Orchestrator(sim, 0.01,1.0,10000,0.00064);
        o.setWorkLoad(s.ParseCSV());
        o.submitVmList(vmList);

        sim.addOnClockTickListener(this::moni);
        sim.start();


        final List<Cloudlet> finishedCloudlets =
                o.getCloudletFinishedList();
        new CloudletsTableBuilder(finishedCloudlets).build();

    }
    public void moni (EventInfo e){
        double x = Math.random();
        if(x < 0.2 && e.getTime()%10 < 5)
            x = cpu_monitor.getVmCurrentpercentage(vmList.get(1));

    }

    private static void createDataCenter() {
        final var hostList = new ArrayList<Host>(hosts);
        for (int i = 0; i < hosts; i++) {
            final var host = createHost();
            hostList.add(host);
        }

        new DatacenterSimple(sim, hostList);
//        .setSchedulingInterval(1);
    }

    private static Host createHost(){
        final var peList = new ArrayList<Pe>(host_pe);
        //List of Host's CPUs (Processing Elements, PEs)
        for (int i = 0; i < host_pe; i++) {
            //Uses a PeProvisionerSimple by default to provision PEs for VMs
            peList.add(new PeSimple(host_mips));
        }

        /*
        Uses ResourceProvisionerSimple by default for RAM and BW provisioning
        and VmSchedulerSpaceShared for VM scheduling.
        */
        return new HostSimple(host_ram, host_bw, host_storage, peList);
    }

    private static List<Vm>  createVms(CPU_Monitor c , BW_Monitor b){
        final var vmlist = new ArrayList<Vm>(VMS);
        for (int i =0 ; i< VMS;i++){
            final var vm = new VmSimple(host_mips,VM_pes);
            vm.setBw(40).setRam(16_250).setSize(10_000);
            vmlist.add(vm);
            c.add(vm);
            b.add(vm);
        }
        return vmlist ;
    }

}
