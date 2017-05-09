package com.fuzz.processor;

import com.fuzz.datacontroller.annotations.DataDefinition;
import com.fuzz.datacontroller.annotations.ErrorMethod;
import com.fuzz.datacontroller.annotations.SuccessMethod;
import com.google.auto.service.AutoService;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class DataControllerProcessor extends AbstractProcessor {

    private DataControllerProcessorManager dataControllerProcessorManager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<>();
        supportedTypes.add(DataDefinition.class.getCanonicalName());
        supportedTypes.add(SuccessMethod.class.getCanonicalName());
        supportedTypes.add(ErrorMethod.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        dataControllerProcessorManager = new DataControllerProcessorManager(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        dataControllerProcessorManager.handle(dataControllerProcessorManager, roundEnv);
        return false;
    }
}
