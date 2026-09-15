package org.bee.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SampleService {


   private final SampleRepository sampleRepository;

    @Autowired
    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public  String sample(String item) {
        Integer itemNumber = sampleRepository.findByItem(item);;
        return String.valueOf(itemNumber);
    }
}
