package me.diu.gachafight.di;

public interface ServiceLocator {
    <T> T getService(Class<T> serviceClass);
}
