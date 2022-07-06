package com.company.src.ResourcesMonitor;

import com.company.src.Automation.Orchestrator;
import com.company.src.Container.Container;
import com.company.src.Container.CountContainer;
import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.listeners.EventInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CPU_Monitor implements ResourceMonitor {


    private ArrayList<Vm> MonitoredVMs = new ArrayList<Vm>();
    private HashMap<Vm,Double> CurrentPercentages = new HashMap<>();
    private Simulation Sim;
    private String path;
    private String outputFilePath
            = "/CPU Metrics Output.csv";
    private ArrayList<CSVWriter> vm_output;
    CSVWriter writer = null;
    public double ReportBW;

    private double PreviousTime = 00.0;
    private double CurrentTime = 00.0;

    public CPU_Monitor(Simulation Sim , boolean history , String path  , double ReportBW) {

        this.Sim=Sim;
        this.path=path;
        this.outputFilePath=path + outputFilePath;
        this.ReportBW=ReportBW;
        this.vm_output = new ArrayList<CSVWriter>();
        Sim.addOnClockTickListener(this::UpdateCurrentVmMetrics);


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

    public void add(Vm vm){
        MonitoredVMs.add(vm);
        File file = new File(path+"/vm "+MonitoredVMs.size()+" CPU metrics.csv");
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

    private void UpdateCurrentVmMetrics (final EventInfo evt){
        CurrentPercentages.clear();
        MonitoredVMs.forEach((vm)->{
            var VM_percentage = vm.getCpuPercentUtilization(evt.getTime());
            CurrentPercentages.put(vm,VM_percentage);
        });
    }

    private void LogMetricsHistory (final EventInfo evt){
        CurrentTime = evt.getTime();
        
        if ((int) Math.floor(CurrentTime)-(int)Math.floor(PreviousTime)==0)
            return;

        var current = getCurrentTotalPercentage();
        writer.writeNext(new String[] {CurrentTime+"",current+""});

        for (int i = 0; i < MonitoredVMs.size(); i++) {
            var vmCPU = getVmmetric(MonitoredVMs.get(i));
            vm_output.get(i).writeNext(new String[]{CurrentTime+"",vmCPU+""});
        }

        PreviousTime = CurrentTime;
    }

    public double getCurrentTotalPercentage(){

        double totalused = 0.0;
        double totalMIPS = 0.0;
        double totalpercentage ;

        for (Vm vm : CurrentPercentages.keySet()){
            var VmMIPS = vm.getTotalMipsCapacity();
            totalMIPS += VmMIPS;
            totalused += VmMIPS * CurrentPercentages.get(vm);
//          reporting_load(vm,ReportMI,ReportBW);
        }

        totalpercentage=totalused/totalMIPS;

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
}
