package ru.kirddos.exta2dp.modules;

import java.lang.reflect.Member;

import io.github.libxposed.api.XposedInterface;

/**
 * Compatibility layer: keeps the old before/after callback style used by the modules
 * on top of the libxposed API 101 interceptor-chain model.
 */
interface BeforeCallback {
    /** The returned value is passed to the after callbacks as {@code state}. */
    Object before(HookCtx callback);
}

interface AfterCallback {
    void after(HookCtx callback, Object state);
}

final class HookCtx {
    private final XposedInterface.Chain chain;
    private final Object[] args;
    private Object result;
    private Throwable throwable;
    private boolean skipped;

    HookCtx(XposedInterface.Chain chain) {
        this.chain = chain;
        // Chain#getArgs() is immutable in API 101, keep a mutable copy and pass it to proceed()
        this.args = chain.getArgs().toArray();
    }

    Member getMember() {
        return chain.getExecutable();
    }

    Object getThisObject() {
        return chain.getThisObject();
    }

    Object[] getArgs() {
        return args;
    }

    Object getResult() {
        return result;
    }

    void setResult(Object result) {
        this.result = result;
        this.throwable = null;
    }

    Throwable getThrowable() {
        return throwable;
    }

    void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        if (throwable != null) this.result = null;
    }

    /** Skip the original method (and the remaining before callbacks) and return {@code result}. */
    Object returnAndSkip(Object result) {
        this.result = result;
        this.throwable = null;
        this.skipped = true;
        return null;
    }

    Object throwAndSkip(Throwable throwable) {
        this.throwable = throwable;
        this.result = null;
        this.skipped = true;
        return null;
    }

    boolean isSkipped() {
        return skipped;
    }

    /** Runs the rest of the chain / the original method, unless a before callback skipped it. */
    void proceedIfNeeded() {
        if (skipped) return;
        try {
            result = chain.proceed(args);
            throwable = null;
        } catch (Throwable t) {
            result = null;
            throwable = t;
        }
    }
}
