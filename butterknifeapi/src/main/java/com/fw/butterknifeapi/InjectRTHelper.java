package com.fw.butterknifeapi;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.fw.butterknifetool.rt.BindView;
import com.fw.butterknifetool.rt.OnClick;
import com.fw.butterknifetool.rt.UIContent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注解工具类，运行时
 */
public class InjectRTHelper {

    private static final Map<Context, List<Field>> fieldMap = new HashMap<>();

    public static void Inject (Context mContext) {
        System.out.println("Inject_InjectRTHelper>>>Inject");
        if (mContext == null) {
            return;
        }
        bindUIContent(mContext);
        bindViewId(mContext);
        bindClickListener(mContext);
    }

    public static void UnInject (Context mContext) {
        System.out.println("Inject_InjectRTHelper>>>UnInject");
        if (mContext == null) {
            return;
        }
        List<Field> list = fieldMap.remove(mContext);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Field field : list) {
            try {
                field.setAccessible(true);
                field.set(mContext, null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void bindViewId (Context mContext) {
        System.out.println("Inject_InjectRTHelper>>>bindViewId");
        if (mContext == null) {
            return;
        }
        Class<? extends Context> cls = mContext.getClass();
        Field[] fields = cls.getFields();
        List<Field> list = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(BindView.class)) {
                BindView bf = field.getAnnotation(BindView.class);
                if (bf == null) {
                    continue;
                }
                int resId = bf.value();
                View component = null;
                if (mContext instanceof Activity) {
                    component = ((Activity) mContext).findViewById(resId);
                }
                if (component == null) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    field.set(mContext, component);
                    list.add(field);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (list.size() > 0) {
            fieldMap.put(mContext, list);
        }
    }

    private static void bindClickListener (Context mContext) {
        System.out.println("Inject_InjectRTHelper>>>bindClickListener");
        if (mContext == null) {
            return;
        }
        Class cls = mContext.getClass();
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnClick.class)) {
                OnClick onClick = method.getAnnotation(OnClick.class);
                if (onClick == null) {
                    continue;
                }
                int[] resIds = onClick.value();
                for (int resId : resIds) {
                    if (resId <= 0) {
                        continue;
                    }
                    View component = null;
                    if (mContext instanceof Activity) {
                        component = ((Activity) mContext).findViewById(resId);
                    }
                    if (component == null) {
                        continue;
                    }
                    component.setOnClickListener(cpt -> {
                        try {
                            method.setAccessible(true);
                            method.invoke(mContext, cpt);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    private static void bindUIContent (Context mContext) {
        System.out.println("Inject_InjectRTHelper>>>bindUIContent");
        if (mContext == null) {
            return;
        }
        Class cls = mContext.getClass();
        if (cls.isAnnotationPresent(UIContent.class)) {
            UIContent uiContent = (UIContent) cls.getAnnotation(UIContent.class);
            if (uiContent == null) {
                return;
            }
            int layoutId = uiContent.value();
            if (mContext instanceof Activity) {
                ((Activity) mContext).setContentView(layoutId);
            }
        }
    }
}
