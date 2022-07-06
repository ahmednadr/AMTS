package com.company.src.Container;

public class ContainerNotRunningException extends Exception{
    String msg = "can't create a new cloudlet in a container with no main process running " +
            "and cant destroy a not running contianer";
}
