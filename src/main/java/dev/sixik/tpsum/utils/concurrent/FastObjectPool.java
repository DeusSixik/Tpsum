package dev.sixik.tpsum.utils.concurrent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Function;

public final class FastObjectPool<T> {

    private final Function<FastObjectPool<T>, T> constructor;
    private final Consumer<T> initializer;
    private final Consumer<T> postRelease;
    private final AtomicReferenceArray<T> pool;
    private final int capacity;
    private final AtomicInteger top = new AtomicInteger(0);

    private final ThreadLocal<T> localCache = ThreadLocal.withInitial(() -> null);

    public FastObjectPool(Function<FastObjectPool<T>, T> constructor,
                          Consumer<T> initializer,
                          Consumer<T> postRelease,
                          int capacity) {
        this.constructor = Objects.requireNonNull(constructor);
        this.initializer = Objects.requireNonNull(initializer);
        this.postRelease = Objects.requireNonNull(postRelease);
        this.capacity = capacity;
        this.pool = new AtomicReferenceArray<>(capacity);

        for (int i = 0; i < capacity; i++) {
            pool.set(i, constructor.apply(this));
        }
        top.set(capacity);
    }

    public T alloc() {
        T cached = localCache.get();
        if (cached != null) {
            localCache.set(null);
            initializer.accept(cached);
            return cached;
        }

        while (true) {
            int index = top.get();
            if (index <= 0) {

                T obj = constructor.apply(this);
                initializer.accept(obj);
                return obj;
            }

            if (top.compareAndSet(index, index - 1)) {
                T obj = pool.getAndSet(index - 1, null);
                if (obj == null) continue;
                initializer.accept(obj);
                return obj;
            }
        }
    }

    public void release(T obj) {
        postRelease.accept(obj);

        if (localCache.get() == null) {
            localCache.set(obj);
            return;
        }

        while (true) {
            int index = top.get();
            if (index >= capacity) {
                return;
            }

            if (top.compareAndSet(index, index + 1)) {
                pool.set(index, obj);
                return;
            }
        }
    }
}
