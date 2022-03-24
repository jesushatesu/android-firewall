package com.nomorejesus.nmjfilter;

public interface NativeCallListener {
    void onNativeRcvCall(String arg);

    void onNativeSndCall(String arg);
}
