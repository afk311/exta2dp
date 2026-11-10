package ru.kirddos.exta2dp.modules;

import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

/**
 * Common base for the ExtA2DP modules (libxposed API 101).
 * Provides hookBefore/hookAfter with the old callback semantics on top of Hooker/Chain.
 */
abstract class ExtModule extends XposedModule {
    private static final String LOG_TAG = "ExtA2DP";

    private static final class Entry {
        final CopyOnWriteArrayList<BeforeCallback> before = new CopyOnWriteArrayList<>();
        final CopyOnWriteArrayList<AfterCallback> after = new CopyOnWriteArrayList<>();
    }

    private final ConcurrentHashMap<Executable, Entry> entries = new ConcurrentHashMap<>();

    void log(String msg) {
        log(Log.INFO, LOG_TAG, msg);
    }

    void log(String msg, Throwable tr) {
        log(Log.ERROR, LOG_TAG, msg, tr);
    }

    private Entry entry(Method method) {
        // One real hook per method, callbacks are dispatched from the entry lists
        return entries.computeIfAbsent(method, m -> {
            Entry e = new Entry();
            hook(m).intercept(chain -> dispatch(e, chain));
            return e;
        });
    }

    void hookBefore(Method method, BeforeCallback callback) {
        entry(method).before.add(callback);
    }

    void hookAfter(Method method, AfterCallback callback) {
        entry(method).after.add(callback);
    }

    private Object dispatch(@NonNull Entry e, @NonNull XposedInterface.Chain chain) throws Throwable {
        HookCtx ctx = new HookCtx(chain);
        Object state = null;
        for (BeforeCallback cb : e.before) {
            try {
                state = cb.before(ctx);
            } catch (Throwable t) {
                log("before callback failed in " + chain.getExecutable(), t);
            }
            if (ctx.isSkipped()) break;
        }
        ctx.proceedIfNeeded();
        for (AfterCallback cb : e.after) {
            try {
                cb.after(ctx, state);
            } catch (Throwable t) {
                log("after callback failed in " + chain.getExecutable(), t);
            }
        }
        if (ctx.getThrowable() != null) throw ctx.getThrowable();
        return ctx.getResult();
    }
}
