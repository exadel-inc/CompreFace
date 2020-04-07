package com.exadel.frs.core.trainservice.system.python;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ScanResult {

    private ScanBox box;
    private List<Double> embedding = new ArrayList<>();
}