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

#define LOG_TAG "NativeLog"

struct nmj_buff {
	char			name[128];
	__be16			protocol;
	__u8	ihl:4,
		ip_version:4;
	__be32	saddr;
	__be32	daddr;
	__be16	source;
	__be16	dest;
};

class JniNativeCallListener {
public:
	JniNativeCallListener(JNIEnv* pJniEnv, jobject pWrapperInstance);
	JniNativeCallListener(){};

	/* for onNativeStringCall(String) */
	void callJavaStringMethod(char* str);


private:
	JNIEnv* getJniEnv();

	/* Method descriptors*/
	jmethodID mOnNativeStringCallmID;

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
        COMMAND_NMJ,

        /* This must be last! */
        __COMMAND_MAX,
};

#define COMMAND_MAX (__COMMAND_MAX - 1)

JniNativeCallListener::JniNativeCallListener(JNIEnv* pJniEnv, jobject pWrappedInstance)
{
	pJniEnv->GetJavaVM(&mJVM);
	mObjectRef = pJniEnv->NewGlobalRef(pWrappedInstance);
	jclass clazz = pJniEnv->GetObjectClass(pWrappedInstance);

	mOnNativeStringCallmID = pJniEnv->GetMethodID(clazz, "onNativeStringCall", "(Ljava/lang/String;)V");
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

void JniNativeCallListener::callJavaStringMethod(char* str)
{
	JNIEnv* jniEnv = getJniEnv();
	jniEnv->CallVoidMethod(mObjectRef, mOnNativeStringCallmID, jniEnv->NewStringUTF(str));
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

    if (genl_hdr->cmd != COMMAND_NMJ) {
        return 0;   //Oops The message type is not mine; ignoring.
    }

    error = genlmsg_parse(nl_hdr, 0, attrs, ATTR_MAX, nmj_policy);
    if (error)
        return nmj_fail(-1, "Parsing message.");

    /* Remember: attrs[0] is a throwaway. */
    /*
    if (attrs[1])
            printf("ATTR_NMJSKB: len:%u type:%u data:%s\n",
                            attrs[1]->nla_len,
                            attrs[1]->nla_type,
                            (char *)nla_data(attrs[1]));
    else
            printf("ATTR_NMJSKB: null\n");

    printf("123");*/

    if (attrs[COMMAND_NMJ])
    {
        nmj_sk = (struct nmj_buff *)nla_data(attrs[1]);
        listener.callJavaStringMethod(nmj_sk->name);
        //nmj_log(nmj_sk->name);
    }
        //nmj_log("Incoming data...");

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

        /* Finally, receive the message. */
        //nl_recvmsgs_default(sk);

        return 0;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nomorejesus_nmjfilter_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */, jobject pNativeCallListener) {

    listener = JniNativeCallListener(env, pNativeCallListener);
    int err = do_things();
    if (err == -1)
        return env->NewStringUTF("Che-to ne to");

    return env->NewStringUTF("Vrode vsyo norm");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nomorejesus_nmjfilter_MainActivity_recvmsg(
        JNIEnv* env,
        jobject /* this */) {

    nl_recvmsgs_default(sk);

    return env->NewStringUTF("Vrode vsyo norm1");
}