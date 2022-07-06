package com.company.src.ResourcesMonitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.company.src.Automation.Orchestrator;
import com.company.src.Container.Container;
import com.company.src.Container.CountContainer;
import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.EventInfo;

public class BW_Monitor implements ResourceMonitor{
//    list of monitored vms
    private ArrayList<Vm> MonitoredVMs = new ArrayList<Vm>();
    // map of vm and its current utilization percentage
    private HashMap<Vm,Double> CurrentPercentages = new HashMap<>();
    private Simulation Sim;
//    path to write metrics files
    private String path;
    private String outputFilePath
            = "/BW Metrics Output.csv";
    CSVWriter writer = null;
    private ArrayList<CSVWriter> vm_output;
//    bandwidth required for reporting metrics
    public double ReportBW;


    private double PreviousTime = 00.0;
    private double CurrentTime = 00.0;

    public BW_Monitor(Simulation Sim , boolean history , String path , double ReportBW) {
        this.Sim=Sim;
        this.path=path;
        this.outputFilePath=path+outputFilePath;
        this.ReportBW=ReportBW;
        this.vm_output = new ArrayList<CSVWriter>();
        Sim.addOnClockTickListener(this::UpdateCurrentVmMetrics);

//       history boolean set true if metrics are to be saved to BW Metrics Output.csv file
        if (history){

            File file = new File(outputFilePath);
            try {
                writer = new CSVWriter(new FileWriter(file), '|',
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Sim.addOnClockTickListener(this::LogMetricsHistory);
        }

    }
/** add a vm to be monitored by BW monitor
 * @param vm vm to be monitored
 * */
    @Override
    public void add(Vm vm) {
        MonitoredVMs.add(vm);
        File file = new File(path+"/vm "+MonitoredVMs.size()+" BW metrics.csv");
        try {
            CSVWriter vmwriter = new CSVWriter(new FileWriter(file), '|',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            vm_output.add(vmwriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * function called every sim clock tick to update the map of vm current utilization percentages
     * @param evt simulation event
     * */
    private void UpdateCurrentVmMetrics (final EventInfo evt){
        CurrentPercentages.clear();
        MonitoredVMs.forEach((vm)->{
            var c = vm.getHost().getBw().getCapacity();
            var VM_percentage = (vm.getHostBwUtilization()*c)/vm.getBw().getCapacity();
            CurrentPercentages.put(vm,VM_percentage);
        });
    }

    @Override
    public double getCurrentTotalPercentage() {
        
        double totalused = 0.0;
        double totalBW = 0.0;
        double totalpercentage ;

        for (Vm vm : CurrentPercentages.keySet()){
            var VmBW = vm.getBw().getCapacity();
            totalBW += VmBW;
            totalused += VmBW * CurrentPercentages.get(vm);
        }

        totalpercentage=totalused/totalBW;

        return totalpercentage;
    }

    public double getVmCurrentpercentage(Vm vm){
        reporting_load(vm,ReportBW);
        return CurrentPercentages.get(vm);
    }
    private double getVmmetric(Vm vm){
        return CurrentPercentages.get(vm);
    }
    private void reporting_load(Vm vm,double BW){
        var vmBW = vm.getBw().getCapacity();
        Orchestrator b =(Orchestrator) vm.getBroker();
        CountContainer c ;
        for (Container cc:
                b.RunningContainers) {
            if(cc.getVm()==vm){
                c = (CountContainer) cc;
                c.MonitorBW = BW;
            }
        }
    }
    private void LogMetricsHistory(EventInfo evt) {
        CurrentTime = evt.getTime();
        
        if ((int) Math.floor(CurrentTime)-(int)Math.floor(PreviousTime)==0)
            return;

        var current = getCurrentTotalPercentage();
        writer.writeNext(new String[] {CurrentTime+"",current+""});

        for (int i = 0; i < MonitoredVMs.size(); i++) {
            var vmBW = getVmmetric(MonitoredVMs.get(i));
            vm_output.get(i).writeNext(new String[]{CurrentTime+"",vmBW+""});
        }


        PreviousTime = CurrentTime;
    }
    
}
