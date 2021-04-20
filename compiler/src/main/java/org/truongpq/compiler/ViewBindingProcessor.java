package org.truongpq.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.truongpq.annotation.BindView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@SupportedAnnotationTypes("org.truongpq.annotation.BindView")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ViewBindingProcessor extends AbstractProcessor {

    private static final String CONST_PARAM_TARGET_NAME = "activity";

    private static final String TARGET_STATEMENT_FORMAT =
            "activity.%1s = (%2s) activity.findViewById(%3s)";

    private Filer mFiler;
    private Types mTypesUtils;
    private Elements mElementsUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mTypesUtils = processingEnvironment.getTypeUtils();
        mElementsUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Map<String, List<Element>> bindViewMap = new HashMap<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            if (mTypesUtils.isSubtype(element.asType(),
                    mElementsUtils.getTypeElement("android.view.View").asType())) {

                TypeElement typeElement = (TypeElement) element.getEnclosingElement();
                String key = typeElement.getQualifiedName().toString();
                if (bindViewMap.get(key) == null) {
                    bindViewMap.put(key, new ArrayList<Element>());
                }
                bindViewMap.get(key).add(element);
            }
        }
        if (bindViewMap.isEmpty()) {
            return true;
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder("ViewBinding")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (Map.Entry<String, List<Element>> entry : bindViewMap.entrySet()) {
            builder.addMethod(createMethod(entry.getValue()));
        }

        JavaFile javaFile =
                JavaFile.builder("org.truongpq.viewbinding", builder.build()).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private MethodSpec createMethod(List<Element> elements) {
        Element firstElement = elements.get(0);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addParameter(TypeName.get(firstElement.getEnclosingElement().asType()),
                        CONST_PARAM_TARGET_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            builder.addStatement(
                    String.format(TARGET_STATEMENT_FORMAT, element.getSimpleName().toString(),
                            element.asType().toString(),
                            element.getAnnotation(BindView.class).value()));
        }
        return builder.build();
    }
}