#include <jni.h>
#include <string>
#include <stdio.h>
#include <netlink/netlink.h>
#include <netlink/socket.h>
#include <netlink/msg.h>
#include <netlink/genl/genl.h>
#include <netlink/genl/ctrl.h>
#include <netlink/errno.h>
#include <stddef.h>
#include <android/log.h>
#include <arpa/inet.h>

#define LOG_TAG "NativeLog"

class JniNativeCallListener {
public:
	JniNativeCallListener(JNIEnv* pJniEnv, jobject pWrapperInstance);
	JniNativeCallListener(){};

	/* for onNativeRcvCall(String) */
	void callJavaRcvMethod(char* str);
	/* for onNativeSndCall(String) */
	void callJavaSndMethod(char* str);


private:
	JNIEnv* getJniEnv();

	/* Method descriptors*/
	jmethodID mOnNativeRcvCallmID;
	/* Method descriptors*/
	jmethodID mOnNativeSndCallmID;

	/* Reference to Java-object*/
	jobject mObjectRef;
	JavaVM* mJVM;
};

static struct nl_sock *sk;

JniNativeCallListener listener;

/**
 * Attributes and commands have to be the same as in kernelspace, so you might
 * want to move these enums to a .h and just #include that from both files.
 */
enum attributes {
        ATTR_UNSPEC,
        ATTR_NMJSKB,

        /* This must be last! */
        __ATTR_MAX,
};

#define ATTR_MAX (__ATTR_MAX - 1)

static struct nla_policy nmj_policy[ATTR_MAX + 1] = {
                [ATTR_NMJSKB] = { .type = NLA_STRING, }
};

enum commands {
		COMMAND_UNSPEC,
        COMMAND_RCVNMJ,
        COMMAND_SNDNMJ,
        /* This must be last! */
        __COMMAND_MAX,
};

#define COMMAND_MAX (__COMMAND_MAX - 1)

JniNativeCallListener::JniNativeCallListener(JNIEnv* pJniEnv, jobject pWrappedInstance)
{
	pJniEnv->GetJavaVM(&mJVM);
	mObjectRef = pJniEnv->NewGlobalRef(pWrappedInstance);
	jclass clazz = pJniEnv->GetObjectClass(pWrappedInstance);

	mOnNativeRcvCallmID = pJniEnv->GetMethodID(clazz, "onNativeRcvCall", "(Ljava/lang/String;)V");
	mOnNativeSndCallmID = pJniEnv->GetMethodID(clazz, "onNativeSndCall", "(Ljava/lang/String;)V");
}

JNIEnv* JniNativeCallListener::getJniEnv()
{
	JavaVMAttachArgs attachArgs;
	attachArgs.version = JNI_VERSION_1_6;
	attachArgs.name = ">>>NativeThread__Any";
	attachArgs.group = NULL;

	JNIEnv* env;
	if (mJVM->AttachCurrentThread(&env, &attachArgs) != JNI_OK) {
		env = NULL;
	}

	return env;
}

void JniNativeCallListener::callJavaRcvMethod(char* str)
{
	JNIEnv* jniEnv = getJniEnv();
	jniEnv->CallVoidMethod(mObjectRef, mOnNativeRcvCallmID, jniEnv->NewStringUTF(str));
}

void JniNativeCallListener::callJavaSndMethod(char* str)
{
	JNIEnv* jniEnv = getJniEnv();
	jniEnv->CallVoidMethod(mObjectRef, mOnNativeSndCallmID, jniEnv->NewStringUTF(str));
}

static int nmj_fail(int error, const char* text)
{
    __android_log_write(ANDROID_LOG_DEBUG, LOG_TAG, text);
    return error;
}

static int nmj_log(const char* text)
{
    __android_log_write(ANDROID_LOG_DEBUG, LOG_TAG, text);
}

/*
 * This function will be called for each valid netlink message received
 * in nl_recvmsgs_default()
 */
static int cb(struct nl_msg *msg, void *arg)
{
    struct nlmsghdr *nl_hdr;
    struct genlmsghdr *genl_hdr;
    struct nlattr *attrs[ATTR_MAX + 1];
    int error;
	struct nmj_buff *nmj_sk;
    //nmj_log("Entering to callback function...");

    nl_hdr = nlmsg_hdr(msg);
    genl_hdr = genlmsg_hdr(nl_hdr);

    if (genl_hdr->cmd != COMMAND_RCVNMJ && genl_hdr->cmd != COMMAND_SNDNMJ) {
        //nmj_log("Entering to IF...");
        return 0;   //Oops The message type is not mine; ignoring.
    }

    error = genlmsg_parse(nl_hdr, 0, attrs, ATTR_MAX, nmj_policy);
    if (error)
        return nmj_fail(-1, "Parsing message.");

    if (attrs[COMMAND_RCVNMJ])
    {
        char* strrcv = (char *)nla_data(attrs[COMMAND_RCVNMJ]);
        listener.callJavaRcvMethod(strrcv);
    }
    else
    {
        char* strsnd = (char *)nla_data(attrs[COMMAND_SNDNMJ]);
        listener.callJavaSndMethod(strsnd);
    }

    return 0;
}

static int do_things(void)
{
        struct genl_family *family;
        int group;
        int error;

        /* Socket allocation yadda yadda. */
        //nmj_log("Allocation socket...");
        sk = nl_socket_alloc();
        if (!sk)
        {
            return nmj_fail(-1, "Allocation socket");
        }
        //nmj_log("Allocation socket done.\n");

        nl_socket_disable_seq_check(sk);

        //nmj_log("Adding callback to socket...\n");
        error = nl_socket_modify_cb(sk, NL_CB_VALID, NL_CB_CUSTOM, cb, NULL);
        if (error)
        {
            return nmj_fail(-1, "Adding callback to socket");
        }
        //nmj_log("Adding callback to socket done.\n");

        //nmj_log("Connecting to socket...\n");
        error = genl_connect(sk);
        if (error)
        {
            return nmj_fail(-1, "Connecting to socket");
        }
        //nmj_log("Connecting to socket done.\n");

        /* Find the multicast group identifier and register ourselves to it. */
        //nmj_log("Search for socket's family and group...\n");
        group = genl_ctrl_resolve_grp(sk, "NMJfamily", "NMJgroup");
        if (group < 0)
        {
            return nmj_fail(-1, "Search for socket's family and group");
        }
        //nmj_log("Search for socket's family and group done.\n");


        //nmj_log("Adding group to socket...\n");
        error = nl_socket_add_memberships(sk, group, 0);
        if (error) {
            return nmj_fail(-1, "Adding group to socket");
        }
        //nmj_log("Adding group to socket...\n");

        return 0;
}

static int sendBlock(){
    int err;
    int family = genl_ctrl_resolve(sk, "NMJfamily");

    struct nl_msg *msg;
    if (!(msg = nlmsg_alloc()))
            nmj_fail(-1, "11111111");

    void *hdr;
    hdr = genlmsg_put(msg, NL_AUTO_PORT, NL_AUTO_SEQ, family, 0, 0, COMMAND_RCVNMJ, 1);
    if (!hdr)
            return nmj_fail(-1, "22222222");

    nla_put_string(msg, ATTR_NMJSKB, "nmjblock NADEUSI ");

    nl_send_sync(sk, msg);

    err = genl_send_simple(sk, family, COMMAND_RCVNMJ, 1, 0);

    return family;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nomorejesus_nmjfilter_MyService_stringFromJNI(
        JNIEnv* env,
        jobject /* this */, jobject pNativeCallListener) {

    listener = JniNativeCallListener(env, pNativeCallListener);
    int err = do_things();
    int err2 = sendBlock();
    if (err2 == -1)
        return env->NewStringUTF("Che-to ne to");

    return env->NewStringUTF("Vrode vsyo norm");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nomorejesus_nmjfilter_MyService_recvmsg(
        JNIEnv* env,
        jobject /* this */) {

    nl_recvmsgs_default(sk);

    return env->NewStringUTF("Vrode vsyo norm1");
}