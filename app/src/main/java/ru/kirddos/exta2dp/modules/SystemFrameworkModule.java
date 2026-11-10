package ru.kirddos.exta2dp.modules;

import ru.kirddos.exta2dp.SourceCodecType;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import static ru.kirddos.exta2dp.ConstUtils.*;

public class SystemFrameworkModule extends ExtModule {
    private static final String TAG = "SystemFrameworkModule";

    private boolean frameworkHooked = false;

    @Override
    public void onModuleLoaded(@NonNull ModuleLoadedParam param) {
        log(TAG + " : " + param.getProcessName());
    }





    @SuppressWarnings("ConstantConditions")
    @SuppressLint({"BlockedPrivateApi", "DiscouragedPrivateApi", "PrivateApi"})
    @Override
    public void onPackageReady(@NonNull PackageReadyParam param) {
        if (frameworkHooked) return;
        frameworkHooked = true;
        try {
            Class<?> btCodecConfig = param.getClassLoader().loadClass("android.bluetooth.BluetoothCodecConfig");

            Method getCodecName = btCodecConfig.getDeclaredMethod("getCodecName", int.class);
            hookBefore(getCodecName, callback -> {

                int type = (int) callback.getArgs()[0];
                //log(TAG + " BluetoothCodecConfig: getCodecName");
                String name = getCustomCodecName(type);
                if (name != null) {
                    callback.returnAndSkip(name);
                }
                return null;
            });
        } catch (ClassNotFoundException | NoSuchMethodException | NullPointerException e) {
            log(TAG + " btCodecConfig: ", e);
        }
    }

    /*
    @SuppressWarnings("ConstantConditions")
    @SuppressLint({"BlockedPrivateApi", "DiscouragedPrivateApi", "PrivateApi"})
    @Override
    public void onSystemServerLoaded(@NonNull SystemServerLoadedParam param) {
        super.onSystemServerLoaded(param);

        log("onSystemServerLoaded");
        log("main classloader is " + this.getClass().getClassLoader());
        log("param classloader is " + param.getClassLoader());
        log("class classloader is " + SourceCodecType.class.getClassLoader());
        log("pid/tid: " + android.os.Process.myPid() + " " + android.os.Process.myTid());
        log("----------");

        log("In android/system");

        try {
            Class<?> audioSystem = param.getClassLoader().loadClass("android.media.AudioSystem");
            Method bluetoothCodecToAudioFormat = audioSystem.getDeclaredMethod("bluetoothCodecToAudioFormat", int.class);

            hookBefore(bluetoothCodecToAudioFormat, callback -> {
                try {
                    int btCodec = (int) callback.getArgs()[0];

                    if (btCodec == SOURCE_CODEC_TYPE_LHDCV2 || btCodec == SOURCE_CODEC_TYPE_LHDCV3 || btCodec == SOURCE_CODEC_TYPE_LHDCV5) {
                        callback.returnAndSkip(AUDIO_FORMAT_FLAC);
                    } else if (btCodec == SOURCE_CODEC_TYPE_LC3PLUS_HR) {
                        callback.returnAndSkip(AUDIO_FORMAT_LC3);
                    } else if (btCodec == SOURCE_CODEC_TYPE_FLAC) {
                        callback.returnAndSkip(AUDIO_FORMAT_FLAC);
                    }
                } catch (NullPointerException e) {
                    log(TAG + " Exception: ", e);
                }
                return null;
            });
            Method audioFormatToBluetoothSourceCodec = audioSystem.getDeclaredMethod("audioFormatToBluetoothSourceCodec", int.class);

            hookBefore(audioFormatToBluetoothSourceCodec, callback -> {
                try {
                    int audioFormat = (int) callback.getArgs()[0];
                    if (audioFormat == AUDIO_FORMAT_LHDC) {
                        //callback.returnAndSkip(SOURCE_CODEC_TYPE_LHDCV3);
                    } else if (audioFormat == AUDIO_FORMAT_LC3) {
                        callback.returnAndSkip(SOURCE_CODEC_TYPE_LC3PLUS_HR);
                    } else if (audioFormat == AUDIO_FORMAT_FLAC) {
                        callback.returnAndSkip(SOURCE_CODEC_TYPE_FLAC);
                    }
                } catch (NullPointerException e) {
                    log(TAG + " Exception: ", e);
                }
                return null;
            });

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log(TAG + " Exception: ", e);
        }
    }*/
}
