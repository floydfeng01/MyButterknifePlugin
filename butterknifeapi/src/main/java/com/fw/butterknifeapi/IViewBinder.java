package com.fw.butterknifeapi;

public interface IViewBinder<T> {

    void bind(T t);

    void unBind (T t);
}
