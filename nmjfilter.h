/*
 * 	
 *		этот файл добавится в net/core и будет использоваться для файла nmjfilter.c
 *		
 */

#include <linux/types.h>

struct nmj_buff {
	char			name[IFNAMSIZ];
	__be16			protocol;
	__u8			ip_version;
	__be32	saddr;
	__be32	daddr;
	__be16	source;
	__be16	dest;
};

struct sock *nl_sk = NULL;
int inputPid;
extern struct net init_net;

void print_skb_info(struct sk_buff *skb);
static void nl_recv_msg(struct sk_buff *skb);
static void nl_send_msg(char *msg, uint16_t len);

static int __init nmjfilter_init(void);
