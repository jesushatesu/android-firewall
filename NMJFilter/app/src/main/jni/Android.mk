LOCAL_PATH := $(call my-dir)

#static library libnl-3 info
LOCAL_MODULE := libnl-3
LOCAL_MODULE_FILENAME := libnl-3
LOCAL_SRC_FILES := ./prebuild/libnl-3.a
LOCAL_EXPORT_C_INCLUDES := ./prebuild/include/libnl3
include $(PREBUILT_STATIC_LIBRARY)

#static library libnl-cli-3 info
include $(CLEAR_VARS)
LOCAL_MODULE := libnl-cli-3
LOCAL_MODULE_FILENAME := libnl-cli-3
LOCAL_SRC_FILES := ./prebuild/libnl-cli-3.a
LOCAL_EXPORT_C_INCLUDES := ./prebuild/include/libnl3
include $(PREBUILT_STATIC_LIBRARY)

#static library libnl-genl-3 info
include $(CLEAR_VARS)
LOCAL_MODULE := libnl-genl-3
LOCAL_MODULE_FILENAME := libnl-genl-3
LOCAL_SRC_FILES := ./prebuild/libnl-genl-3.a
LOCAL_EXPORT_C_INCLUDES := ./prebuild/include/libnl3
include $(PREBUILT_STATIC_LIBRARY)

#static library libnl-idiag-3 info
include $(CLEAR_VARS)
LOCAL_MODULE := libnl-idiag-3
LOCAL_MODULE_FILENAME := libnl-idiag-3
LOCAL_SRC_FILES := ./prebuild/libnl-idiag-3.a
LOCAL_EXPORT_C_INCLUDES := ./prebuild/include/libnl3
include $(PREBUILT_STATIC_LIBRARY)

#static library libnl-genl-3 info
include $(CLEAR_VARS)
LOCAL_MODULE := libnl-nf-3
LOCAL_MODULE_FILENAME := libnl-nf-3
LOCAL_SRC_FILES := ./prebuild/libnl-nf-3.a
LOCAL_EXPORT_C_INCLUDES := ./prebuild/include/libnl3
include $(PREBUILT_STATIC_LIBRARY)

#static library libnl-route-3 info
include $(CLEAR_VARS)
LOCAL_MODULE := libnl-route-3
LOCAL_MODULE_FILENAME := libnl-route-3
LOCAL_SRC_FILES := ./prebuild/libnl-route-3.a
LOCAL_EXPORT_C_INCLUDES := ./prebuild/include/libnl3
include $(PREBUILT_STATIC_LIBRARY)

# wrapper info
include $(CLEAR_VARS)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/prebuild/include/libnl3
LOCAL_MODULE    := nmjfilter
#LOCAL_MODULE_FILENAME := libnmjfilter
LOCAL_SRC_FILES := nmjfilter.cpp
LOCAL_STATIC_LIBRARIES := libnl-3 libnl-cli-3 libnl-genl-3 libnl-idiag-3 libnl-nf-3 libnl-route-3

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)