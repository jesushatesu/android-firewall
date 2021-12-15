/*
 * 	
 *		этот файл добавляется в net/core и используется для файла nmjfilter.c и dev.c
 *		
 */
#include <linux/kernel.h>
#include <linux/netlink.h>
#include <net/netlink.h>
#include <net/net_namespace.h>
#include <net/genetlink.h>

struct nmj_buff {
	char			name[IFNAMSIZ];
	__be16			protocol;
	__u8	ihl:4,
		ip_version:4;
	__be32	saddr;
	__be32	daddr;
	__be16	source;
	__be16	dest;
};

static int register_nmjfamily(void);
void nmj_genl_send_msg(char *msg);
void print_skb_info(struct sk_buff *skb);
static int receive_msg(struct sk_buff *skb, struct genl_info *info);

static int __init nmjfilter_init(void);
