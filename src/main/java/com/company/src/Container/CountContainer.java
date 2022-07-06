package com.company.src.Container;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import static java.lang.Math.abs;
import static org.cloudbus.cloudsim.core.CloudSim.LOGGER;

public class CountContainer extends Container{
    /**
     * number of requests currently handled by this container
     * */
    private double count = 0;
    /**
     * single request message size in MBs
     * */
    private double MessageSize ; //    MB
    public double MonitorBW = 0;
    /**
     * accumalative size of packets waiting to be sent due to full bw utilization
     * */
    private double WaitingPackets = 0;

    public CountContainer(int pes, double minUtilization, double maxUtilization, int ConcurrencyValue, double Ms) {
        super(pes, minUtilization, maxUtilization, ConcurrencyValue);
        this.MessageSize = Ms;
    }

    @Override
    public Double CPUUpdateUtilization(UtilizationModelDynamic utilizationModelDynamic) {
        if(this.state==ContainerState.FULL)
            return 1.0;

        double percentage = (count/ConcurrencyValue);

        LOGGER.trace("Base Cloudlet Utilization : {}",(count/ConcurrencyValue));

        return percentage;
    }

    @Override
    public Double BWUpdateUtilization(UtilizationModelDynamic utilizationModelDynamic) {
        var vmBW = this.getMainProcess().getVm().getBw().getCapacity();
        var vmAvailableBW = this.getMainProcess().getVm().getBw().getAvailableResource();
        double Current = (count*MessageSize) + MonitorBW;
        MonitorBW = 0;

        if (Current == vmAvailableBW)
            return Current/vmBW;
        if(Current > vmAvailableBW){
            WaitingPackets += Current - vmAvailableBW;
            return Double.valueOf(vmAvailableBW/vmBW);
        }else{
            double ReadyPackets = vmAvailableBW - Current > WaitingPackets ? WaitingPackets : vmAvailableBW - Current;
            WaitingPackets -= ReadyPackets;
            return (Current + ReadyPackets) /vmBW;
        }
    }
    public void setCount(int c){
        if(c > this.ConcurrencyValue){
            this.count = ConcurrencyValue;
            return;
        }
        this.count = c;
    }
    public int getCount(){
        return (int) this.count;
    }
    public int updateCount(int c){
        if(c<0){
            if(abs(c)>this.count){
                int remainder = (int) - (abs(c) - this.count);
                this.count=0;
                return remainder;
            }else{
                this.count += c;
                return 0;
            }
        }else{
            if((c + this.count) > this.ConcurrencyValue){
                int remainder = (int) (c + this.count) - ConcurrencyValue;
                this.count = ConcurrencyValue;
                return remainder;
            }else{
                this.count += c;
                return 0;
            }
        }
    }
    @Override
    public Container Clone() {
        return new CountContainer(this.PEs,this.MinUtilization, this.MaxUtilization, this.ConcurrencyValue, this.MessageSize);
    }

    @Override
    public CountContainer setState(ContainerState state) {
        super.setState(state);
        return this;
    }
}
