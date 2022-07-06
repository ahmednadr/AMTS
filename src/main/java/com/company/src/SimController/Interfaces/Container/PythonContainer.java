package com.company.src.SimController.Interfaces.Container;

import com.company.src.Container.Container;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;

public class PythonContainer extends Container {

    private PythonContainerInterface c;

//    private boolean tried_python_Destroy = false;

    public PythonContainer(int pes, double minUtilization, double maxUtilization, int ConcurrencyValue) {
        super(pes, minUtilization, maxUtilization, ConcurrencyValue);
    }
    public void Submit_interface(PythonContainerInterface c){
        this.c=c;
    }
    @Override
    public Double CPUUpdateUtilization(UtilizationModelDynamic utilizationModelDynamic) {
        return c.CPUUpdateUtilization(utilizationModelDynamic);
    }

    @Override
    public Double BWUpdateUtilization(UtilizationModelDynamic utilizationModelDynamic) {
        return c.BWUpdateUtilization(utilizationModelDynamic);
    }

    @Override
    public Container Clone() {
        return c.Clone();
    }

//    @Override
//    public void Destroy() throws ContainerNotRunningException {
//        if(!tried_python_Destroy){
//            c.Destroy();
//            tried_python_Destroy=true;
//        }
//        super.Destroy();
//    }

}
