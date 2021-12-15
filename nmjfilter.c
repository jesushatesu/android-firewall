/*
 * 	
 *		этот файл добавляется в net/core и используется для отправки нужных данных о входящих или исходящих skb
 *		
 */

#include <asm/uaccess.h>
#include <linux/types.h>
#include <linux/kernel.h>
#include <linux/string.h>
#include <linux/mm.h>
#include <linux/socket.h>
#include <linux/sockios.h>
#include <linux/errno.h>
#include <linux/if_ether.h>
#include <linux/netdevice.h>
#include <linux/skbuff.h>
#include <net/net_namespace.h>
#include <net/sock.h>
#include <linux/rtnetlink.h>
#include <linux/netlink.h>
#include <linux/stat.h>
#include <linux/init.h>
#include <linux/ip.h>
#include <net/ip.h>
#include <linux/inetdevice.h>
#include <linux/vmalloc.h>
#include <linux/tcp.h>
#include <net/tcp.h>

#include "nmjfilter.h"


/**
 * Attributes are fields of data your messages will contain.
 * The designers of Netlink really want you to use these instead of just dumping
 * data to the packet payload... and I have really mixed feelings about it.
 */
enum attributes {
        /*
         * The first one has to be a throwaway empty attribute; I don't know
         * why.
         * If you remove it, ATTR_NMJSKB (the first one) stops working, because
         * it then becomes the throwaway.
         */
        ATTR_UNSPEC,
        ATTR_NMJSKB,

        /* This must be last! */
        __ATTR_MAX,
};

#define ATTR_MAX (__ATTR_MAX - 1)


/**
 * Here you can define some constraints for the attributes so Linux will
 * validate them for you.
 */
static struct nla_policy nmjpolicy[ATTR_MAX + 1] = {
                [ATTR_NMJSKB] = { .type = NLA_STRING, } //TODO: изменить аттрибут
};

/**
 * This callback runs whenever the socket receives messages.
 * We don't use it now, but Linux complains if we don't define it.
 */
static int receive_msg(struct sk_buff *skb, struct genl_info *info)
{
        printk(KERN_INFO "Received a message in kernelspace.\n");
        return 0;
}

/**
 * Message type codes. All you need is a hello sorta function, so that's what
 * I'm defining.
 */
enum commands {
		COMMAND_UNSPEC,
        COMMAND_NMJ,
        /* This must be last! */
        __COMMAND_MAX,
};

#define COMMAND_MAX (__COMMAND_MAX - 1)

enum nmj_grps {
	NMJ_SCAN,

};

/**
 * Actual message type definition.
 */
struct genl_ops nmj_ops[] = {
		{
                .cmd = COMMAND_NMJ,
                .flags = 0,
                .policy = nmjpolicy,
                .doit = receive_msg,
                .dumpit = NULL,
		},
};

/**
 * And more specifically, anyone who wants to hear messages you throw at
 * specific multicast groups need to register themselves to the same multicast
 * group, too.
 */
struct genl_multicast_group nmj_groups[] = {
        [NMJ_SCAN] = { .name = "NMJgroup" },
};


/**
 * A Generic Netlink family is a group of listeners who can and want to speak
 * your language.
 * Anyone who wants to hear your messages needs to register to the same family
 * as you.
 */
struct genl_family nmj_family = {
                .hdrsize = 0,
                .name = "NMJfamily",
                .policy = nmjpolicy,
                .module = THIS_MODULE,
                .version = 1,
                .maxattr = ATTR_MAX,
                .ops = nmj_ops,
                .n_ops = ARRAY_SIZE(nmj_ops),
                .mcgrps = nmj_groups,
                .n_mcgrps = ARRAY_SIZE(nmj_groups),
};


void print_skb_info(struct sk_buff *skb)
{
	struct iphdr *ip;
	struct nmj_buff *nmj;
	nmj = kmalloc(sizeof(struct nmj_buff), GFP_USER);
	
	strcpy(nmj->name, skb->dev->name);
	
	ip = (struct iphdr *)skb_network_header(skb);
	
	nmj->protocol = ip->protocol;
	nmj->ip_version = ip->version;
	

		nmj->saddr = ntohl(ip->saddr);
		nmj->daddr = ntohl(ip->daddr);
	
		if (ip->protocol == IPPROTO_TCP)
		{
			struct tcphdr *tcp;
			
			/* Задаем смещение в байтах для указателя на TCP заголовок */
			/* ip->ihl - длина IP заголовка в 32-битных словах */
			skb_set_transport_header(skb, ip->ihl * 4);
			
			/* Сохраняем указатель на структуру заголовка TCP */
			tcp = (struct tcphdr *)skb_transport_header(skb);
			
			nmj->source = tcp->source;
			nmj->dest = tcp->dest;
			//printk(KERN_INFO "nmj_buff TCP - name: %s, protocol: %d, ip_version: %d, saddr: %d.%d.%d.%d:%d, daddr: %d.%d.%d.%d:%d ", nmj->name, nmj->protocol, nmj->ip_version, nmj->saddr>>24, (nmj->saddr>>16)&0x00FF,(nmj->saddr>>8)&0x0000FF, (nmj->saddr)&0x000000FF, nmj->source, nmj->daddr>>24, (nmj->daddr>>16)&0x00FF,(nmj->daddr>>8)&0x0000FF, (nmj->daddr)&0x000000FF, nmj->dest);
			
	    }
	    if (ip->protocol == IPPROTO_UDP)
		{
			struct udphdr *udp;
			
			skb_set_transport_header(skb, ip->ihl * 4);
			udp = (struct udphdr *)skb_transport_header(skb);
			
			nmj->source = udp->source;
			nmj->dest = udp->dest;
			//printk(KERN_INFO "nmj_buff UDP - name: %s, protocol: %d, ip_version: %d, saddr: %d.%d.%d.%d:%d, daddr: %d.%d.%d.%d:%d ", nmj->name, nmj->protocol, nmj->ip_version, nmj->saddr>>24, (nmj->saddr>>16)&0x00FF,(nmj->saddr>>8)&0x0000FF, (nmj->saddr)&0x000000FF, nmj->source, nmj->daddr>>24, (nmj->daddr>>16)&0x00FF,(nmj->daddr>>8)&0x0000FF, (nmj->daddr)&0x000000FF, nmj->dest);
		}
		if (ip->protocol == IPPROTO_ICMP)
		{
		//printk(KERN_INFO "nmj_buff ICMP - name: %s, protocol: %d, ip_version: %d, saddr: %d.%d.%d.%d, daddr: %d.%d.%d.%d", nmj->name, nmj->protocol, nmj->ip_version, nmj->saddr>>24, (nmj->saddr>>16)&0x00FF,(nmj->saddr>>8)&0x0000FF, (nmj->saddr)&0x000000FF, nmj->daddr>>24, (nmj->daddr>>16)&0x00FF,(nmj->daddr>>8)&0x0000FF, (nmj->daddr)&0x000000FF);
		}
		
	nmj_genl_send_msg(nmj->name);//(char *)nmj, sizeof(struct nmj_buff));
	printk(KERN_INFO "NMJFilter completeded!");
	kfree_skb(skb);
}

void nmj_genl_send_msg(char *msg)
{
        struct sk_buff *skb;
        void *msg_head;
        int error;

        skb = genlmsg_new(NLMSG_GOODSIZE, GFP_KERNEL);
        if (!skb) {
                printk(KERN_INFO "NMJFilter genlmsg_new() failed.\n");
                goto end;
        }

        msg_head = genlmsg_put(skb, 0, 0, &nmj_family, 0, COMMAND_NMJ);
        if (!msg_head) {
                printk(KERN_INFO "NMJFilter genlmsg_put() failed.\n");
                kfree_skb(skb);
                goto end;
        }

        error = nla_put_string(skb, ATTR_NMJSKB, msg);
        if (error) {
                printk(KERN_INFO "NMJFilter nla_put_string() failed: %d\n", error);
                kfree_skb(skb);
                goto end;
        }

        genlmsg_end(skb, msg_head);

        /*
         * The family has only one group, so the group ID is just the family's
         * group offset.
         * mcgrp_offset is supposed to be private, so use this value for debug
         * purposes only.
         */
        error = genlmsg_multicast_allns(&nmj_family, skb, 0, 0, GFP_KERNEL);
        if (error) {
                printk(KERN_INFO "NMJFilter genlmsg_multicast_allns() failed: %d\n", error);
                goto end;
        }

        printk(KERN_INFO "NMJFilter success.\n");
end:
        return;
}

static int register_nmjfamily(void)
{
        int error;

        printk(KERN_INFO "NMJFilter registering family.\n");
        error = genl_register_family(&nmj_family);
        if (error)
                pr_err("NMJFilter family registration failed: %d\n", error);

        return error;
}

static int __init nmjfilter_init(void)
{

    printk("Entering NMJFILTER: %s\n", __FUNCTION__);
    
    int error;

    error = register_nmjfamily();
    if (error)
            return error;

    return 0;
}



subsys_initcall(nmjfilter_init);
