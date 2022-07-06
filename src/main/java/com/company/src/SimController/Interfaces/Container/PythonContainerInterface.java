package com.company.src.SimController.Interfaces.Container;

import com.company.src.Container.Container;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;

public interface PythonContainerInterface {
    /** an interface to be implemented by pythonContainer inheriting class
     *
     * */
    Double CPUUpdateUtilization(UtilizationModelDynamic utilizationModelDynamic);
    Double BWUpdateUtilization(UtilizationModelDynamic utilizationModelDynamic);

    Container Clone();
//    void Destroy();
}
