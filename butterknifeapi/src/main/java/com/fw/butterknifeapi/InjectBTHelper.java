package com.fw.butterknifeapi;

import android.content.Context;

import java.lang.reflect.Constructor;

/**
 * 注解工具类,编译时
 */
public class InjectBTHelper {

    /**
     * 通过反射方式调用注入方法
     * @param mContext
     */
    public static void Inject(Context mContext) {
        try {
            Class cls = Class.forName(mContext.getClass().getCanonicalName() + "$$ViewBinder");
            Constructor constructor = cls.getConstructor();
            IViewBinder viewBinder = (IViewBinder) constructor.newInstance();
            viewBinder.bind(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用反注入
     * @param mContext
     */
    public static void UnInject (Context mContext) {
        try {
            Class cls = Class.forName(mContext.getClass().getCanonicalName() + "$$ViewBinder");
            Constructor constructor = cls.getConstructor();
            IViewBinder viewBinder = (IViewBinder) constructor.newInstance();
            viewBinder.unBind(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
