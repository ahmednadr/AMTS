package com.company.src.Automation;

import com.company.src.Container.*;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerFirstFit;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.EventInfo;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.abs;


/**
 * an horizontal autoscaling broker that creates and destroys instances ({@link Cloudlet} - {@link Container})
 * to adapt to {@link com.company.src.WorkLoad.WorkLoad}
 *
 * @author Ahmed Nader
 */


public class Orchestrator extends DatacenterBrokerFirstFit {
    /**
     * number of requests indexed by time in seconds
     * */
    private ArrayList<Integer> WorkLoad;

    private double PreviousTime = 0.0;
    private double CurrentTime = 0.0;

    /**
     * number of request at PreviousTime
     */
    private int PreviousCount = 0;

    /** concurrency setting that specifies the maximum number of requests
    that can be processed simultaneously by a given container instance */
    private int ConcurrencyValue;
    private double MessageSize;

    /** the current number of containers running*/ 
    private int CurrentLoad = 0;


    /**
     * specify container min and max util of a single core
     */
    private double ContainerMinUtil;
    private double ContainerMaxUtil ;

    /** a list of running containers */
    public ArrayList<Container> RunningContainers;



    public void setWorkLoad(ArrayList<Integer> WorkLoad) {
        this.WorkLoad = WorkLoad;
    }


    public Orchestrator(CloudSim simulation ,  double Min , double Max , int ConcurrencyValue , double MessageSize){
        super(simulation);
        this.ContainerMaxUtil=Max;
        this.ContainerMinUtil=Min;
        this.ConcurrencyValue=ConcurrencyValue;
        this.MessageSize = MessageSize;
        RunningContainers=new ArrayList<>();

        // call the method Run everytime the simulator clock ticks
        simulation.addOnClockTickListener(this::Run);
    }



    /**
     * starts a new container and adds it to  RunningContainer
     * @param container the container to be started
    * */
    private void RunContainer (Container container) throws ContianerAlreadyRunningException {
        if(container.isRunning())
            throw new ContianerAlreadyRunningException();
//      broker assign container main process to a vm
        super.submitCloudlet(container.getMainProcess());

//      add container to running containers list
        RunningContainers.add(container);
        Vm ContainerVm = container.getMainProcess().getVm();

//      set container's vm and broker
        container.setVm(ContainerVm);
        container.setBroker(this);
    }

    /** stops a running container and removes it from the RunningContainer array
     * @param container the container to be stopped
    */
    private void DestroyContainer (Container container) throws ContainerNotRunningException {
        container.Destroy();
        RunningContainers.remove(container);
    }

    /** runs n new containers 
     * @param n number of new containers
     */
    private void ScaleUp(int n){
        for (int i = 0; i < n; i++) {
            try {
                RunContainer (new CountContainer(1,ContainerMinUtil,ContainerMaxUtil,ConcurrencyValue,MessageSize)
                        .setState(ContainerState.ADAPTIVE));
            } catch (ContianerAlreadyRunningException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("{}: {}: Spinned up {} new containers",getSimulation().clockStr(),getName(),n);
    }

    /** stops n running containers
     * @param n number of containers to be stopped
    */
    private  void ScaleDown(int n){
        for (int i = 0; i < n; i++) {
            try {
                DestroyContainer(RunningContainers.get(0));
            } catch (ContainerNotRunningException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("{}: {}: Stopped {} containers",getSimulation().clockStr(),getName(),n);
    }


    /** create and stop instances to adapt to current work loads
     * @param evt details about the current state of the simulation
     */

    public void Run(final EventInfo evt) {

        // update the clock
        CurrentTime = evt.getTime();

        // to avoid floating point errors as the time never increment to an exact second
        if ((int) Math.floor(CurrentTime)-(int)Math.floor(PreviousTime)==0)
            return;

        //Update every 10 secs instead every second to smooth out the noise and rapid changes.
        if ((int)Math.floor(CurrentTime)%10 != 0)
            return;
        
        int currentCount = WorkLoad.get((int) Math.floor(CurrentTime));
        int new_requests = currentCount - PreviousCount;

        int index = 0;

        int IncomingLoad = currentCount / ConcurrencyValue + 1;
        
        int Diff = IncomingLoad - this.CurrentLoad;
        if (Diff != 0) {
            if (Diff > 0) {
                ScaleUp(abs(Diff));
            } else
                ScaleDown(abs(Diff));
        }
        Collections.sort(RunningContainers);
        boolean down = false;
        if(new_requests < 0){
            index = RunningContainers.size()-1;
            down = true;
        }

        CountContainer chosen = (CountContainer)RunningContainers.get(index);

        while (new_requests != 0){
            int remainder = chosen.updateCount(new_requests);
            new_requests = remainder;
            if(remainder !=0){
                if(down){
                    index --;
                }else{
                    index ++;
                }
            }

            chosen =(CountContainer) RunningContainers.get(index);
        }
        this.CurrentLoad = IncomingLoad;
        PreviousTime = CurrentTime;
        PreviousCount = 0;
        RunningContainers.forEach(x->{PreviousCount+=((CountContainer)x).getCount();});
    }


}
