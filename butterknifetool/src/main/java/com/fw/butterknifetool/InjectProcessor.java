package com.fw.butterknifetool;

import com.fw.butterknifetool.bt.BindView;
import com.fw.butterknifetool.bt.OnClick;
import com.fw.butterknifetool.bt.UIContent;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
public class InjectProcessor extends AbstractProcessor{

    private Map<String, JavaFileDetail> javaFileMap = new HashMap<>();
    private Elements mElements; //用来处理程序元素的工具类
    private Filer filer; // 用来生成java文件的工具类

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processUIContent(annotations, roundEnv);
        processBindView(annotations, roundEnv);
        processBindClick(annotations, roundEnv);

        for (Map.Entry<String, JavaFileDetail> entry : javaFileMap.entrySet()) {
            JavaFile javaFile = JavaFile.builder(entry.getValue().getPackageName(), entry.getValue().generateFile()).build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //清除，否则会有一些未知错误
        javaFileMap.clear();
        return true;
    }

    /**
     * 保存有注解属性的变量
     * @param annotations
     * @param roundEnv
     */
    private void processBindView (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> bindViews = roundEnv.getElementsAnnotatedWith(BindView.class); //获取所有注解为BindView的属性
        for (Element element : bindViews) {
            if (element.getKind() == ElementKind.FIELD) { //如果注解为变量类型
                VariableElement variableElement = (VariableElement) element;
                int resId = variableElement.getAnnotation(BindView.class).value(); //获取注解的资源ID
                //获取注解的变量所属类的全名
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                String fullName = typeElement.getQualifiedName().toString();
                //获取java文件处理对象
                JavaFileDetail javaFileDetail = javaFileMap.get(fullName);
                if (javaFileDetail == null) {
                    javaFileDetail = new JavaFileDetail(mElements, typeElement);
                    //保存java文件处理对象
                    javaFileMap.put(fullName, javaFileDetail);
                }
                //缓存注解的绑定ViewId，准备生成java文件
                javaFileDetail.addViewId(resId, variableElement);
            }
        }
    }

    /**
     * 保存有注解属性的Click方法
     * @param annotations
     * @param roundEnv
     */
    private void processBindClick (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> bindClicks = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element element : bindClicks) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) element;
                int[] resIds = executableElement.getAnnotation(OnClick.class).value();
                //获取注解的方法所属类的全名
                TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
                String fullName = typeElement.getQualifiedName().toString();
                //获取java文件处理对象
                JavaFileDetail javaFileDetail = javaFileMap.get(fullName);
                if (javaFileDetail == null) {
                    javaFileDetail = new JavaFileDetail(mElements, typeElement);
                    //保存java文件处理对象
                    javaFileMap.put(fullName, javaFileDetail);
                }
                //缓存注解的绑定Click事件的Id，准备生成java文件
                javaFileDetail.addClickId(resIds, executableElement);
            }
        }
    }

    /**
     * 保存有注解属性的资源Id
     * @param annotations
     * @param roundEnv
     */
    private void processUIContent (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> uiContents = roundEnv.getElementsAnnotatedWith(UIContent.class);
        for (Element element : uiContents) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                int layoutId = typeElement.getAnnotation(UIContent.class).value();
                //获取类的全名
                String fullName = typeElement.getQualifiedName().toString();
                //获取java文件处理对象
                JavaFileDetail javaFileDetail = javaFileMap.get(fullName);
                if (javaFileDetail == null) {
                    javaFileDetail = new JavaFileDetail(mElements, typeElement);
                    //保存java文件处理对象
                    javaFileMap.put(fullName, javaFileDetail);
                }
                //保存layoutId，准备生成java文件
                javaFileDetail.setLayoutId(layoutId);
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        annotations.add(OnClick.class.getCanonicalName());
        annotations.add(UIContent.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 自动生成的java文件描述
     */
    private class JavaFileDetail {
        private String mPackageName; // 生成的包名
        private String mClassName; //生成的类名
        private TypeElement mTypeElement; //对应处理的类
        private Map<Integer, VariableElement> variableElementMap = new HashMap<>(); //需要绑定的控件集合
        private Map<int[], ExecutableElement> executableElementMap = new HashMap<>(); //需要绑定的点击控件集合
        private int layoutId;

        public JavaFileDetail(Elements elements, TypeElement mTypeElement) {
            this.mTypeElement = mTypeElement;
            this.mPackageName = elements.getPackageOf(mTypeElement).getQualifiedName().toString();
            this.mClassName = mTypeElement.getSimpleName() + "$$ViewBinder";
        }

        /**
         * 设置布局
         * @param layoutId
         */
        public void setLayoutId (int layoutId) {
            this.layoutId = layoutId;
        }

        /**
         * 添加控件
         * @param viewId
         * @param variableElement
         */
        public void addViewId (int viewId, VariableElement variableElement) {
            variableElementMap.put(viewId, variableElement);
        }

        /**
         * 添加点击控件
         * @param viewIds
         * @param executableElement
         */
        public void addClickId (int[] viewIds, ExecutableElement executableElement) {
            executableElementMap.put(viewIds, executableElement);
        }

        /**
         * 获取包名
         * @return
         */
        public String getPackageName() {
            return this.mPackageName;
        }

        /**
         * 获取待生成的文件信息
         * @return
         */
        public TypeSpec generateFile () {
            //生成unBind方法
            MethodSpec.Builder unBindBuild = MethodSpec.methodBuilder("unBind")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(TypeVariableName.get(mTypeElement.getSimpleName().toString()), "target");

            //生成bind方法
            MethodSpec.Builder bindBuild = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(TypeVariableName.get(mTypeElement.getSimpleName().toString()), "target")
                    .addStatement("target.setContentView(" + layoutId + ")");

            //实现bind和unBind方法中赋值控件Id的功能
            for (Map.Entry<Integer, VariableElement> entry : variableElementMap.entrySet()) {
                bindBuild.addStatement("target." + entry.getValue().getSimpleName().toString() +
                        "= (" + entry.getValue().asType().toString() + ") target.findViewById(" + entry.getKey() + ")");
                unBindBuild.addStatement("target." + entry.getValue().getSimpleName().toString() + "= null");
            }

            //实现控件点击事件方法
            for (Map.Entry<int[], ExecutableElement> entry : executableElementMap.entrySet()) {
                for (int id : entry.getKey()) {
                    bindBuild.addStatement("target.findViewById(" + id + ").setOnClickListener(cpt -> {target." + entry.getValue().getSimpleName().toString() + "(cpt);})");
                }
            }

            //生成类和方法
            TypeSpec typeSpec = TypeSpec.classBuilder(mClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.froad.butterknifeapi", "IViewBinder"), TypeName.get(mTypeElement.asType())))
                    .addMethod(bindBuild.build())
                    .addMethod(unBindBuild.build())
                    .build();

            return typeSpec;
        }
    }
}
