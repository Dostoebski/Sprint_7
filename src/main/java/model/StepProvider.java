package model;

import io.qameta.allure.Step;

public class StepProvider {
    @Step("{0}")
    public static void step(String message){
    }
}
